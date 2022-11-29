package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Project;
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
public class ProjectResp {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String rid;
    private String displayName;
    private String thumbnail;
    private UserResp createdBy;

    public static ProjectResp from(Project project) {
        try {
            return Utils.fromEntityToTransportObject(project, ProjectResp.class,
                (Project entity, ProjectResp transportObj, List<Field> failedFields) ->
                    transportObj.setCreatedBy(UserResp.from(entity.getCreatedBy())));
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static ProjectResp Empty() {
        return new ProjectResp();
    }
}
