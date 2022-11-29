package com.sharefable.api.integration;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

public class HealthTest extends TestWithRunnerAndSetup {
    @SneakyThrows
    @Test
    void shouldRespondsToHealthCheck() {
        ApiResp resp = sendRequest(Routes.HEALTH, HttpMethod.GET);
        Assertions.assertEquals(ApiResp.ResponseStatus.Success, resp.getStatus());
        Assertions.assertEquals("ok", resp.getData());
    }
}
