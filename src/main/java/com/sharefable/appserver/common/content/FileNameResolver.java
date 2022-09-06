package com.sharefable.appserver.common.content;

import lombok.Getter;

public abstract class FileNameResolver {
    @Getter
    protected String fileName;

    protected abstract String generateFileName();
}
