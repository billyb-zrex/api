package com.sharefable.appserver.common.content;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public abstract class BaseParser {
    protected Object content;

    protected BaseParser(String htmlStr, boolean isBase64Encoded) {
        if (isBase64Encoded){
            byte[] decoded = Base64.getDecoder().decode(htmlStr);
            content = new String(decoded, StandardCharsets.UTF_8);
        } else {
            content = htmlStr;
        }
    }

    public Object getContent() {
        return content;
    }

    abstract String fileName();

    abstract Map<String, String> queryParams();
}
