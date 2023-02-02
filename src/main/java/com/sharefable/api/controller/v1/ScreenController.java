package com.sharefable.api.controller.v1;


import com.sharefable.api.auth.UserPrincipal;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.ScreenService;
import com.sharefable.api.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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


    @RequestMapping(value = Routes.COPY_SCREEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespScreen> copyScreen(@RequestBody ReqCopyScreen body, @AuthenticationPrincipal UserPrincipal principal) {
        RespScreen respScreen = screenService.copyFromParentScreen(body, principal.userEntity());
        return ApiResp.<RespScreen>builder().data(respScreen).build();
    }

    @RequestMapping(value = Routes.GET_ALL_SCREENS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespScreen[]> getAllScreensForOrg(@AuthenticationPrincipal UserPrincipal principal) {
        Long orgId = principal.userEntity().getBelongsToOrg();
        List<RespScreen> allScreens = screenService.getAllScreensForOrg(orgId);
        return ApiResp.<RespScreen[]>builder().status(ApiResp.ResponseStatus.Success).data(allScreens.toArray(RespScreen[]::new)).build();
    }

    @RequestMapping(value = Routes.GET_SCREEN, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespScreen> getScreenByRId(@RequestParam("rid") String rId) {
        Optional<RespScreen> maybeScreen = screenService.getScreenByRid(rId);
        if (maybeScreen.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, String.format("Unable to find screen with rid %s", rId));
        }

        return ApiResp.<RespScreen>builder().data(maybeScreen.get()).build();
    }

    @RequestMapping(value = Routes.RECORD_EL_EDIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespScreen> recordEdit(@RequestBody ReqRecordEdit body, @AuthenticationPrincipal UserPrincipal principal) {
        RespScreen resp = screenService.updateEditForScreen(body, principal.userEntity());
        return ApiResp.<RespScreen>builder().data(resp).build();
    }

    @RequestMapping(value = Routes.RENAME_SCREEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespScreen> renameTour(@RequestBody ReqRenameGeneric body, @AuthenticationPrincipal UserPrincipal principal) {
        ReqRenameGeneric nBody = body.normalizeDisplayName();
        RespScreen resp = screenService.renameScreen(nBody, principal.userEntity());
        return ApiResp.<RespScreen>builder().data(resp).build();
    }
}
