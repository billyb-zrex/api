package com.sharefable.api.controller;

public interface Routes {
    String API_V1 = "/api/v1";

    String HEALTH = "/health";

    String NEW_PROJECT = "/project/new";

    String GET_ALL_PROJECTS = "/projects";

    String UPDATE_PROJECT = "/project/update/{id}";

    String NEW_ASSET = "/asset/new/{id}";

    String GET_PROXY_ASSET = "/asset/get/{id}/{*proxy}";

    String GET_CMN_ASSET = "/asset/cmn/{type}/{name}";
}
