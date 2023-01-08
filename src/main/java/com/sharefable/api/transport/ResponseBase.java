package com.sharefable.api.transport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class ResponseBase {
    public Timestamp createdAt;
    public Timestamp updatedAt;
}
