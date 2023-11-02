package com.sharefable.api.transport;

@GenerateTSDef
public class PaymentTerms {
    public enum Plan {
        STARTUP,
        BUSINESS
    }

    public enum Interval {
        MONTHLY,
        YEARLY
    }
}
