package com.sharefable.api.controller;

public interface Routes {
  String API_V1 = "/v1";

  /* === Vendor prefix === */
  String COBALT = "/vr/ct";
  String APP_SUMO = "/vr/as";
  String HUBSPOT = "/vr/hs";
  String ZAPIER = "/vr/zp";

  /* === Routes w/o authentication === */
  String HEALTH = "/health";
  String GET_COMMON_CONFIG = "/cconfig";
  String GET_SCREEN = "/screen";
  String GET_TOUR = "/tour";
  String GET_TOUR_BY_ID = "/tour/by/id/{id}";
  String LOG_USER_EVENTS = "/lue";
  String LOG_USER_EVENTS_DIRECT = "/lued";
  String NF_HOOK = "/nfhook";
  String REFRESH_SETTINGS = "/refreshsettings";
  String CHARGEBEE_WEBHOOK = "/wh/cb";
  String API_KEY_WEBHOOK_PROBE = "/apikey/probe";
  String FEATURE_PLAN_MATRIX = "/featureplanmtx";

  // only for migration
  String PUBLISH_TOUR_INTERNAL = "/m/tpub";

  // internal data entry routes
  String ADD_OR_UPDATE_PLATFORM_INTEGRATION = "/ide/platform_integration";

  /* === Routes with authentication === */
  String __BEHIND_LOGIN__ = "/f"; // f => behind spring security [F]ilters for authentication
  String IAM = __BEHIND_LOGIN__ + "/iam";
  String NEW_ORG = __BEHIND_LOGIN__ + "/neworg";
  String GET_ORG = __BEHIND_LOGIN__ + "/org";
  String UPDATE_ORG_PROPS = __BEHIND_LOGIN__ + "/updtorgprops";
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
  String ONBOARDING_TOUR = __BEHIND_LOGIN__ + "/conbtrs";
  String ONBOARDING_TOUR_PREVIEW_ONLY = __BEHIND_LOGIN__ + "/onbtrspreview";
  String TRANSCODE_VIDEO = __BEHIND_LOGIN__ + "/vdt";
  String RESIZE_IMG = __BEHIND_LOGIN__ + "/rzeimg";
  String CHECKOUT = __BEHIND_LOGIN__ + "/checkout";
  String GET_SUBSCRIPTION = __BEHIND_LOGIN__ + "/subs";
  String VALIDATE_SUBSCRIPTION_FOR_UPGRADE_OR_DOWNGRADE = __BEHIND_LOGIN__ + "/subsvalid";
  String GET_ALL_USER_IN_ORG = __BEHIND_LOGIN__ + "/users";
  String ACTIVATE_OR_DEACTIVATE_USER = __BEHIND_LOGIN__ + "/aodusr";
  String GEN_CHECKOUT_URL = __BEHIND_LOGIN__ + "/genchckouturl";
  String PUBLISH_TOUR = __BEHIND_LOGIN__ + "/tpub";
  String TOTAL_VIEWS = __BEHIND_LOGIN__ + "/totalviews";
  String ANN_VIEWS = __BEHIND_LOGIN__ + "/annviews";
  String STEPS_DURATION = __BEHIND_LOGIN__ + "/stpsdur";
  String CONVERSION = __BEHIND_LOGIN__ + "/convrsn";
  String GET_ALL_TOUR_LEADS = __BEHIND_LOGIN__ + "/gettrleads";
  String GET_LEAD_ACTIVITY_DATA_FILE = __BEHIND_LOGIN__ + "/getleadactvitydatafile";
  String TOKEN_FOR_LINKED_ACCOUNT = __BEHIND_LOGIN__ + COBALT + "/tknlnkdacc";
  String LIST_APPS_FOR_LINKED_ACCOUNT = __BEHIND_LOGIN__ + COBALT + "/lstapp";
  String COBALT_EVENT_AUTHED = __BEHIND_LOGIN__ + COBALT + "/evnt";
  String TENANT_INTEGRATIONS = __BEHIND_LOGIN__ + "/tenant_integrations";
  String TENANT_INTEGRATION = __BEHIND_LOGIN__ + "/tenant_integration";
  String DEL_TENANT_INTEGRATION = __BEHIND_LOGIN__ + "/delete/tenant_integration/{id}";
  String CREATE_NEW_API_KEY = __BEHIND_LOGIN__ + "/new/apikey";
  String GET_API_KEY = __BEHIND_LOGIN__ + "/apikey";
  String NEW_INVITE = __BEHIND_LOGIN__ + "/new/invite";
  String ALL_ORG_FOR_USER = __BEHIND_LOGIN__ + "/orgsfruser";
  String ASSIGN_ORG_TO_USER = __BEHIND_LOGIN__ + "/orgstouser";

  /* === Cross service w/o authentication === */

  String GET_ALL_TOURS_BY_API_KEY = "/via/ak/tours";
  String UPLOAD_LEAD_LEVEL_ANALYTICS = "/updleadanalytics";
  String ADD_OR_UPDATE_LEAD_INFO = "/ldinf";
  String HUBSPOT_DATA_FETCH_URL_HOOK = HUBSPOT + "/dfu";
  String APP_SUMO_WEBHOOK = APP_SUMO + "/whk";
  String APP_SUMO_REDIRECT_URL = APP_SUMO + "/redir";
  String ZAPIER_WEBHOOK_REG = ZAPIER + "/reghook";
  String ZAPIER_WEBHOOK_UN_REG = ZAPIER + "/unreghook";
  String ZAPIER_WEBHOOK_SAMPLE = ZAPIER + "/sample_data";
  String HOUSE_LEAD_INFO = "/hldinf";
  String GET_TOUR_ASSET_FILE_PATH = "/trasstpath";
  String POPULATED_LEAD_360 = "/poplead";
  String NEW_LOG = "/new/log";
  String GET_TENANT_INTEGRATION_BY_ID = "/fat/tenant_integration/{id}";
  // INFO although this is named as cobalt event, this event is fable's internal event and is used in multiple areas
  String COBALT_EVENT_PUB = COBALT + "/evnt";
  String FORCE_CREATE_LINKED_ACCOUNT = COBALT + "/forcecreatelinkedaccount";
}
