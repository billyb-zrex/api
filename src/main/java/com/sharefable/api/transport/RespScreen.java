package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Screen;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@GenerateTSDef
public class RespScreen extends ResponseBase {
    // id and parentScreenId information are required by client to group the screens together while displaying
    // all available screens. In order to enable this functionality we are leaking id information to client, which
    // is not ideal, but we can handle this later on
    private Long id;
    private Long parentScreenId;
    private String rid;
    private String assetPrefixHash;
    private String displayName;
    private RespUser createdBy;
    private String thumbnail;
    private String url;
    private String icon;
    private Optional<RespTour> tour;

    public static RespScreen from(Screen screen) {
        try {
            RespScreen resp = (RespScreen) Utils.fromEntityToTransportObject(screen);
            // `fromEntityToTransportObject` can't convert collection<type> to collection<resp_type>
            // hence this explicit conversion is necessary
            // Also although screen <-> tour is saved as many-many relationship in db / jpa, it's
            // logically stored as many-one relationship
            if (screen.getTours() == null) {
                resp.setTour(Optional.empty());
            } else {
                resp.setTour(screen.getTours().stream().findFirst().map(RespTour::from));
            }
            return resp;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static RespScreen Empty() {
        return new RespScreen();
    }
}
