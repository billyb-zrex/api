package com.sharefable.api.transport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Base64Utils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class ProxyAssetReqParsedTest {
    @Test
    void validProxyAssetReqParsing() {
        ProxyAssetReq req = new ProxyAssetReq();
        req.setOrigin("https://fonts.googleapis.com/css?family=Google+Sans:300,400,500,700,800,900");
        req.setProjectId(1L);

        String clientInfo = "{ \"kie\": \"\", \"ua\": \"moz\" }";
        String encodedInfo = Base64Utils.encodeToString(clientInfo.getBytes(StandardCharsets.UTF_8));
        req.setClientInfo(encodedInfo);

        Optional<ProxyAssetReqParsed> parsed = ProxyAssetReqParsed.from(req);
        Assertions.assertTrue(parsed.isPresent());
        Assertions.assertInstanceOf(URL.class, parsed.get().getOriginParsed());
        Assertions.assertEquals("", parsed.get().getCookie());
        Assertions.assertEquals("moz", parsed.get().getUserAgent());
    }
}
