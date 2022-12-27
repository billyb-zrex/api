package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Org;
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
public class OrgResp {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String rid;
    private String displayName;
    private String thumbnail;

    public static OrgResp from(Org org) {
        try {
            return Utils.fromEntityToTransportObject(org, OrgResp.class, (Org entity, OrgResp transportObj, List<Field> failedFields) -> {
                /* noop */
            });
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static OrgResp Empty() {
        return new OrgResp();
    }
}
