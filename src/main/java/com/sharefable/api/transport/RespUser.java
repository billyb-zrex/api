package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class RespUser extends ResponseBase {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private RespOrg belongsToOrg;

    public static RespUser from(User user) {
        try {
            return (RespUser) Utils.fromEntityToTransportObject(user);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static RespUser Empty() {
        return new RespUser();
    }
}
