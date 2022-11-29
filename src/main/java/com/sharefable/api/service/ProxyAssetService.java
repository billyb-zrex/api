package com.sharefable.api.service;

import com.sharefable.api.common.Consts;
import com.sharefable.api.common.Utils;
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

import java.util.Optional;

@Service
@Slf4j
public class ProxyAssetService {
    private final ProxyAssetRepo proxyAssetRepo;
    private final RestTemplate restClient;
    private final S3Service s3Service;

    @Autowired
    public ProxyAssetService(ProxyAssetRepo proxyAssetRepo, RestTemplate restClient, S3Service s3Service) {
        this.proxyAssetRepo = proxyAssetRepo;
        this.restClient = restClient;
        this.s3Service = s3Service;
    }

    @Transactional
    public ProxyAssetResp createProxyAsset(ProxyAssetReqParsed body) {
        String origin = body.getOrigin();
        String hashedOrigin = DigestUtils.sha1Hex(origin);
        Optional<ProxyAsset> proxyAsset = proxyAssetRepo.findProxyAssetByBelongsToProjAndRid(body.getProjectId(), hashedOrigin);
        if (proxyAsset.isPresent()) {
            return ProxyAssetResp.from(proxyAsset.get());
        }

        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isBlank(body.getCookie())) {
            headers.add("Cookie", body.getCookie());
        }
        if (!StringUtils.isBlank(body.getUserAgent())) {
            headers.add("User-Agent", body.getUserAgent());
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> resp = this.restClient.exchange(origin, HttpMethod.GET, entity, byte[].class);
            if (resp.getBody() != null) {
                String fileName = Utils.createUuidWord();
                if (!StringUtils.isBlank(body.getAssumedFileExt())) {
                    fileName += body.getAssumedFileExt();
                }
                String filePath = body.getProjectId() + Consts.PATH_FOR_COMMON_ASSET + "/" + fileName;
                String fullQualifiedFilePath = this.s3Service.upload(filePath, S3Service.AssetType.Project, resp.getBody());

                ProxyAsset asset = ProxyAsset.builder()
                    .rid(hashedOrigin)
                    .fullOriginUrl(origin)
                    .proxyUri(fullQualifiedFilePath)
                    .belongsToProj(body.getProjectId())
                    .build();

                ProxyAsset savedAsset = proxyAssetRepo.save(asset);
                return ProxyAssetResp.from(savedAsset);
            } else {
                log.error("Cannot get asset {} . Empty body", origin);
                return ProxyAssetResp.Empty();
            }
        } catch (HttpStatusCodeException ex) {
            log.error("Cannot get asset {} [Status: {}, resp from server: {}]", origin, ex.getStatusCode(), ex.getResponseBodyAsString());
            ex.printStackTrace();
            return ProxyAssetResp.Empty();
        }
    }
}
