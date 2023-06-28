package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Tour;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
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

    public static RespTourWithScreens from(Tour tour) {
        try {
            RespTourWithScreens resp = (RespTourWithScreens) Utils.fromEntityToTransportObject(tour);
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
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    private static RespTourWithScreens Empty() {
        return new RespTourWithScreens();
    }
}
