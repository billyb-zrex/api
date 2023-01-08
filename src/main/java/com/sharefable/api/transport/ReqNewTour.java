package com.sharefable.api.transport;

import com.sharefable.api.common.Utils;

public record ReqNewTour(String name, String description) {
    public ReqNewTour normalizeDisplayName() {
        return new ReqNewTour(Utils.normalizeWhitespace(name()), description());
    }
}
