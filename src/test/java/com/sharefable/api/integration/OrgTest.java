package com.sharefable.api.integration;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.transport.NewOrgReq;
import com.sharefable.api.transport.OrgResp;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class OrgTest extends TestWithRunnerAndSetup {
    @SneakyThrows
    @Test
    void testNewOrgCreationWithoutThumbnail() {
        NewOrgReq newOrgReq = new NewOrgReq();
        newOrgReq.setDisplayName("Acme");

        String str = mapToJson(newOrgReq);
        ApiResp newOrgResp = sendRequest(
            Routes.API_V1 + Routes.NEW_ORG,
            HttpMethod.POST,
            str
        );

        Assertions.assertEquals(ApiResp.ResponseStatus.Success, newOrgResp.getStatus());
        OrgResp org = mapFromMap((Map<String, Object>) newOrgResp.getData(), OrgResp.class);

        Assertions.assertEquals("Acme", org.getDisplayName());
        Assertions.assertTrue(StringUtils.startsWith(org.getRid(), "acme-"), "Received rid=" + org.getRid());

        ApiResp getOrgResp = sendRequest(Routes.API_V1 + Routes.GET_ORG + "?id=" + org.getId(), HttpMethod.GET);
        Assertions.assertEquals(ApiResp.ResponseStatus.Success, newOrgResp.getStatus());
        OrgResp org2 = mapFromMap((Map<String, Object>) getOrgResp.getData(), OrgResp.class);

        Assertions.assertEquals(org, org2, "Org=" + org + "Org2=" + org2);
    }
}
