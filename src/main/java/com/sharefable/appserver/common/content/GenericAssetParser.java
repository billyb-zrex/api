package com.sharefable.appserver.common.content;

public class GenericAssetParser extends BaseAssetBodyParser {
    protected final FileNameResolver fileNameResolver;

     public GenericAssetParser(FileNameResolver fileNameResolver, String assetStr, boolean isBase64Encoded) {
        super(assetStr, isBase64Encoded);
        this.fileNameResolver = fileNameResolver;
    }

    @Override
    public String fileName() {
        return fileNameResolver.getFileName();
    }
}
