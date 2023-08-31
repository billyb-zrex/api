package com.sharefable.api.transport.req;

import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.PaymentTerms;

@GenerateTSDef
public record ReqSubscriptionInfo(
    PaymentTerms.Plan pricingPlan,
    PaymentTerms.Interval pricingInterval
) {
    public ReqSubscriptionInfo normalize() {
        PaymentTerms.Plan plan = pricingPlan() == null ? PaymentTerms.Plan.BUSINESS : pricingPlan();
        PaymentTerms.Interval interval = pricingInterval() == null ? PaymentTerms.Interval.YEARLY : pricingInterval();
        return new ReqSubscriptionInfo(plan, interval);
    }
}
