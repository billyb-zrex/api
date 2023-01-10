package com.sharefable.api.transport;

import com.sharefable.api.common.Utils;

import java.util.Optional;

@GenerateTSDef
public record ReqNewTour(String name, Optional<String> description) {
    public ReqNewTour normalizeDisplayName() {
        return new ReqNewTour(Utils.normalizeWhitespace(name()), description());
    }
}
