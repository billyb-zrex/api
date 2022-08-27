package com.sharefable.appserver.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResp {
    public enum ResponseStatus { Success, Failure }
    public enum ErrorCode {
        IllegalArgs(100);

        public final Integer code;
        ErrorCode(Integer code) {
            this.code = code;
        }
    }

    private ResponseStatus status;
    private Object data;
    private String errStr;
    private ErrorCode errCode;
}
