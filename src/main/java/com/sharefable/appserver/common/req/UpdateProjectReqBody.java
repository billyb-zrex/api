package com.sharefable.appserver.common.req;

import com.sharefable.appserver.common.UpdateLog;
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
