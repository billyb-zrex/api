package com.sharefable.appserver.common.req;

public class ReqParamMissingException extends Exception{
    public ReqParamMissingException(String msg) {
        super(msg);
    }
}
