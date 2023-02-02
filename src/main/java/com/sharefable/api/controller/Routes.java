package com.sharefable.api.controller;

public interface Routes {
    String __BEHIND_LOGIN__ = "/f"; // f => behind spring security [F]ilters for authentication
    String HEALTH = "/health";
    String API_V1 = "/v1";
    String NEW_ORG = /* __BEHIND_LOGIN__ + */ "/neworg";  // TODO uncomment this once the login is implemented
    String GET_ORG = __BEHIND_LOGIN__ + "/org";
    String NEW_USER = "/newuser";
    String GET_USER = "/user";
    String PROXY_ASSET = __BEHIND_LOGIN__ + "/proxyasset";
    String NEW_SCREEN = __BEHIND_LOGIN__ + "/newscreen";
    String GET_ALL_SCREENS = __BEHIND_LOGIN__ + "/screens";
    String GET_SCREEN = "/screen";
    String COPY_SCREEN = __BEHIND_LOGIN__ + "/copyscreen";
    String GET_COMMON_CONFIG = "/cconfig";
    String GET_ALL_TOURS = __BEHIND_LOGIN__ + "/tours";
    String NEW_TOUR = __BEHIND_LOGIN__ + "/newtour";
    String GET_TOUR = "/tour";
    String UPLOAD_LINK = __BEHIND_LOGIN__ + "/getuploadlink";
    String RECORD_EL_EDIT = __BEHIND_LOGIN__ + "/recordeledit";
    String RECORD_TOUR_EDIT = __BEHIND_LOGIN__ + "/recordtredit";
    String RENAME_TOUR = __BEHIND_LOGIN__ + "/renametour";
    String RENAME_SCREEN = __BEHIND_LOGIN__ + "/renamescreen";
}
