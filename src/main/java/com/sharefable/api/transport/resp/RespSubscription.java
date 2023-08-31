package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Subscription;
import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.PaymentTerms;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j
@GenerateTSDef
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespSubscription extends ResponseBase {
    private PaymentTerms.Plan paymentPlan;
    private PaymentTerms.Interval paymentInterval;
    private com.chargebee.models.Subscription.Status status;
    private Timestamp trialStartedOn;
    private Timestamp trialEndsOn;

    public static RespSubscription from(Subscription subs) {
        try {
            return (RespSubscription) Utils.fromEntityToTransportObject(subs);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
