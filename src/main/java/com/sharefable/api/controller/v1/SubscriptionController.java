package com.sharefable.api.controller.v1;

import com.chargebee.models.Event;
import com.chargebee.models.Subscription;
import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.SubscriptionService;
import com.sharefable.api.transport.req.ReqSubscriptionInfo;
import com.sharefable.api.transport.resp.RespSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subsService;

    @RequestMapping(value = Routes.CHECKOUT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespSubscription> createOrUpdateSubscription(@RequestBody ReqSubscriptionInfo subsInfo, @AuthUser User user) {
        ReqSubscriptionInfo info = subsInfo.normalize();
        RespSubscription subs = subsService.getSubscriptionForUser(user);
        if (subs == null) {
            subs = subsService.newSubscription(info, user);
        } else {
            subs = subsService.updateSubscription(info, user);
        }
        return ApiResp.<RespSubscription>builder().status(ApiResp.ResponseStatus.Success).data(subs).build();
    }

    @RequestMapping(value = Routes.GET_SUBSCRIPTION, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespSubscription> getSubscription(@AuthUser User user) {
        RespSubscription subs = subsService.getSubscriptionForUser(user);
        return ApiResp.<RespSubscription>builder().status(ApiResp.ResponseStatus.Success).data(subs).build();
    }

    @RequestMapping(value = Routes.GEN_CHECKOUT_URL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String generateCheckoutUrl(@AuthUser User user) {
        return subsService.createHostedPage(user);
    }

    @RequestMapping(value = Routes.CHARGEBEE_WEBHOOK, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleWebhook(@RequestBody String payload) {
        Event event = new Event(payload);
        log.warn("Subscription event {}", event.eventType());
        Subscription subs = event.content().subscription();
        switch (event.eventType()) {
            case SUBSCRIPTION_ACTIVATED, SUBSCRIPTION_REACTIVATED, SUBSCRIPTION_CHANGED, SUBSCRIPTION_PAUSED, SUBSCRIPTION_RESUMED, SUBSCRIPTION_RENEWED, SUBSCRIPTION_CANCELLED, SUBSCRIPTION_TRIAL_EXTENDED -> {
                subsService.resyncSubscription(subs);
            }
            case SUBSCRIPTION_TRIAL_END_REMINDER, PAYMENT_FAILED, PAYMENT_SUCCEEDED, PAYMENT_INITIATED, SUBSCRIPTION_RENEWAL_REMINDER -> {
            }
            default -> log.warn("No handler present for chargebee webhook {}", event.eventType());
        }

        return ResponseEntity.ok().build();
    }
}
