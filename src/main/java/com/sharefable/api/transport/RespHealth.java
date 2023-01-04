package com.sharefable.api.transport;

import lombok.Builder;

@Builder
public class RespHealth extends ResponseBase {
    private final String status = "up";
}
