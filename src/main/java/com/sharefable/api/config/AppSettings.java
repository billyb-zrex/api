package com.sharefable.api.config;

import com.sharefable.api.entity.Settings;
import com.sharefable.api.repo.AppSettingsRepo;
import com.sharefable.api.transport.SchemaVersion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@Getter
@Slf4j
public class AppSettings {
  @Getter(AccessLevel.NONE)
  private final AppSettingsRepo settingsRepo;

  private SchemaVersion currentSchemaVersion;
  private String onboardingTourIds;
  private boolean isMigrationFlatSet;
  private boolean isDataEntryFlagSet;

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

    // Turn this on when migration scripts are run and needs to access api for migration
    String migrationFlagRaw = hm.getOrDefault("MIGRATION_FLAG", "0");
    isMigrationFlatSet = StringUtils.equals(migrationFlagRaw, "1");
    // Turn this on when manual data entry of a table is required
    String dataEntryFlagRaw = hm.getOrDefault("DATA_ENTRY_FLAG", "0");
    isDataEntryFlagSet = StringUtils.equals(dataEntryFlagRaw, "1");

    log.info("Settings loaded currentSchemaVersion=[{}] onboardingTourIds=[{}] migrationFlag=[{}] dataEntryFlag=[{}]",
      currentSchemaVersion,
      onboardingTourIds,
      isMigrationFlatSet,
      isDataEntryFlagSet
    );
  }
}

