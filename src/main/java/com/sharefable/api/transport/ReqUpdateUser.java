package com.sharefable.api.transport;

import org.apache.commons.lang3.StringUtils;

@GenerateTSDef
public record ReqUpdateUser(String firstName, String lastName) {
    public ReqUpdateUser normalize() {
        String nFirstName = StringUtils.substring(firstName == null ? "" : firstName, 0, 49);
        String nLastName = StringUtils.substring(lastName == null ? "" : lastName, 0, 49);
        return new ReqUpdateUser(nFirstName, nLastName);
    }
}
