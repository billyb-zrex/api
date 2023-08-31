package com.sharefable.api.transport;

@GenerateTSDef
public class PaymentTerms {
    public enum Plan {
        PRO,
        BUSINESS
    }

    public enum Interval {
        MONTHLY,
        YEARLY
    }
}
