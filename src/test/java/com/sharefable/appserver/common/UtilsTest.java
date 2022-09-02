package com.sharefable.appserver.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

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
}
