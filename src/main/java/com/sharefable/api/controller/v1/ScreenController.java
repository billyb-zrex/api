package com.sharefable.api.controller.v1;


import com.sharefable.api.auth.UserPrincipal;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.transport.NewScreenReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class ScreenController {
    @RequestMapping(value = Routes.NEW_SCREEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp newUser(@RequestBody NewScreenReq body, @AuthenticationPrincipal UserPrincipal principal) {
        log.warn("body name:: {}, user name:: {}", body.name(), principal.userEntity().getFirstName());
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data("""
            {"r": "ok" }
            """).build();
    }
}
