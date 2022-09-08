package com.sharefable.api.controller.v1;

import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class CommonAssetController {
    private final S3Service s3Service;

    @Autowired
    public CommonAssetController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @RequestMapping(
        value = Routes.GET_CMN_ASSET,
        method = {RequestMethod.GET}
    )
    public ResponseEntity<byte[]> getProxyAsset(@PathVariable("type") String assetType, @PathVariable("name") String assetName) {
        log.info("Common resource is fetched with type {} & name {}", assetType, assetName);
        try {
            byte[] content = s3Service.getObjectContent("project/cmn/" + assetType + "/" + assetName);

            HttpHeaders headers = new HttpHeaders();
            String contentType = "";
            if (assetType.equalsIgnoreCase("js")) {
                // https://stackoverflow.com/a/21098951 -> application/javascript is obsolate
                contentType = "text/javascript";
                // For service worker only add this header to improve scope
                // https://stackoverflow.com/a/48068714
                if (assetName.equalsIgnoreCase("sw.js")) {
                    headers.add("Service-Worker-Allowed", "/");
                    headers.add("Cache-Control", "no-store must-revalidate");
                    headers.add("Max-Age", "0");
                }
            }

            headers.add("Content-Type", contentType);
            return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(content);
        } catch (IOException e) {
            log.error("Asset of type {} with name {} could not be retrieved. Error {}", assetType, assetName, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new byte[]{});
        }
    }
}
