package com.sharefable.api.transport.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.HouseLeadInfo;
import com.sharefable.api.entity.Lead360;
import com.sharefable.api.transport.GenerateTSDef;
import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@GenerateTSDef
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class RespHouseLeadInfo extends ResponseBase {
  private Long orgId;
  private String leadEmailId;
  private Set<Lead360> info360;

  public static RespHouseLeadInfo from(HouseLeadInfo houseLeadInfo) {
    try {
      return (RespHouseLeadInfo) Utils.fromEntityToTransportObject(houseLeadInfo);
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
             InvocationTargetException e) {
      log.error("Can't convert entity to transport object. Error: " + e.getMessage());
      Sentry.captureException(e);
      return Empty();
    }
  }

  public static RespHouseLeadInfo Empty() {
    return new RespHouseLeadInfo();
  }
}
