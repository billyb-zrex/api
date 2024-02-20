package com.sharefable.api.controller;

public interface Routes {
    String API_V1 = "/v1";

    /* === Routes w/o authentication === */
    String HEALTH = "/health";
    String GET_COMMON_CONFIG = "/cconfig";
    String GET_SCREEN = "/screen";
    String GET_TOUR = "/tour";
    String LOG_USER_EVENTS = "/lue";
    String NF_HOOK = "/nfhook";
    String REFRESH_SETTINGS = "/refreshsettings";
    String CHARGEBEE_WEBHOOK = "/wh/cb";

    /* === Routes with authentication === */
    String __BEHIND_LOGIN__ = "/f"; // f => behind spring security [F]ilters for authentication
    String IAM = __BEHIND_LOGIN__ + "/iam";
    String NEW_ORG = __BEHIND_LOGIN__ + "/neworg";
    String GET_ORG = __BEHIND_LOGIN__ + "/org";
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
    String DELETE_TOUR = __BEHIND_LOGIN__ + "/deltour";
    String UPDATE_TOUR_PROPERTY = __BEHIND_LOGIN__ + "/updtrprop";
    String UPLOAD_LINK = __BEHIND_LOGIN__ + "/getuploadlink";
    String RECORD_EL_EDIT = __BEHIND_LOGIN__ + "/recordeledit";
    String RECORD_TOUR_EDIT = __BEHIND_LOGIN__ + "/recordtredit";
    String RECORD_TOUR_LOADER_EDIT = __BEHIND_LOGIN__ + "/recordtrloaderedit";
    String RENAME_TOUR = __BEHIND_LOGIN__ + "/renametour";
    String RENAME_SCREEN = __BEHIND_LOGIN__ + "/renamescreen";
    String UPDATE_SCREEN_PROPERTY = __BEHIND_LOGIN__ + "/updatescreenproperty";
    String DUPLICATE_TOUR = __BEHIND_LOGIN__ + "/duptour";
    String ONBOADING_TOUR = __BEHIND_LOGIN__ + "/conbtrs";
    String TRANSCODE_VIDEO = __BEHIND_LOGIN__ + "/vdt";
    String RESIZE_IMG = __BEHIND_LOGIN__ + "/rzeimg";
    String CHECKOUT = __BEHIND_LOGIN__ + "/checkout";
    String GET_SUBSCRIPTION = __BEHIND_LOGIN__ + "/subs";
    String GET_ALL_USER_IN_ORG = __BEHIND_LOGIN__ + "/users";
    String ACTIVATE_OR_DEACTIVATE_USER = __BEHIND_LOGIN__ + "/aodusr";
    String GEN_CHECKOUT_URL = __BEHIND_LOGIN__ + "/genchckouturl";
    String PUBLISH_TOUR = __BEHIND_LOGIN__ + "/tpub";
}
