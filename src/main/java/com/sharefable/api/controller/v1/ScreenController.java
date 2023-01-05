package com.sharefable.api.controller.v1;


import com.sharefable.api.auth.UserPrincipal;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.ScreenService;
import com.sharefable.api.transport.ReqNewScreen;
import com.sharefable.api.transport.RespScreen;
import com.sharefable.api.transport.ResponseBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class ScreenController {
    private final ScreenService screenService;

    @Autowired
    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @RequestMapping(value = Routes.NEW_SCREEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespScreen> newScreen(@RequestBody ReqNewScreen body, @AuthenticationPrincipal UserPrincipal principal) {
        ReqNewScreen req = body.normalizeDisplayName();
        RespScreen resp = screenService.createNewScreen(req, principal.userEntity());
        return ApiResp.<RespScreen>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
    }

    @RequestMapping(value = Routes.GET_ALL_SCREENS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<ResponseBase[]> getAllScreensForOrg(@AuthenticationPrincipal UserPrincipal principal) {
        Long orgId = principal.userEntity().getBelongsToOrg().getId();
        List<RespScreen> allScreensForOrg = screenService.getAllScreensForOrg(orgId);
        ResponseBase[] screens = allScreensForOrg.toArray(ResponseBase[]::new);
        return ApiResp.<ResponseBase[]>builder().status(ApiResp.ResponseStatus.Success).data(screens).build();
    }
}
