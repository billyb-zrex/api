package com.sharefable.api.config;

import com.sharefable.api.controller.Routes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AuthenticationConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(Routes.API_V1 + Routes.__BEHIND_LOGIN__ + "/**")
            // This should only be active for dev / staging env
            .httpBasic(Customizer.withDefaults())
            .csrf().disable();
        return http.build();
    }
}
