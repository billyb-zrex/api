package com.sharefable.api.transport.req;

import com.sharefable.api.common.Utils;
import com.sharefable.api.transport.GenerateTSDef;

@GenerateTSDef
public record ReqRenameGeneric(String newName, String rid) {
    public ReqRenameGeneric normalizeDisplayName() {
        return new ReqRenameGeneric(Utils.normalizeWhitespace(newName()), rid);
    }
}
