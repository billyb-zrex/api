package com.sharefable.appserver.common;

public interface FieldParser<T, M> {
    T parse(M input);
}
