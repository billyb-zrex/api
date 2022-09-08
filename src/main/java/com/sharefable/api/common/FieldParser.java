package com.sharefable.api.common;

public interface FieldParser<T, M> {
    T parse(M input);
}
