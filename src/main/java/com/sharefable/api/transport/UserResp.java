package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class UserResp {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;

    public static UserResp from(User user) {
        try {
            return Utils.fromEntityToTransportObject(user, UserResp.class, (User entity, UserResp transportObj, List<Field> failedFields) -> {
                /* noop */
            });
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static UserResp Empty() {
        return new UserResp();
    }
}
