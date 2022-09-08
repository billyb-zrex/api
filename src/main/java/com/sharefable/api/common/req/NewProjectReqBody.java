package com.sharefable.api.common.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class NewProjectReqBody {
    private String name;
}
