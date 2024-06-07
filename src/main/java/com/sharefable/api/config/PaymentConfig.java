package com.sharefable.api.config;

import com.chargebee.Environment;
import com.sharefable.api.transport.PaymentTerms;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "com.sharefable.payment")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class PaymentConfig {
  private static final Map<PaymentTerms.Plan, Map<PaymentTerms.Interval, String>> PAYMENT_TERMS_PLAN = Map.of(
    PaymentTerms.Plan.SOLO,
    Map.of(
      PaymentTerms.Interval.MONTHLY, "solo-3-USD-Monthly",
      PaymentTerms.Interval.YEARLY, "solo-3-USD-Yearly"
    ),
    PaymentTerms.Plan.STARTUP,
    Map.of(
      PaymentTerms.Interval.MONTHLY, "startup-1-USD-Monthly",
      PaymentTerms.Interval.YEARLY, "startup-1-USD-Yearly"
    ),
    PaymentTerms.Plan.BUSINESS,
    Map.of(
      PaymentTerms.Interval.MONTHLY, "business-3-USD-Monthly",
      PaymentTerms.Interval.YEARLY, "business-3-USD-Yearly"
    ),
    // AppSumo lifetime tiers
    PaymentTerms.Plan.LIFETIME_TIER1,
    Map.of(
      PaymentTerms.Interval.LIFETIME, "tier-1-USD-lifetime"
    ),
    PaymentTerms.Plan.LIFETIME_TIER2,
    Map.of(
      PaymentTerms.Interval.LIFETIME, "tier-2-USD-lifetime"
    ),
    PaymentTerms.Plan.LIFETIME_TIER3,
    Map.of(
      PaymentTerms.Interval.LIFETIME, "tier-3-USD-lifetime"
    ),
    PaymentTerms.Plan.LIFETIME_TIER4,
    Map.of(
      PaymentTerms.Interval.LIFETIME, "tier-4-USD-lifetime"
    ),
    PaymentTerms.Plan.LIFETIME_TIER5,
    Map.of(
      PaymentTerms.Interval.LIFETIME, "tier-5-USD-lifetime"
    )
  );


  private String cbSiteName;

  private String cbApiKey;

  @PostConstruct
  public void configure() {
    Environment.configure(cbSiteName, cbApiKey);
  }

  public String getPlanId(PaymentTerms.Plan plan, PaymentTerms.Interval interval) {
    Map<PaymentTerms.Interval, String> plans = PAYMENT_TERMS_PLAN.get(plan);
    return plans == null ? null : plans.get(interval);
  }

  public Pair<PaymentTerms.Plan, PaymentTerms.Interval> getPlanItemsById(String planId) {
    for (Map.Entry<PaymentTerms.Plan, Map<PaymentTerms.Interval, String>> item : PAYMENT_TERMS_PLAN.entrySet()) {
      for (Map.Entry<PaymentTerms.Interval, String> item2 : item.getValue().entrySet()) {
        if (StringUtils.equals(item2.getValue(), planId)) {
          return Pair.with(item.getKey(), item2.getKey());
        }
      }
    }
    return null;
  }
}
