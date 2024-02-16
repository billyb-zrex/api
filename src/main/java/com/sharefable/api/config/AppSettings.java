package com.sharefable.api.config;

import com.sharefable.api.transport.SchemaVersion;

public record AppSettings(
    SchemaVersion currentSchemaVersion,
    String onboardingTourIds
) {
}
