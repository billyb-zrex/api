package com.sharefable.appserver.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void normalizeProjectName() {
        String displayName = "Hello / world ___";
        String nName = Utils.normalizeProjectName(displayName);
        Assertions.assertEquals("hello_world____", nName);
    }
}
