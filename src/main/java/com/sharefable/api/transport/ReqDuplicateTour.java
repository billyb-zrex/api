package com.sharefable.api.transport;

import com.sharefable.api.common.Utils;

@GenerateTSDef
public record ReqDuplicateTour(String duplicateTourName, String fromTourRid) {

    public ReqDuplicateTour normalizeDisplayName() {
        return new ReqDuplicateTour(Utils.normalizeWhitespace(duplicateTourName), fromTourRid);
    }
}
