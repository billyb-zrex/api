package com.sharefable.api.config;

import com.chargebee.Environment;
import com.sharefable.api.transport.PaymentTerms;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        PaymentTerms.Plan.PRO,
        Map.of(
            PaymentTerms.Interval.MONTHLY, "pro-USD-Monthly",
            PaymentTerms.Interval.YEARLY, "pro-USD-Yearly"
        ),
        PaymentTerms.Plan.BUSINESS,
        Map.of(
            PaymentTerms.Interval.MONTHLY, "business-USD-Monthly",
            PaymentTerms.Interval.YEARLY, "business-USD-Yearly"
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
}
