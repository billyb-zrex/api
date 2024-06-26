package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.entity.EntityConfigKV;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.transport.GenerateTSDef;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@GenerateTSDef
public class RespTourWithScreens extends RespTour {
  private List<RespScreen> screens;
  private Optional<Map<String, String>> idxm;
  private RespCommonConfig cc;

  public static RespTourWithScreens from(Tour tour, EntityConfigKV entityConfigKV) {
    RespTourWithScreens resp = (RespTourWithScreens) RespTour.from(tour, entityConfigKV);
    // `fromEntityToTransportObject` can't convert collection<type> to collection<resp_type>
    // hence this explicit conversion is necessary
    // Also although screen <-> tour is saved as many-many relationship in db / jpa, it's
    // logically stored as many-one relationship
    if (tour.getScreens() == null) {
      resp.setScreens(List.of());
    } else {
      resp.setScreens(tour.getScreens().stream().map(RespScreen::from).toList());
    }
    return resp;
  }

  public static RespTourWithScreens from(Tour tour, RespCommonConfig cc, EntityConfigKV entityConfigKV) {
    RespTourWithScreens resp = from(tour, entityConfigKV);
    resp.setCc(cc);
    return resp;
  }

  private static RespTourWithScreens Empty() {
    return new RespTourWithScreens();
  }
}
