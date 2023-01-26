package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.UserPrincipal;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.TourService;
import com.sharefable.api.transport.ReqNewTour;
import com.sharefable.api.transport.ReqRecordEdit;
import com.sharefable.api.transport.ReqRenameTour;
import com.sharefable.api.transport.RespTour;
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
public class TourController {
    private final TourService tourService;

    @Autowired
    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @RequestMapping(value = Routes.GET_ALL_TOURS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespTour[]> getAllTours(@AuthenticationPrincipal UserPrincipal principal) {
        Long orgId = principal.userEntity().getBelongsToOrg();
        List<RespTour> allTours = tourService.getAllToursForOrg(orgId);
        return ApiResp.<RespTour[]>builder().status(ApiResp.ResponseStatus.Success).data(allTours.toArray(RespTour[]::new)).build();
    }

    @RequestMapping(value = Routes.NEW_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespTour> newTour(@RequestBody ReqNewTour body, @AuthenticationPrincipal UserPrincipal principal) {
        ReqNewTour req = body.normalizeDisplayName();
        RespTour tour = tourService.createNewTour(req, principal.userEntity());
        return ApiResp.<RespTour>builder().status(ApiResp.ResponseStatus.Success).data(tour).build();
    }

    @RequestMapping(value = Routes.GET_TOUR, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespTour> getScreenByRId(@RequestParam("rid") String rId) {
        Optional<RespTour> maybeTour = tourService.getTourByRid(rId);
        if (maybeTour.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, String.format("Unable to find tour with rid %s", rId));
        }
        return ApiResp.<RespTour>builder().data(maybeTour.get()).build();
    }

    @RequestMapping(value = Routes.RECORD_TOUR_EDIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespTour> recordEdit(@RequestBody ReqRecordEdit body, @AuthenticationPrincipal UserPrincipal principal) {
        RespTour resp = tourService.updateEditForTour(body, principal.userEntity());
        return ApiResp.<RespTour>builder().data(resp).build();
    }

    @RequestMapping(value = Routes.RENAME_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespTour> renameTour(@RequestBody ReqRenameTour body, @AuthenticationPrincipal UserPrincipal principal) {
        RespTour resp = tourService.renameTour(body, principal.userEntity());
        return ApiResp.<RespTour>builder().data(resp).build();
    }
}
