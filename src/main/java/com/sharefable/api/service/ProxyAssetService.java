package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.ProxyAsset;
import com.sharefable.api.repo.ProxyAssetRepo;
import com.sharefable.api.transport.ProxyAssetReqParsed;
import com.sharefable.api.transport.ProxyAssetResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ProxyAssetService {
    private final ProxyAssetRepo proxyAssetRepo;
    private final RestTemplate restClient;
    private final S3Service s3Service;
    private final S3Config s3Config;

    @Autowired
    public ProxyAssetService(ProxyAssetRepo proxyAssetRepo, RestTemplate restClient, S3Service s3Service, S3Config s3Config) {
        this.proxyAssetRepo = proxyAssetRepo;
        this.restClient = restClient;
        this.s3Service = s3Service;
        this.s3Config = s3Config;
    }

    @Transactional
    public ProxyAssetResp createProxyAsset(ProxyAssetReqParsed body) {
        String origin = body.getOrigin();
        String hashedOrigin = DigestUtils.sha1Hex(origin);
        Optional<ProxyAsset> proxyAsset = proxyAssetRepo.findProxyAssetByRid(hashedOrigin);
        if (proxyAsset.isPresent()) {
            return ProxyAssetResp.from(proxyAsset.get(), s3Config);
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
            int status = resp.getStatusCodeValue();
            boolean isValidResponse = status >= 200 && status < 300;
            if (resp.getBody() != null && isValidResponse) {
                String fileName = Utils.createUuidWord();
                HttpHeaders respHeaders = resp.getHeaders();
                String contentType = null;
                // Get the Content-Type information from response and set it directly into the s3 bucket
                for (Map.Entry<String, List<String>> h : respHeaders.entrySet()) {
                    String headerName = h.getKey();
                    if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.CONTENT_TYPE)) {
                        // https://stackoverflow.com/a/50405667
                        contentType = String.join(",", h.getValue());
                    }
                }

                Map<String, String> userDefinedMetadata = new HashMap<>(1);
                if (contentType == null) {
                    log.warn("Content-Type header is not passed for resource {}. Client might not behave properly", origin);
                } else {
                    userDefinedMetadata.put(HttpHeaders.CONTENT_TYPE, contentType);
                }
                userDefinedMetadata.put("Orig-Url", origin);

                AssetFilePath assetFilePath = s3Config.getQualifiedPathFor(S3Config.AssetType.ProxyAsset, fileName);
                assetFilePath = s3Service.upload(assetFilePath, resp.getBody(), userDefinedMetadata);

                ProxyAsset asset = ProxyAsset.builder()
                    .rid(hashedOrigin)
                    .fullOriginUrl(origin)
                    .proxyUri(assetFilePath.getFilePath())
                    .httpStatus(status)
                    .build();

                ProxyAsset savedAsset = proxyAssetRepo.save(asset);
                return ProxyAssetResp.from(savedAsset, s3Config);
            } else {
                log.error("Cannot get asset {} . Empty body or not okay status. Status = {}", origin, status);
                return ProxyAssetResp.Empty();
            }
        } catch (HttpStatusCodeException ex) {
            log.error("Cannot get asset {} [Status: {}, resp from server: {}]", origin, ex.getStatusCode(), ex.getResponseBodyAsString());
            ex.printStackTrace();
            return ProxyAssetResp.Empty();
        }
    }
}
