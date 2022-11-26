package com.sharefable.api.common;

import org.apache.commons.lang3.RandomStringUtils;
import org.javatuples.Pair;

import javax.xml.bind.DatatypeConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Utils {
    static String getShortRandomId() {
        return RandomStringUtils.random(8, "0123456789abcdefghijklmnopqrstuvwxyz");
    }

    static String createReadableId(String name) {
        String uuid = getShortRandomId();
        return name.toLowerCase() + "-" + uuid;
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
}
