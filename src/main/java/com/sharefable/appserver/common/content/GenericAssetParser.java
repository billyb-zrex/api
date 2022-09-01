package com.sharefable.appserver.common.content;

import java.util.Map;

public class GenericAssetParser extends BaseParser {
    protected final FileNameResolver fileNameResolver;

     public GenericAssetParser(FileNameResolver fileNameResolver, String assetStr, boolean isBase64Encoded) {
        super(assetStr, isBase64Encoded);
        this.fileNameResolver = fileNameResolver;
    }

    @Override
    public String fileName() {
        return fileNameResolver.getFileName();
    }

    @Override
    Map<String, String> queryParams() {
        return fileNameResolver.getQueryParams();
    }
}
