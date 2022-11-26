package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.entity.Org;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewOrgResp {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String rid;
    private String displayName;
    private String thumbnail;

    public static NewOrgResp from(Org org) {
        NewOrgRespBuilder newOrgRespBuilder = NewOrgResp.builder()
            .id(org.getId())
            .createdAt(org.getCreatedAt())
            .updatedAt(org.getUpdatedAt())
            .rid(org.getRid())
            .displayName(org.getDisplayName());

        if (StringUtils.isNotBlank(org.getThumbnail())) {
            newOrgRespBuilder.thumbnail(org.getThumbnail());
        }
        return newOrgRespBuilder.build();
    }

    public static NewOrgResp Empty() {
        return NewOrgResp.builder().build();
    }
}
