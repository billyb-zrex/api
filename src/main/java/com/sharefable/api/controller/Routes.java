package com.sharefable.api.controller;

public interface Routes {
    String __BEHIND_LOGIN__ = "/bf"; // bf => [B]ehind spring security [F]ilters for authentication

    String HEALTH = "/health";

    String API_V1 = "/v1";

    String NEW_ORG = __BEHIND_LOGIN__ + "/neworg";

    String GET_ORG = __BEHIND_LOGIN__ + "/org";

    String NEW_USER = "/newuser";

    String PROXY_ASSET = "/proxyasset";

    String NEW_SCREEN = __BEHIND_LOGIN__ + "/newscreen";
}
