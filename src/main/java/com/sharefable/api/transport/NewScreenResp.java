package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Screen;
import com.sharefable.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class NewScreenResp {
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String rId;
    private String assetPrefixHash;
    private String displayName;
    private User createdBy;
    private String thumbnail;
    private String url;
    private String icon;

    public static NewScreenResp from(Screen screen) {
        try {
            return Utils.fromEntityToTransportObject(screen, NewScreenResp.class, (Screen entity, NewScreenResp transportObj, List<Field> failedFields) -> {
                /* noop */
            });
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static NewScreenResp Empty() {
        return new NewScreenResp();
    }
}
