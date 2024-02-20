package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.MediaProcessingService;
import com.sharefable.api.transport.req.ReqMediaProcessing;
import com.sharefable.api.transport.resp.RespMediaProcessingInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class MediaProcessingController {
    private final MediaProcessingService mpiService;

    @Autowired
    public MediaProcessingController(MediaProcessingService mpiService) {
        this.mpiService = mpiService;
    }

    @RequestMapping(value = Routes.TRANSCODE_VIDEO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    public ApiResp<RespMediaProcessingInfo[]> transcodeVideo(@RequestBody ReqMediaProcessing body) {
        RespMediaProcessingInfo[] infos = mpiService.transcodeVideoForStreaming(body);
        return ApiResp.<RespMediaProcessingInfo[]>builder().status(ApiResp.ResponseStatus.Success).data(infos).build();
    }

    @RequestMapping(value = Routes.RESIZE_IMG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    //@PreAuthorize("hasAuthority(@Perm.WRITE_SCREEN)")
    public ApiResp<RespMediaProcessingInfo> resizeImage(@RequestBody ReqMediaProcessing body, @RequestParam("pr") Optional<String> proposedResolution) {
        RespMediaProcessingInfo info = mpiService.resizeImage(body, proposedResolution.orElse("480"));
        return ApiResp.<RespMediaProcessingInfo>builder().status(ApiResp.ResponseStatus.Success).data(info).build();
    }
}
