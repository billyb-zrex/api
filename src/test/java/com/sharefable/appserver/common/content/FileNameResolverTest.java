package com.sharefable.appserver.common.content;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

class FileNameResolverTest {
    @Test
    void genFileName() throws MalformedURLException {
        ProxyAssetFileNameResolver resolver1 = new ProxyAssetFileNameResolver(
            new URL("https://cdn.letsflyby.com"),
            new URL("https://cdn.letsflyby.com")
        );
        Assertions.assertTrue(
            resolver1.getFileName().matches("assets/" + ProxyAssetFileNameResolver.INDEX_FILE + "_[0-9a-zA-Z]+"),
            "filename: " + resolver1.getFileName()
        );

        resolver1 = new ProxyAssetFileNameResolver(
            new URL("https://cdn.letsflyby.com"),
            new URL("https://cdn.letsflyby.com/")
        );
        Assertions.assertTrue(
            resolver1.getFileName().matches("assets/" + ProxyAssetFileNameResolver.INDEX_FILE + "_[0-9a-zA-Z]+"),
            "filename: " + resolver1.getFileName()
        );


        resolver1 = new ProxyAssetFileNameResolver(
            new URL("https://cdn.letsflyby.com/app"),
            new URL("https://cdn.letsflyby.com/app")
        );
        Assertions.assertTrue(
            resolver1.getFileName().matches("assets/app_" + ProxyAssetFileNameResolver.INDEX_FILE + "_[0-9a-zA-Z]+"),
            "filename: " + resolver1.getFileName()
        );

        resolver1 = new ProxyAssetFileNameResolver(
            new URL("https://cdn.letsflyby.com/app"),
            new URL("https://cdn.letsflyby.com/app/home")
        );
        Assertions.assertTrue(
            resolver1.getFileName().matches("assets/app_home_[0-9a-zA-Z]+"),
            "filename: " + resolver1.getFileName()
        );

        resolver1 = new ProxyAssetFileNameResolver(
            new URL("https://cdn.letsflyby.com/app"),
            new URL("https://cdn.letsflyby.com/app/home/main.js")
        );
        Assertions.assertTrue(
            resolver1.getFileName().matches("assets/app_home_main_[0-9a-zA-Z]+\\.js"),
            "filename: " + resolver1.getFileName()
        );

        resolver1 = new ProxyAssetFileNameResolver(
            new URL("https://cdn.letsflyby.com/app"),
            new URL("https://cdn.letsflyby.com/app/home/main.js?fallback=some&ts=284&empt")
        );
        Assertions.assertTrue(
            resolver1.getFileName().matches("assets/app_home_main_[0-9a-zA-Z]+\\.js"),
            "filename: " + resolver1.getFileName()
        );
    }

//    @Test
    @SneakyThrows
    void test() {
        URL url = new URL("https://cdn.letsflyby.com/app/home/main.js?fallback=some&ts=284&ts=555&emp");
        System.out.println("path: " + url.getPath());
        System.out.println("file: " + url.getFile());
        System.out.println("q: " + url.getQuery());

        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(url.toURI()).build().getQueryParams();
        Map<String, String> params2 = params.toSingleValueMap();
        params2.forEach((key, value) -> System.out.println(key + ":" + value));
//        params.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
