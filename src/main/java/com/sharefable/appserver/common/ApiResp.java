package com.sharefable.appserver.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResp {
    public enum ResponseStatus { Success, Failure }

    private ResponseStatus status;
    private Object data;
}
