package com.sharefable.api.transport;

import com.sharefable.api.common.Utils;

@GenerateTSDef
public record ReqRenameTour(String newName, String rid) {
    public ReqRenameTour normalizeDisplayName() {
        return new ReqRenameTour(Utils.normalizeWhitespace(newName()), rid);
    }
}
