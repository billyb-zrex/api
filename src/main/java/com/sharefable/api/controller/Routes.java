package com.sharefable.api.controller;

public interface Routes {
    String __BEHIND_LOGIN__ = "/f"; // f => behind spring security [F]ilters for authentication

    String HEALTH = "/health";

    String API_V1 = "/v1";

    String NEW_ORG = __BEHIND_LOGIN__ + "/neworg";

    String GET_ORG = __BEHIND_LOGIN__ + "/org";

    String NEW_USER = "/newuser";

    String GET_USER = "/user";

    String PROXY_ASSET = __BEHIND_LOGIN__ + "/proxyasset";

    String NEW_SCREEN = __BEHIND_LOGIN__ + "/newscreen";
}
