package com.sharefable.appserver.common.content;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/*
 * Parse body for requests for which status != 302
 */
public abstract class BaseAssetBodyParser {
    protected String content;

    protected BaseAssetBodyParser(String contentStr, boolean isBase64Encoded) {
        if (isBase64Encoded){
            byte[] decoded = Base64.getDecoder().decode(contentStr);
            content = new String(decoded, StandardCharsets.UTF_8);
        } else {
            content = contentStr;
        }
    }

    public String getContent() {
        return content;
    }

    public abstract String fileName();
}
