package com.sharefable.api.common.content;

import java.nio.charset.StandardCharsets;

/*
 * Parse body for requests for which status != 302
 */
public abstract class BaseAssetBodyParser {
    protected byte[] content;

    protected BaseAssetBodyParser(String contentStr, boolean isBase64Encoded) {
        if (isBase64Encoded) {
            content = this.decodeContent(contentStr);
        } else {
            content = contentStr.getBytes(StandardCharsets.UTF_8);
        }
    }

    protected abstract byte[] decodeContent(String encodedContent);

    public byte[] getContent() {
        return content;
    }

    public abstract String fileName();
}
