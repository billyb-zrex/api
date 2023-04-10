package com.sharefable.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.ExcludeEmailDomain;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Qualifier("userService")
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    ObjectMapper objectMapper = new ObjectMapper();

    // used from AuthUser annotation
    public User getOrCreateUserFromJwt(Jwt jwt) throws JsonProcessingException {
        Map<String, Object> claims = jwt.getClaims();
        Object userDetailsClaim = claims.get("https://identity.sharefable.com/user");
        String userDetailsClaimStr = objectMapper.writeValueAsString(userDetailsClaim);
        UserClaimFromAuth0 userClaimFromAuth0 = objectMapper.readValue(userDetailsClaimStr, UserClaimFromAuth0.class);
        Optional<User> maybeUser = this.userRepo.findUserByEmail(userClaimFromAuth0.email());
        return maybeUser.orElseGet(() -> createNewUserIfNotExist(userClaimFromAuth0, jwt.getSubject()));
    }

    public User createNewUserIfNotExist(UserClaimFromAuth0 user, String authId) {
        String emailDomain = Utils.getDomainFromEmail(user.email());
        if (StringUtils.isBlank(emailDomain)) {
            log.error("Can't find domain from email {}", user.email());
            throw new IllegalStateException("Can't create user");
        }
        User newUser = User.builder()
            .email(user.email())
            .avatar(user.picture())
            .authId(authId)
            .firstName(StringUtils.substring(user.givenName, 0, 49))
            .lastName(StringUtils.substring(user.familyName, 0, 49))
            .domainBlacklisted(ExcludeEmailDomain.NOT_ALLOWED.contains(emailDomain))
            .build();
        return userRepo.save(newUser);
    }

    record UserClaimFromAuth0(String picture, String email, String familyName, String givenName) {
    }
}
