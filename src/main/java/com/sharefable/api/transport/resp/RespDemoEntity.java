package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.EntityInfo;
import com.sharefable.api.common.TopLevelEntityType;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.DemoEntity;
import com.sharefable.api.entity.EntityConfigKV;
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
public class RespDemoEntity extends ResponseBase {
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
  private TopLevelEntityType entityType;
  private EntityInfo info;
  private Timestamp lastInteractedAt;
  @OptionalPropInTS
  private Object globalOpts;
  @OptionalPropInTS
  private TourSettings settings;

  public static RespDemoEntity from(DemoEntity demoEntity) {
    try {
      RespDemoEntity resp = (RespDemoEntity) Utils.fromEntityToTransportObject(demoEntity);

      S3Config.EntityFilesConfig entityFilesConfig = S3Config.getEntityFiles();
      resp.setPubDataFileName(entityFilesConfig.publishedDataFile().filename(demoEntity.getPublishedVersion()));
      resp.setPubLoaderFileName(entityFilesConfig.publishedLoaderFile().filename(demoEntity.getPublishedVersion()));
      resp.setPubEditFileName(entityFilesConfig.publishedEditFile().filename(demoEntity.getPublishedVersion()));
      resp.setPubTourEntityFileName(entityFilesConfig.publishedTourEntityFile().filename());

      return resp;
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
             InvocationTargetException e) {
      log.error("Can't convert entity to transport object. Error: " + e.getMessage());
      Sentry.captureException(e);
      return Empty();
    }
  }

  public static RespDemoEntity from(DemoEntity demoEntity, EntityConfigKV entityConfigKV) {
    RespDemoEntity resp = from(demoEntity);
    resp.setGlobalOpts(entityConfigKV == null ? null : entityConfigKV.getConfigVal());
    return resp;
  }

  private static RespDemoEntity Empty() {
    return new RespDemoEntity();
  }
}
