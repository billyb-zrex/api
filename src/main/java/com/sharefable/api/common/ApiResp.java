package com.sharefable.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResp {
    public enum ResponseStatus { Success, Failure }
    public enum ErrorCode {
        IllegalArgs(100),
        NotFound(101);

        public final int code;
        ErrorCode(int code) {
            this.code = code;
        }
        @JsonValue
        public int toValue() {
            return this.code;
        }
    }

    private ResponseStatus status;
    private Object data;
    private String errStr;
    private ErrorCode errCode;
}
