package com.sharefable.api.transport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
public abstract class ResponseBase {
    public Timestamp createdAt;
    public Timestamp updatedAt;
}
