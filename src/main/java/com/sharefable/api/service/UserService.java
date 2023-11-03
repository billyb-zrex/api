package com.sharefable.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.ExcludeEmailDomain;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.NfEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Qualifier("userService")
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final NfHookService nfHookService;
    private final SubscriptionService subService;

    ObjectMapper objectMapper = new ObjectMapper();

    // used from AuthUser annotation
    public User getOrCreateUserFromJwt(Jwt jwt) throws JsonProcessingException {
        UserClaimFromAuth0 userClaimFromAuth0 = getUserClaimsFromAuth0(jwt);
        Optional<User> maybeUser = this.userRepo.findUserByEmail(userClaimFromAuth0.email());
        User user = maybeUser.orElseGet(() -> createNewUserIfNotExist(userClaimFromAuth0, jwt.getSubject()));
        // If the user is deactivated any new auth attempt would mark the user as active.
        // This is not ideal but for the timebeing this would do.
        // Ideally any nonactive user has zero role based permission.
        return setUserActiveOrInactive(user, true);
    }

    public UserClaimFromAuth0 getUserClaimsFromAuth0(Jwt jwt) throws JsonProcessingException {
        Map<String, Object> claims = jwt.getClaims();
        Object userDetailsClaim = claims.get("https://identity.sharefable.com/user");
        String userDetailsClaimStr = objectMapper.writeValueAsString(userDetailsClaim);
        UserClaimFromAuth0 userClaimFromAuth0 = objectMapper.readValue(userDetailsClaimStr, UserClaimFromAuth0.class);
        return userClaimFromAuth0;
    }

    User setUserActiveOrInactive(User user, Boolean isActive) {
        if (isActive == user.getActive()) return user;
        user.setActive(isActive);
        User changedUser = userRepo.save(user);
        subService.updateNoOfSeatInSubscription(user.getBelongsToOrg());
        return changedUser;
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
            .active(true)
            .build();
        sendNotificationToSlack(user.email());
        return userRepo.save(newUser);
    }

    private void sendNotificationToSlack(String userEmail) {
        Map<String, String> eventInfo = new HashMap<>();
        eventInfo.put("emailId", userEmail);
        nfHookService.sendNotification(NfEvents.NEW_USER_SIGNUP, eventInfo);
    }

    public record UserClaimFromAuth0(String picture, String email, String familyName, String givenName) {
    }
}
