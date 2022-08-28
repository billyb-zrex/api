package com.sharefable.appserver.controller;

public interface Routes {
    String API_V1 = "/api/v1";

    String HEALTH = "/health";

    String NEW_PROJECT = "/project/new";

    String GET_ALL_PROJECTS = "/projects";

    String UPDATE_PROJECT = "/project/update/{id}";
}
