package com.sharefable.appserver.common.content;

import java.util.Base64;

public class GenericTextAssetParser extends BaseAssetBodyParser {
    protected final FileNameResolver fileNameResolver;

     public GenericTextAssetParser(FileNameResolver fileNameResolver, String assetStr, boolean isBase64Encoded) {
        super(assetStr, isBase64Encoded);
        this.fileNameResolver = fileNameResolver;
    }

    @Override
    protected byte[] decodeContent(String encodedContent) {
        return Base64.getDecoder().decode(encodedContent);
    }

    @Override
    public String fileName() {
        return fileNameResolver.getFileName();
    }
}
