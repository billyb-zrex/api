package com.sharefable.api.common.content;

import lombok.Getter;

public abstract class FileNameResolver {
    @Getter
    protected String fileName;

    protected abstract String generateFileName();
}
