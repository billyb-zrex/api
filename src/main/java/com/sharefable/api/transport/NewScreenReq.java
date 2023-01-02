package com.sharefable.api.transport;

import java.util.Optional;

import static com.sharefable.api.common.Utils.normalizeWhitespace;

public record NewScreenReq(
    String name,
    String url,
    String thumbnail, // base64 image data
    Optional<String> favIcon,
    Optional<Long> parentId,
    String body
) {
    public Long normalizedParentId() {
        return parentId().orElse(0L);
    }

    public NewScreenReq normalizeDisplayName() {
        return new NewScreenReq(normalizeWhitespace(name()), url(), thumbnail(), favIcon(), parentId(), body());
    }
}
