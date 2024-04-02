package com.sharefable.api.config;

import com.sharefable.api.entity.Settings;
import com.sharefable.api.repo.AppSettingsRepo;
import com.sharefable.api.transport.SchemaVersion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
  private String isMigrationOn;

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
    isMigrationOn = hm.getOrDefault("MIGRATION", "0");
    log.info("Settings loaded currentSchemaVersion=[{}] onboardingTourIds=[{}] migration=[{}]",
      currentSchemaVersion,
      onboardingTourIds,
      isMigrationOn
    );
  }
}

