package com.sharefable.api.common;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class UtilsTest {

    @Test
    void normalizeProjectName() {
        String displayName = "Hello / world ___";
        String nName = Utils.normalizeProjectName(displayName);
        Assertions.assertEquals("hello_world____", nName);
    }

    @Test
    void mediaTypeTest() {
        MediaType mediaType = MediaType.parseMediaType("nonenope/hii;a=b");
        System.out.println(mediaType.getType() + " :: " + mediaType.getSubtype() + " :: " + mediaType.getSubtypeSuffix());
        System.out.println(mediaType.getParameter("a"));
        System.out.println(mediaType);
    }

    @Test
    void getNearestMap() {
        Map<String, String> m1 = new HashMap<>();
        m1.put("ts", "2973498");
        m1.put("active", "1");
        m1.put("ref", "hn");

        Map<String, String> m2 = new HashMap<>();
        m2.put("ts", "09725");
        m2.put("ref", "ph");
        m2.put("cb", "none");

        ArrayList<Map<String, String>> list = new ArrayList<>();
        list.add(m1);
        list.add(m2);

        HashMap<String, String> matchWith = new HashMap<>();

        int nearestMapIndex = Utils.getNearestMap(list, matchWith);
        Assertions.assertEquals(0, nearestMapIndex);

        matchWith = new HashMap<>();
        matchWith.put("cb", "none");
        nearestMapIndex = Utils.getNearestMap(list, matchWith);
        Assertions.assertEquals(1, nearestMapIndex);


        matchWith = new HashMap<>();
        matchWith.put("active", "1");
        matchWith.put("cb", "default");
        nearestMapIndex = Utils.getNearestMap(list, matchWith);
        Assertions.assertEquals(0, nearestMapIndex);
    }

    @Test
    void test() {
        String proxyPath = "/_next/static/chunks/pages/[[...params]].js";
        String dec = UriUtils.encodePath(proxyPath, StandardCharsets.UTF_8);
        System.out.println(dec);
    }

    @Test
    @SneakyThrows
    void test2() {
        URL url = new URL("https://api.acme.com/home#part?key=value");
        System.out.println(url.getPath());
    }
}
