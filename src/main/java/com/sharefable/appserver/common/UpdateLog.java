package com.sharefable.appserver.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class UpdateLog<T> {
    public static<M, N> UpdateLog<M> clone(UpdateLog<N> log, FieldParser<M, N> parser){
        UpdateLog<M> newLog = new UpdateLog<>();
        newLog.setType(log.getType());
        newLog.setValue(log.getValue());
        newLog.setField(parser.parse(log.getField()));
        return newLog;
    }


    public static<M> UpdateLog<M> clone(UpdateLog<M> log, Object value){
        UpdateLog<M> newLog = new UpdateLog<>();
        newLog.setType(log.getType());
        newLog.setValue(value);
        newLog.setField(log.getField());
        return newLog;
    }

    public enum UpdateType { Str, Img }

    private T field;
    private Object value;
    private UpdateType type;
}
