package com.sharefable.api.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewProjectReq {
    private String displayName;
    private String thumbnail;
    private Long createdBy;
}
