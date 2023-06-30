package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.ProxyAsset;
import com.sharefable.api.repo.ProxyAssetRepo;
import com.sharefable.api.transport.ParsedReqProxyAsset;
import com.sharefable.api.transport.resp.RespProxyAsset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ProxyAssetService {
    private final ProxyAssetRepo proxyAssetRepo;
    private final RestTemplate restClient;
    private final S3Service s3Service;
    private final S3Config s3Config;

    String[] ignoreList = new String[]{"fonts.googleapis.com"};

    @Autowired
    public ProxyAssetService(ProxyAssetRepo proxyAssetRepo, RestTemplate restClient, S3Service s3Service, S3Config s3Config) {
        this.proxyAssetRepo = proxyAssetRepo;
        this.restClient = restClient;
        this.s3Service = s3Service;
        this.s3Config = s3Config;
    }

    @Transactional
    public RespProxyAsset createProxyAsset(ParsedReqProxyAsset body) {
        String origin = body.getOrigin();
        String hashedOrigin = DigestUtils.sha1Hex(origin);

        try {
            boolean shouldIgnore = Utils.isUrlPresentInIgnoreList(new URL(origin), ignoreList);
            if (shouldIgnore) {
                return RespProxyAsset.from(origin);
            }
        } catch (MalformedURLException e) {
            log.error("Could not match with ignore list as the url {} could not be parsed to URL.", origin);
            e.printStackTrace();
        }

        Optional<ProxyAsset> proxyAsset = proxyAssetRepo.findProxyAssetByRid(hashedOrigin);
        if (proxyAsset.isPresent()) {
            return RespProxyAsset.from(proxyAsset.get(), s3Config);
        }

        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isBlank(body.getCookie())) {
            headers.add(HttpHeaders.COOKIE, body.getCookie());
        }
        if (!StringUtils.isBlank(body.getUserAgent())) {
            headers.add(HttpHeaders.USER_AGENT, body.getUserAgent());
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> resp = this.restClient.exchange(origin, HttpMethod.GET, entity, byte[].class);

            // if css then convert the body to string and parse the body for further urls and process those again
            // if not then continue with previous code

            HttpHeaders respHeaders = resp.getHeaders();
            String contentType = Utils.getContentTypeFromHeader(respHeaders);
            String contentEncoding = Utils.getContentEncodingFromHeader(respHeaders);
            int status = resp.getStatusCode().value();
            boolean isRedirected = status == 302 || status == 301 || status == 307 || status == 308;
            boolean isValidResponse = status >= 200 && status < 300;
            if (isRedirected) {
                List<String> locations = respHeaders.get(HttpHeaders.LOCATION);
                String redirectTo = "";
                if (locations != null) {
                    redirectTo = locations.get(0);
                    if (redirectTo == null || redirectTo.isEmpty()) {
                        redirectTo = "";
                    }
                }
                log.info("Redirecting req for {} with status {} to {}", origin, status, redirectTo);
                if (!redirectTo.isEmpty()) {
                    Optional<ParsedReqProxyAsset> redirectProxyAsset = body.updateUrl(redirectTo);
                    if (redirectProxyAsset.isEmpty()) {
                        log.error("Cant form redirect url {}", redirectTo);
                        return RespProxyAsset.Empty();
                    } else {
                        return createProxyAsset(redirectProxyAsset.get());
                    }
                } else {
                    log.error("Asset returns redirection status {} but location not found", status);
                    return RespProxyAsset.Empty();
                }
            } else if (resp.getBody() != null && isValidResponse) {
                String fileName = Utils.createUuidWord();

                byte[] contentBody = resp.getBody();

                if (contentType.contains("css") && contentEncoding.isEmpty()) {
                    String resolvedBody = resolveNestedProxyForCssFile(new String(contentBody), body);
                    contentBody = resolvedBody.getBytes(StandardCharsets.UTF_8);
                }

                // Get the Content-Type information from response and set it directly into the s3 bucket
                Map<String, String> metadata = new HashMap<>(3);
                for (Map.Entry<String, List<String>> h : respHeaders.entrySet()) {
                    String headerName = h.getKey();
                    if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.CONTENT_TYPE)) {
                        // https://stackoverflow.com/a/50405667
                        metadata.put(HttpHeaders.CONTENT_TYPE, contentType);
                    } else if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.CONTENT_ENCODING)) {
                        metadata.put(HttpHeaders.CONTENT_ENCODING, contentEncoding);
                    }
                }
                metadata.put("Orig-Url", origin);

                AssetFilePath assetFilePath = s3Config.getQualifiedPathFor(S3Config.AssetType.ProxyAsset, fileName);
                assetFilePath = s3Service.upload(assetFilePath, contentBody, metadata);

                ProxyAsset asset = ProxyAsset.builder()
                    .rid(hashedOrigin)
                    .fullOriginUrl(origin)
                    .proxyUri(assetFilePath.getFilePath())
                    .httpStatus(status)
                    .build();

                ProxyAsset savedAsset = proxyAssetRepo.save(asset);
                return RespProxyAsset.from(savedAsset, s3Config);


            } else {
                log.error("Cannot get asset {} . Empty body or not okay status. Status = {}", origin, status);
                return RespProxyAsset.Empty();
            }

        } catch (HttpStatusCodeException ex) {
            log.error("Cannot get asset {} [Status: {}, resp from server: {}]", origin, ex.getStatusCode(), ex.getResponseBodyAsString());
            ex.printStackTrace();
            return RespProxyAsset.Empty();
        } catch (Exception ex) {
            log.error("Cannot get asset {} error {}", origin, ex.getMessage());
            ex.printStackTrace();
            return RespProxyAsset.Empty();
        }
    }

    private String resolveNestedProxyForCssFile(String content, ParsedReqProxyAsset body) {
        String respbody = content;
        ArrayList<Pair<String, String>> nestedUrls = new ArrayList<>();
        // format of url(...) or url("...") or url('...')
        Pattern urlRegex = Pattern.compile("url\\(\"(.*?)\"\\)|url\\('(.*?)'\\)|url\\((.*?)\\)");
        Matcher urlMatcher = urlRegex.matcher(respbody);

        while (urlMatcher.find()) {
            int l = urlMatcher.groupCount();
            String url = "";
            while (l > 0) {
                // the first group is always the full string hence the condition is not >= 0
                if (urlMatcher.group(l) != null) {
                    url = urlMatcher.group(l);
                    break;
                }
                l--;
            }
            if (StringUtils.isBlank(url)
                || StringUtils.startsWithIgnoreCase(url, "data:")
                || StringUtils.startsWithIgnoreCase(url, "#")) {
                continue;
            }
            url = url.trim();
            nestedUrls.add(Pair.with(url, urlMatcher.group(0)));
        }

        int l = nestedUrls.size();
        log.info("{} nested css found", l);
        int i = 0;
        for (Pair<String, String> urlPair : nestedUrls) {
            String url = urlPair.getValue0();
            String replaceTarget = urlPair.getValue1();
            log.info("Resolving nested css {} {}/{}", url, i++, l);
            Optional<ParsedReqProxyAsset> nestedParsedReqBody = body.updateUrl(url);
            if (nestedParsedReqBody.isEmpty()) continue;
            RespProxyAsset nestedProxyUri = createProxyAsset(nestedParsedReqBody.get());
            respbody = respbody.replace(replaceTarget, "url(" + nestedProxyUri.getProxyUri() + ")");
        }
        return respbody;
    }
}
