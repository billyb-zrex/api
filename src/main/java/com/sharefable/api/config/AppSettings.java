package com.sharefable.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.entity.Settings;
import com.sharefable.api.repo.AppSettingsRepo;
import com.sharefable.api.transport.SchemaVersion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "com.sharefable.api.self")
@Getter
@Slf4j
public class AppSettings {
  @Getter(AccessLevel.NONE)
  private final AppSettingsRepo settingsRepo;
  private final ObjectMapper mapper = new ObjectMapper();
  private SchemaVersion currentSchemaVersion;
  private String onboardingTourIds;
  private boolean isMigrationFlatSet;
  private boolean isDataEntryFlagSet;
  private Map<String, Object> featurePlanMatrix;
  @Setter
  private String publicEndpoint;

  @Autowired
  public AppSettings(AppSettingsRepo settingsRepo) {
    this.settingsRepo = settingsRepo;
    load();
  }

  public void load() {
    Iterable<Settings> settings = settingsRepo.findAll();
    HashMap<String, String> hm = new HashMap<>();
    for (Settings setting : settings) {
      hm.put(setting.getK(), setting.getV());
    }

    currentSchemaVersion = SchemaVersion.of(hm.get("CURRENT_SCHEMA_VERSION"));
    onboardingTourIds = hm.getOrDefault("ONBOARDING_TOUR_IDS", "");
    try {
      TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
      };
      featurePlanMatrix = mapper.readValue(hm.getOrDefault("FEATURE_PLAN_MATRIX", null), typeRef);
    } catch (Exception e) {
//      featurePlanMatrix = null;
      log.error("Something went wrong with getting #featurePerPlan {}", e.getMessage());
    }

    // Turn this on when migration scripts are run and needs to access api for migration
    String migrationFlagRaw = hm.getOrDefault("MIGRATION_FLAG", "0");
    isMigrationFlatSet = StringUtils.equals(migrationFlagRaw, "1");
    // Turn this on when manual data entry of a table is required
    String dataEntryFlagRaw = hm.getOrDefault("DATA_ENTRY_FLAG", "0");
    isDataEntryFlagSet = StringUtils.equals(dataEntryFlagRaw, "1");

    log.info("Settings loaded currentSchemaVersion=[{}] onboardingTourIds=[{}] migrationFlag=[{}] dataEntryFlag=[{}] featurePlanMatrix=[{}]",
      currentSchemaVersion,
      onboardingTourIds,
      isMigrationFlatSet,
      isDataEntryFlagSet,
      featurePlanMatrix
    );
  }
}

