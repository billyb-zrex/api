package com.sharefable.api.common;

import java.lang.reflect.Field;
import java.util.List;

public interface EntityTransportConversionDelegate<K, T> {
    void apply(K entityObj, T transportObj, List<Field> failedToConvertFields);
}
