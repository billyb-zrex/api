package com.sharefable.api.transport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public record NewOrgReq(String displayName, String thumbnail) {
    public NewOrgReq normalizeDisplayName() {
        return new NewOrgReq(displayName().trim(), thumbnail);
    }

    public ObjectValidationResult validate() {
        boolean isValid = true;
        List<String> msgs = new ArrayList<>();
        if (StringUtils.isBlank(displayName())) {
            isValid = false;
            String msg = "Org name can't be empty";
            log.error(msg);
            msgs.add(msg);
        }

        return new ObjectValidationResult(isValid, msgs);
    }
}
