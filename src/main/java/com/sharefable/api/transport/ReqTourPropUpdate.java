package com.sharefable.api.transport;

import java.util.Optional;

@GenerateTSDef
public record ReqTourPropUpdate(
    String tourRid,
    Optional<Boolean> inProgress
) {
}
