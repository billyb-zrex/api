package com.sharefable.api.transport.req;

import com.sharefable.api.transport.GenerateTSDef;

import java.util.Optional;

@GenerateTSDef
public record ReqTourPropUpdate(
  String tourRid,
  Optional<Boolean> inProgress
) {
}
