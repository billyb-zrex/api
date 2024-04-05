package com.sharefable.api.transport.req;

import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.OptionalPropInTS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GenerateTSDef
public class ReqCreateOrUpdateTenantIntegration {
  String integrationType;
  @OptionalPropInTS
  Long tenantIntegrationId;
  @OptionalPropInTS
  Long relayId;
  String event;
  @OptionalPropInTS
  Boolean disabled;
  Map<String, Object> tenantConfig;
}
