package com.sharefable.api.transport;

import com.sharefable.api.common.Utils;

@GenerateTSDef
public record ReqRenameGeneric(String newName, String rid) {
    public ReqRenameGeneric normalizeDisplayName() {
        return new ReqRenameGeneric(Utils.normalizeWhitespace(newName()), rid);
    }
}
