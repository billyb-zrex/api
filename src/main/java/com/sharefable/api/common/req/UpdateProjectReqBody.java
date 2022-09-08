package com.sharefable.api.common.req;

import com.sharefable.api.common.UpdateLog;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class UpdateProjectReqBody {
    private List<UpdateLog<String>> change;
}
