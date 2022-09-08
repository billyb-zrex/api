package com.sharefable.api.common.content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class HtmlParserTest {

    @Test
    void shouldBeAbleToParseAndEditHtml() throws MalformedURLException {
        String htmlStr = "" +
            "<!DOCTYPE html>" +
            "<html>" +
            "   <head>" +
            "       <link href=\"https://cdn.acme.com/style1.css\">" +
            "       <link rel=\"preload\" href=\"https://cdn.acme.com/style2.css\">" +
            "       <link rel=\"stylesheet\" href=\"https://cdn.acme.com/style3.css\">" +
            "       <script defer src=\"https://cdn.acme.com/script1.js\"></script>" +
            "       <script src=\"https://cdn.acme.com/script2.js\"></script>" +
            "       <script></script>" +
            "   </head>" +
            "   <body>" +
            "       <div>Welcome</div>" +
            "   </body>" +
            "</html>";
        BaseAssetBodyParser parser = new HtmlParser(
            new ProxyAssetFileNameResolver(
                new URL("https://cdn.letsflyby.com/app"),
                new URL("https://cdn.letsflyby.com/app")
            ),
            htmlStr,
            false
        );

        Document oriDoc = Jsoup.parse(htmlStr);
        Document parsedDoc = Jsoup.parse(new String(parser.getContent(), StandardCharsets.UTF_8));

        Assertions.assertEquals(
            oriDoc.getElementsByTag("script").size() + 1, // One script gets appended at the bottom
            parsedDoc.getElementsByTag("script").size());

        Assertions.assertEquals(
            oriDoc.getElementsByTag("link").size(),
            parsedDoc.getElementsByTag("link").size());

        Assertions.assertFalse(parsedDoc.getElementsByTag("link").get(0).hasAttr("href"));
        Assertions.assertEquals(
            "https://cdn.acme.com/style1.css",
            parsedDoc.getElementsByTag("link").get(0)
                .attr(HtmlParser.PROXY_PREFIX_ATTR_NAME + "href"));

        Assertions.assertFalse(parsedDoc.getElementsByTag("link").get(1).hasAttr("rel"));
        Assertions.assertEquals(
            "preload",
            parsedDoc.getElementsByTag("link").get(1)
                .attr(HtmlParser.PROXY_PREFIX_ATTR_NAME + "rel"));
        Assertions.assertEquals(
            "https://cdn.acme.com/style2.css",
            parsedDoc.getElementsByTag("link").get(1)
                .attr(HtmlParser.PROXY_PREFIX_ATTR_NAME + "href"));
        Assertions.assertEquals(2, parsedDoc.getElementsByTag("link").get(1).attributesSize());

        Assertions.assertEquals(
            "stylesheet",
            parsedDoc.getElementsByTag("link").get(2).attr("rel"));

        Assertions.assertEquals(
            "https://cdn.acme.com/script1.js",
            parsedDoc.getElementsByTag("script").get(0)
                .attr(HtmlParser.PROXY_PREFIX_ATTR_NAME + "src"));

        Elements bodyEls = parsedDoc.body().getAllElements();
        Element lasElOfBody = bodyEls.get(bodyEls.size() - 1);
        Assertions.assertEquals("script", lasElOfBody.tagName());
        Assertions.assertEquals(HtmlParser.PROXY_SCRIPT, lasElOfBody.outerHtml());
    }
}
