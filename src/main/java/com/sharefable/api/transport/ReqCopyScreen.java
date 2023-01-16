package com.sharefable.api.transport;

@GenerateTSDef
public record ReqCopyScreen(
    Long parentId,
    String tourRid
) {
}
