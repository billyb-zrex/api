package com.sharefable.api.integration;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.transport.NewOrgReqBody;
import com.sharefable.api.transport.NewOrgResp;
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
        NewOrgReqBody newOrgReqBody = new NewOrgReqBody();
        newOrgReqBody.setDisplayName("Acme");

        String str = mapToJson(newOrgReqBody);
        ApiResp newOrgResp = sendRequest(
            Routes.API_V1 + Routes.NEW_ORG,
            HttpMethod.POST,
            str
        );

        Assertions.assertEquals(ApiResp.ResponseStatus.Success, newOrgResp.getStatus());
        NewOrgResp org = mapFromMap((Map<String, Object>) newOrgResp.getData(), NewOrgResp.class);

        Assertions.assertEquals("Acme", org.getDisplayName());
        Assertions.assertTrue(StringUtils.startsWith(org.getRid(), "acme-"), "Received rid=" + org.getRid());

        ApiResp getOrgResp = sendRequest(Routes.API_V1 + Routes.GET_ORG + "?id=" + org.getId(), HttpMethod.GET);
        Assertions.assertEquals(ApiResp.ResponseStatus.Success, newOrgResp.getStatus());
        NewOrgResp org2 = mapFromMap((Map<String, Object>) getOrgResp.getData(), NewOrgResp.class);

        Assertions.assertEquals(org, org2, "Org=" + org + "Org2=" + org2);
    }
}
