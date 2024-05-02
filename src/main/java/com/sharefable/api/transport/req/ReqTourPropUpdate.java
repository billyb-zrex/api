package com.sharefable.api.transport.req;

import com.sharefable.api.transport.GenerateTSDef;
import com.sharefable.api.transport.Responsiveness;

import java.util.Map;
import java.util.Optional;

@GenerateTSDef
public record ReqTourPropUpdate(
  String tourRid,
  Optional<Map<String, Object>> site,
  Optional<Boolean> inProgress,
  Optional<Boolean> responsive,
  Optional<Responsiveness> responsive2
) {
}
