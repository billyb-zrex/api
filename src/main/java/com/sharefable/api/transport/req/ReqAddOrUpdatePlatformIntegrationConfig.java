package com.sharefable.api.transport.req;

import java.util.Map;

public record ReqAddOrUpdatePlatformIntegrationConfig(
  String type,
  String name,
  String icon,
  String description,
  Boolean disabled,
  Map<String, Object> config
) {
}
