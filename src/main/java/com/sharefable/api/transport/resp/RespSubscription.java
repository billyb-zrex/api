package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.CreditInfo;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.EntityConfigKV;
import com.sharefable.api.entity.Subscription;
import com.sharefable.api.transport.Credit;
import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.PaymentTerms;
import io.sentry.Sentry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j
@GenerateTSDef
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespSubscription extends ResponseBase {
  private static final ObjectMapper mapper = new ObjectMapper();
  private PaymentTerms.Plan paymentPlan;
  private PaymentTerms.Interval paymentInterval;
  private com.chargebee.models.Subscription.Status status;
  private Timestamp trialStartedOn;
  private Timestamp trialEndsOn;
  private Credit creditInfo;

  public static RespSubscription from(Subscription subs) {
    try {
      return (RespSubscription) Utils.fromEntityToTransportObject(subs);
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
             InvocationTargetException e) {
      log.error("Can't convert entity to transport object. Error: " + e.getMessage());
      Sentry.captureException(e);
      return null;
    }
  }

  public static RespSubscription from(Subscription subs, List<EntityConfigKV> entityConfigKV) {
    RespSubscription resp = from(subs);
    if (resp == null) {
      return null;
    }

    Integer value = entityConfigKV.stream()
      .map(configKV -> mapper.convertValue(configKV.getConfigVal(), CreditInfo.class))
      .map(CreditInfo::getValue)
      .reduce(0, Integer::sum);

    resp.setCreditInfo(new Credit(value));

    return resp;
  }
}
