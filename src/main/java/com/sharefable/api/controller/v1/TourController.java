package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.TourService;
import com.sharefable.api.transport.EditTour;
import com.sharefable.api.transport.req.*;
import com.sharefable.api.transport.resp.RespTour;
import com.sharefable.api.transport.resp.RespTourWithScreens;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class TourController {
    private final TourService tourService;

    @RequestMapping(value = Routes.GET_ALL_TOURS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.READ_TOUR)")
    public ApiResp<RespTour[]> getAllTours(@AuthUser User user) {
        Long orgId = user.getBelongsToOrg();
        List<RespTour> allTours = tourService.getAllToursForOrg(orgId);
        return ApiResp.<RespTour[]>builder().status(ApiResp.ResponseStatus.Success).data(allTours.toArray(RespTour[]::new)).build();
    }

    @RequestMapping(value = Routes.NEW_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    public ApiResp<RespTour> newTour(@RequestBody ReqNewTour body, @AuthUser User user) {
        ReqNewTour req = body.normalizeDisplayName();
        RespTour tour = tourService.createNewTour(req, user);
        return ApiResp.<RespTour>builder().status(ApiResp.ResponseStatus.Success).data(tour).build();
    }

    @RequestMapping(value = Routes.GET_TOUR, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespTour> getTourByRId(@RequestParam("rid") String rId, @RequestParam("s") Optional<Boolean> shouldGetScreens) {
        RespTour tour = tourService.getTourByRid(rId, shouldGetScreens.orElse(Boolean.FALSE));
        return ApiResp.<RespTour>builder().data(tour).build();
    }

    @RequestMapping(value = Routes.RECORD_TOUR_EDIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    public ApiResp<RespTour> recordEdit(@RequestBody ReqRecordEdit body, @AuthUser User user) {
        RespTour resp = tourService.updateEditForTour(body, user, EditTour.INDEX);
        return ApiResp.<RespTour>builder().data(resp).build();
    }

    @RequestMapping(value = Routes.RECORD_TOUR_LOADER_EDIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    public ApiResp<RespTour> recordLoaderEdit(@RequestBody ReqRecordEdit body, @AuthUser User user) {
        RespTour resp = tourService.updateEditForTour(body, user, EditTour.LOADER);
        return ApiResp.<RespTour>builder().data(resp).build();
    }

    @RequestMapping(value = Routes.RENAME_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    public ApiResp<RespTour> renameTour(@RequestBody ReqRenameGeneric body, @AuthUser User user) {
        ReqRenameGeneric nBody = body.normalizeDisplayName();
        RespTour resp = tourService.renameTour(nBody, user);
        return ApiResp.<RespTour>builder().data(resp).build();
    }

    @RequestMapping(value = Routes.DUPLICATE_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    @Transactional
    public ApiResp<RespTourWithScreens> duplicateTour(@RequestBody ReqDuplicateTour body, @AuthUser User user) {
        ReqDuplicateTour nBody = body.normalizeDisplayName();
        RespTourWithScreens resp = tourService.duplicateTour(nBody, user);
        return ApiResp.<RespTourWithScreens>builder().data(resp).build();
    }

    @RequestMapping(value = Routes.DELETE_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    public ApiResp<RespTour[]> deleteTour(@RequestBody ReqTourRid body, @AuthUser User user) {
        List<RespTour> allTours = tourService.removeTour(body, user);
        return ApiResp.<RespTour[]>builder().status(ApiResp.ResponseStatus.Success).data(allTours.toArray(RespTour[]::new)).build();
    }

}
