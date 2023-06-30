package com.sharefable.api.transport.req;

import com.sharefable.api.common.Utils;
import com.sharefable.api.transport.GenerateTSDef;

import java.util.Optional;

@GenerateTSDef
public record ReqNewTour(String name, Optional<String> description) {
    public ReqNewTour normalizeDisplayName() {
        return new ReqNewTour(Utils.normalizeWhitespace(name()), description());
    }
}
