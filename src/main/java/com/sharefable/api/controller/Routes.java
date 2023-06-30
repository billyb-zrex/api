package com.sharefable.api.controller;

public interface Routes {
    String API_V1 = "/v1";

    /* === Routes w/o authentication === */
    String HEALTH = "/health";
    String GET_COMMON_CONFIG = "/cconfig";
    String GET_SCREEN = "/screen";
    String GET_TOUR = "/tour";

    /* === Routes with authentication === */
    String __BEHIND_LOGIN__ = "/f"; // f => behind spring security [F]ilters for authentication
    String IAM = __BEHIND_LOGIN__ + "/iam";
    String NEW_ORG = __BEHIND_LOGIN__ + "/neworg";
    String GET_ORG_FOR_USER = __BEHIND_LOGIN__ + "/orgforiam";
    String ASSIGN_IMPLICIT_USER_ORG = __BEHIND_LOGIN__ + "/assgnimplorg";
    String UPDATE_USER_PROP = __BEHIND_LOGIN__ + "/userprop";
    String PROXY_ASSET = __BEHIND_LOGIN__ + "/proxyasset";
    String NEW_SCREEN = __BEHIND_LOGIN__ + "/newscreen";
    String CREATE_THUMBNAIL = __BEHIND_LOGIN__ + "/genthumb";
    String GET_ALL_SCREENS = __BEHIND_LOGIN__ + "/screens";
    String COPY_SCREEN = __BEHIND_LOGIN__ + "/copyscreen";
    String ASSOCIATE_SCREEN_TO_TOUR = __BEHIND_LOGIN__ + "/astsrntotour";
    String GET_ALL_TOURS = __BEHIND_LOGIN__ + "/tours";
    String NEW_TOUR = __BEHIND_LOGIN__ + "/newtour";
    String UPLOAD_LINK = __BEHIND_LOGIN__ + "/getuploadlink";
    String RECORD_EL_EDIT = __BEHIND_LOGIN__ + "/recordeledit";
    String RECORD_TOUR_EDIT = __BEHIND_LOGIN__ + "/recordtredit";
    String RENAME_TOUR = __BEHIND_LOGIN__ + "/renametour";
    String RENAME_SCREEN = __BEHIND_LOGIN__ + "/renamescreen";
    String UPDATE_SCREEN_PROPERTY = __BEHIND_LOGIN__ + "/updatescreenproperty";
    String DUPLICATE_TOUR = __BEHIND_LOGIN__ + "/duptour";
    String TRANSCODE_VIDEO = __BEHIND_LOGIN__ + "/vdt";
    String RESIZE_IMG = __BEHIND_LOGIN__ + "/rzeimg";
}
