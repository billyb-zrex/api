package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.SumViews;
import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.TotalVisitorsByYmd;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@GenerateTSDef
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class RespTourView {
    private Long tourId;
    private SumViews totalViews;
    private List<TotalVisitorsByYmd> totalVisitorsByYmd;

    public static RespTourView Empty() {
        return new RespTourView();
    }
}
