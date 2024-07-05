package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.EntityConfigKV;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.transport.*;
import io.sentry.Sentry;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Map;

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
  private Timestamp lastPublishedDate;
  private Boolean onboarding;
  private Boolean inProgress;
  private RespUser createdBy;
  private String pubDataFileName;
  private String pubLoaderFileName;
  private String pubEditFileName;
  private String pubTourEntityFileName;
  private Map<String, Object> site;
  private Boolean responsive;
  private Responsiveness responsive2;
  private TourDeleted deleted;
  @OptionalPropInTS
  private Object globalOpts;
  @OptionalPropInTS
  private TourSettings settings;

  public static RespTour from(Tour tour) {
    try {
      RespTour resp = (RespTour) Utils.fromEntityToTransportObject(tour);

      S3Config.EntityFilesConfig entityFilesConfig = S3Config.getEntityFiles();
      resp.setPubDataFileName(entityFilesConfig.publishedDataFile().filename(tour.getPublishedVersion()));
      resp.setPubLoaderFileName(entityFilesConfig.publishedLoaderFile().filename(tour.getPublishedVersion()));
      resp.setPubEditFileName(entityFilesConfig.publishedEditFile().filename(tour.getPublishedVersion()));
      resp.setPubTourEntityFileName(entityFilesConfig.publishedTourEntityFile().filename());

      return resp;
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
             InvocationTargetException e) {
      log.error("Can't convert entity to transport object. Error: " + e.getMessage());
      Sentry.captureException(e);
      return Empty();
    }
  }

  public static RespTour from(Tour tour, EntityConfigKV entityConfigKV) {
    RespTour resp = RespTour.from(tour);
    resp.setGlobalOpts(entityConfigKV == null ? null : entityConfigKV.getConfigVal());
    return resp;
  }

  private static RespTour Empty() {
    return new RespTour();
  }
}
