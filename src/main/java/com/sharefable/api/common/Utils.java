package com.sharefable.api.common;

import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Utils {
    static String getShortRandomId() {
        return RandomStringUtils.random(8, "0123456789abcdefghijklmnopqrstuvwxyz");
    }

    static String createReadableId(String name) {
        String uuid = getShortRandomId();
        return name.toLowerCase().replaceAll("\\s+", "-") + "-" + uuid;
    }

    static String createUuidWord() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    static Pair<byte[], ImageType> getImageDataFromBase64Str(String base64Data) {
        String[] dataSplit = base64Data.split(",");
        Pattern pattern = Pattern.compile("data:image/(.*?);base64", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSplit[0]);
        ImageType imgType = ImageType.Unknown;
        if (matcher.find()) {
            imgType = ImageType.de(matcher.group(1));
        }
        return Pair.with(DatatypeConverter.parseBase64Binary(dataSplit[1]), imgType);
    }

    static String getterMethodNameFromFieldName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
    }

    static String setterMethodNameFromFieldName(String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
    }

    static <K, T> T fromEntityToTransportObject(K entity, Class<T> clz, EntityTransportConversionDelegate<K, T> delegate)
        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        T transportObject = clz.getDeclaredConstructor().newInstance();
        Field[] fields = clz.getDeclaredFields();
        List<Field> notConvertedFields = new ArrayList<>(fields.length);
        for (Field field : fields) {
            try {
                Method getterFromEntity = entity.getClass().getMethod(getterMethodNameFromFieldName(field.getName()));
                Object valueFromEntity = getterFromEntity.invoke(entity);

                Method setterFromTransport = clz.getMethod(setterMethodNameFromFieldName(field.getName()), field.getType());
                setterFromTransport.invoke(transportObject, valueFromEntity);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                notConvertedFields.add(field);
            }
        }
        delegate.apply(entity, transportObject, notConvertedFields);

        return transportObject;
    }
}
