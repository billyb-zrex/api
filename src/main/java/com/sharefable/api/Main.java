package com.sharefable.api;

import com.sharefable.api.config.AppSettings;
import com.sharefable.api.entity.Settings;
import com.sharefable.api.repo.AppSettingsRepo;
import com.sharefable.api.transport.SchemaVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;

@SpringBootApplication
@Slf4j
@EnableAsync
public class Main {
    private final AppSettingsRepo settingsRepo;

    @Autowired
    public Main(AppSettingsRepo settingsRepo) {
        this.settingsRepo = settingsRepo;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(60))
            .build();
    }

    @Bean
    public AppSettings appSettings() {
        Iterable<Settings> settings = settingsRepo.findAll();
        HashMap<String, String> hm = new HashMap<>();
        for (Settings setting : settings) {
            hm.put(setting.getK(), setting.getV());
        }
        return new AppSettings(
            SchemaVersion.of(hm.get("CURRENT_SCHEMA_VERSION")), hm.getOrDefault("ONBOARDING_TOUR_IDS", "")
        );
    }
}
