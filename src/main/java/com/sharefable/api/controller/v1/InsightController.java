package com.sharefable.api.controller.v1;

import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(Routes.API_V1)
@Slf4j
public class InsightController {
    private final InsightService firehoseService;

    @RequestMapping(value = Routes.LOG_USER_EVENTS, method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public String sendEvents(@RequestBody String userEventLogs) {
        firehoseService.sendEventsToFirehose(userEventLogs);
        return "ok";
    }
}
