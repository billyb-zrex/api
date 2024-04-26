package com.sharefable.api.transport.req;

import com.sharefable.api.transport.GenerateTSDef;

import java.util.Map;
import java.util.Optional;

@GenerateTSDef
public record ReqTourPropUpdate(
  String tourRid,
  Optional<Map<String, Object>> site,
  Optional<Boolean> inProgress,
  Optional<Boolean> responsive
) {
}
