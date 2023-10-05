package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.transport.GenerateTSDef;
import io.sentry.Sentry;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@GenerateTSDef
public class RespTour extends ResponseBase {
    private Long id;
    private String rid;
    private String assetPrefixHash;
    private String displayName;
    private String description;
    private Optional<Timestamp> lastPublishedDate;
    private RespUser createdBy;

    public static RespTour from(Tour tour) {
        try {
            return (RespTour) Utils.fromEntityToTransportObject(tour);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            Sentry.captureException(e);
            return Empty();
        }
    }

    private static RespTour Empty() {
        return new RespTour();
    }
}
