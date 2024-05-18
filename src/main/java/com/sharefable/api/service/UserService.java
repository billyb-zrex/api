package com.sharefable.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.UnauthorizedReason;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.OrgContext;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.NfEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    User user = userRepo.findUserByEmail(userClaimFromAuth0.email())
      .orElseGet(() -> createNewUser(userClaimFromAuth0, jwt.getSubject()));

    Long orgId = OrgContext.getCurrentOrgId();
    if (orgId != null) {
      Set<Org> orgs = user.getOrgs();
      orgs = orgs == null ? Set.of() : orgs;
      boolean isOrgValid = false;
      for (Org org : orgs) {
        if (Objects.equals(org.getId(), orgId)) {
          user.setBelongsToOrg(orgId);
          isOrgValid = true;
          break;
        }
      }
      if (!isOrgValid) {
        log.error("orgId {} is passed but user {} is not associated with org", orgId, user.getEmail());
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, objectMapper.writeValueAsString(
          Map.of("r", UnauthorizedReason.OrgSuggestedButInvalidAssociation)
        ));
      }
    }

    // If the user is deactivated any new auth attempt would mark the user as active.
    // This is not ideal but for the timebeing this would do.
    // Ideally any nonactive user has zero role based permission.
    return setUserActiveOrInactive(user, true);
  }

  public UserClaimFromAuth0 getUserClaimsFromAuth0(Jwt jwt) throws JsonProcessingException {
    Map<String, Object> claims = jwt.getClaims();
    Object userDetailsClaim = claims.get("https://identity.sharefable.com/user");
    String userDetailsClaimStr = objectMapper.writeValueAsString(userDetailsClaim);
    return objectMapper.readValue(userDetailsClaimStr, UserClaimFromAuth0.class);
  }

  User setUserActiveOrInactive(User user, Boolean isActive) {
    if (isActive == user.getActive()) return user;
    user.setActive(isActive);
    User changedUser = userRepo.save(user);
    subService.updateNoOfSeatInSubscription(user.getBelongsToOrg());
    return changedUser;
  }

  public User createNewUser(UserClaimFromAuth0 user, String authId) {
    Pair<String, Boolean> domainInf = Utils.getDomainFromEmailForRespectiveEmail(user.email());
    String emailDomain = domainInf.getValue0();
    Boolean isWorkEmail = domainInf.getValue1();

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
      .domainBlacklisted(!isWorkEmail)
      .active(true)
      .build();
    sendUserNf(user.email(), newUser.getFirstName(), newUser.getLastName());
    return userRepo.save(newUser);
  }

  public void sendUserNf(String userEmail, String firstName, String lastName) {
    Map<String, String> eventInfo = new HashMap<>();

    if (StringUtils.isBlank(userEmail) || StringUtils.isBlank(firstName)) {
      log.warn("Didn't send message as one of userEmail=[{}] or firstName=[{}] is blank", userEmail, firstName);
      return;
    }

    eventInfo.put("emailId", userEmail);
    eventInfo.put("firstName", firstName);
    if (!StringUtils.isBlank(lastName)) eventInfo.put("lastName", lastName);
    nfHookService.sendNotification(NfEvents.NEW_USER_SIGNUP, eventInfo);
  }

  public record UserClaimFromAuth0(String picture, String email, String familyName, String givenName) {
  }
}
