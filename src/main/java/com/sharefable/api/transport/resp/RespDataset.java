package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Dataset;
import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.OptionalPropInTS;
import io.sentry.Sentry;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@GenerateTSDef
public class RespDataset {
  private String name;
  private Integer lastPublishedVersion;
  private Timestamp lastPublishedDate;
  @OptionalPropInTS
  private RespUploadUrl presignedUrl;

  public static RespDataset from(Dataset dataset) {
    try {
      RespDataset resp = new RespDataset();
      resp.setName(dataset.getName());
      resp.setLastPublishedDate(dataset.getLastPublishedDate());
      resp.setLastPublishedVersion(dataset.getLastPublishedVersion());
      return resp;
    } catch (Exception e) {
      log.error("Can't convert entity to transport object. Error: " + e.getMessage());
      Sentry.captureException(e);
      return Empty();
    }
  }

  public static RespDataset from(Dataset dataset, RespUploadUrl url) {
    RespDataset resp = from(dataset);
    resp.setPresignedUrl(url);
    return resp;
  }

  private static RespDataset Empty() {
    return new RespDataset();
  }
}
