package com.sharefable.api.common.content;

import javax.xml.bind.DatatypeConverter;

public class GenericBinaryAssetParser extends BaseAssetBodyParser {
    protected final FileNameResolver fileNameResolver;

    protected GenericBinaryAssetParser(FileNameResolver fileNameResolver, String contentStr, boolean isBase64Encoded) {
        super(contentStr, isBase64Encoded);
        this.fileNameResolver = fileNameResolver;
    }

    @Override
    protected byte[] decodeContent(String encodedContent) {
        return DatatypeConverter.parseBase64Binary(encodedContent);
    }

    @Override
    public String fileName() {
        return fileNameResolver.getFileName();
    }
}
