package com.sharefable.api.transport;

public record NewScreenReq(
    String name
//    String url,
//    String thumbnail,
//    String favIcon,
//    Optional<Long> parentId,
//    String body
) {
//    boolean isRootScreen() {
//        return parentId().isEmpty();
//    }
}
