package com.sharefable.api.service;

import com.chargebee.Result;
import com.chargebee.models.Customer;
import com.chargebee.models.HostedPage;
import com.chargebee.org.json.JSONObject;
import com.sharefable.api.common.SubscriptionManagedBy;
import com.sharefable.api.config.PaymentConfig;
import com.sharefable.api.entity.Log;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.Subscription;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.repo.SubscriptionRepo;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.NfEvents;
import com.sharefable.api.transport.PaymentTerms;
import com.sharefable.api.transport.req.ReqSubscriptionInfo;
import com.sharefable.api.transport.resp.RespSubsValidation;
import com.sharefable.api.transport.resp.RespSubscription;
import io.sentry.Sentry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
  private final SubscriptionRepo repo;
  private final PaymentConfig paymentConfig;
  private final OrgRepo orgRepo;
  private final UserRepo userRepo;
  private final LogService logService;
  private final NfHookService nfHookService;

  public RespSubscription getSubscriptionForUser(User user) {
    Long orgId = user.getBelongsToOrg();
    if (orgId == null) return null;
    Optional<Org> maybeOrg = orgRepo.findById(orgId);
    if (maybeOrg.isEmpty()) return null;
    Subscription subs = repo.getSubscriptionByOrgId(orgId);
    if (subs == null) return null;
    return RespSubscription.from(subs);
  }

  @Transactional
  public RespSubscription newSubscription(ReqSubscriptionInfo info, User user) {
    if (user.getBelongsToOrg() == null) return null;
    Optional<Org> maybeOrg = orgRepo.findById(user.getBelongsToOrg());
    if (maybeOrg.isEmpty()) return null;
    Org org = maybeOrg.get();

    if (info.pricingInterval() == PaymentTerms.Interval.LIFETIME && !StringUtils.isBlank(info.lifetimeLicense())) {
      // process lifetime license from appsumo, in this case we don't process saas pricing at all (chargebee)

      // appsumo sends a webhook on license activate / deactivate / purchase / upgrade / downgrade
      // when it does that, we save the data to logs table
      // When user logs in after that we fetch the license information and populate the subscription table
      // There might be a very little edge case when user logs in but the license information is not passed via webhook
      // or may be the webhook failed. In that case we use status Future

      Optional<Log> license = logService.getLicenseFromLog(info.lifetimeLicense());
      Subscription.SubscriptionBuilder<?, ?> builder = Subscription.builder()
        .paymentInterval(PaymentTerms.Interval.LIFETIME)
        .cbSubscriptionId(info.lifetimeLicense())
        .trialEndsOn(Timestamp.from(Instant.ofEpochMilli(1893436200000L))) // static value 2030
        .trialStartedOn(Timestamp.from(Instant.ofEpochMilli(1893436200000L))) // static value 2030
        .cbCustomerId("")
        .managedBy(SubscriptionManagedBy.APPSUMO)
        .orgId(org.getId());

      boolean isDeactivated = false;
      if (license.isPresent()) {
        Object logLine = license.get().getLogLine();
        Map<String, Object> licenseInfo = (Map<String, Object>) logLine;

        String event = (String) licenseInfo.get("event");
        if (StringUtils.equalsIgnoreCase(event, "deactivate")) {
          isDeactivated = true;
        } else {
          Object rawTier = licenseInfo.get("tier");
          int tier = 1;
          if (rawTier != null) {
            tier = (Integer) rawTier;
          }
          PaymentTerms.Plan plan = switch (tier) {
            case 1 -> PaymentTerms.Plan.LIFETIME_TIER1;
            case 2 -> PaymentTerms.Plan.LIFETIME_TIER2;
            // case 3
            default -> PaymentTerms.Plan.LIFETIME_TIER3;
          };
          builder
            .paymentPlanId(paymentConfig.getPlanId(plan, info.pricingInterval()))
            .paymentPlan(plan)
            .status(com.chargebee.models.Subscription.Status.ACTIVE);
        }
      } else {
        builder
          .paymentPlanId(paymentConfig.getPlanId(info.pricingPlan(), info.pricingInterval()))
          .paymentPlan(info.pricingPlan())
          .status(com.chargebee.models.Subscription.Status.FUTURE);
      }

      // If license is deactivated start saas plan without breaking the flow
      // if license is active then create appsumo subscription activation
      if (!isDeactivated) {
        Subscription subs = builder.build();
        repo.save(subs);

        sendUserDetailsWithPlans(user, subs);

        return RespSubscription.from(subs);
      }
    }

    try {
      final int numberOfMembersInOrg = userRepo.countActiveUsersByBelongsToOrgWhoAreNotFableSupport(org.getId());

      // Create a customer object in chargebee
      Result cusomerResult = Customer.create()
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .company(org.getDisplayName())
        .request();
      Customer customer = cusomerResult.customer();

      // Create a subscription object in chargebee
      String planId = paymentConfig.getPlanId(info.pricingPlan(), info.pricingInterval());
      Result subsResult = com.chargebee.models.Subscription.createWithItems(customer.id())
        .subscriptionItemItemPriceId(0, planId)
        .subscriptionItemQuantity(0, numberOfMembersInOrg)
        .request();
      com.chargebee.models.Subscription cbSubs = subsResult.subscription();

      Subscription subs = Subscription.builder()
        .paymentPlanId(planId)
        .paymentPlan(info.pricingPlan())
        .paymentInterval(info.pricingInterval())
        .cbSubscriptionId(cbSubs.id())
        .trialEndsOn(cbSubs.trialEnd())
        .trialStartedOn(cbSubs.trialStart())
        .managedBy(SubscriptionManagedBy.CHARGEBEE)
        .status(cbSubs.status())
        .orgId(org.getId())
        .cbCustomerId(customer.id())
        .build();

      repo.save(subs);

      sendUserDetailsWithPlans(user, subs);

      return RespSubscription.from(subs);
    } catch (Exception e) {
      log.error("Can't create account subscription for user {}.  Error: {}", user.getEmail(), e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while creating subscription");
    }
  }

  @Transactional
  public RespSubscription updateSubscription(ReqSubscriptionInfo info, Long orgId) {
    Subscription subs = repo.getSubscriptionByOrgId(orgId);
    Set<User> users = userRepo.getUsersByBelongsToOrgAndActiveIsTrue(orgId);
    Optional<User> user = users.stream().findFirst();

    // If subscription purchased, upgraded, downgraded update it
    // if it's deactivated start a new chargebee subscription
    // delete existing chargebee subscription

    String planId = paymentConfig.getPlanId(info.pricingPlan(), info.pricingInterval());
    final int numberOfMembersInOrg = userRepo.countActiveUsersByBelongsToOrgWhoAreNotFableSupport(orgId);

    boolean isLifetimeSubscription = info.pricingInterval() == PaymentTerms.Interval.LIFETIME &&
      !StringUtils.isBlank(info.lifetimeLicense());
    Map<String, Object> licenseInfo;
    if (isLifetimeSubscription) {
      Optional<Log> license = logService.getLicenseFromLog(info.lifetimeLicense());
      if (license.isPresent()) {
        licenseInfo = (Map<String, Object>) license.get().getLogLine();
        String event = (String) licenseInfo.get("event");
        if (StringUtils.equalsIgnoreCase(event, "deactivate")) {
          // If the license got deactivated then make lifetime subscription as false
          isLifetimeSubscription = false;
        }
      } else {
        throw new RuntimeException("License " + info.lifetimeLicense() + " should be present for user");
      }
    }

    try {
      if (isLifetimeSubscription) {
        if (subs.getManagedBy() == SubscriptionManagedBy.CHARGEBEE || subs.getManagedBy() == SubscriptionManagedBy.APPSUMO) {
          // If user is choosing saas -> lifetime
          // if upgrading / downgrading lifetime
          repo.delete(subs);
          return newSubscription(info, user.get());
        } else {
          throw new RuntimeException("Unknown subscription manager " + subs.getManagedBy());
        }
      } else {
        if (subs.getManagedBy() == SubscriptionManagedBy.APPSUMO) {
          // if switching from appsumo to saas. this happens when someone activates saas plan from app sumo plan
          // 1. delete app sumo subscription
          // 2. start a new saas subscription
          repo.delete(subs);
          return user.map(value -> newSubscription(
            new ReqSubscriptionInfo(
              PaymentTerms.Plan.SOLO,
              PaymentTerms.Interval.MONTHLY,
              null
            ),
            value
          )).orElse(null);
        } else if (subs.getManagedBy() == SubscriptionManagedBy.CHARGEBEE) {
          // saas upgrade
          try {
            com.chargebee.models.Subscription.updateForItems(subs.getCbSubscriptionId())
              .subscriptionItemItemPriceId(0, planId)
              .request();
          } catch (Exception e) {
            // this is just a fail safe mechanism as sometimes chargebee throws error if numberOfMembersInOrg is set
            // or this is created for the case where solo plan requires numberOfMember to have not set but rest of the plan
            // requires numberofMembers to be set
            // TODO Fix this
            log.error("CB param mismatch. Trying another way", e);
            com.chargebee.models.Subscription.updateForItems(subs.getCbSubscriptionId())
              .subscriptionItemItemPriceId(0, planId)
              .subscriptionItemQuantity(0, numberOfMembersInOrg)
              .request();
          }

          subs.setPaymentPlan(info.pricingPlan());
          subs.setPaymentInterval(info.pricingInterval());
          subs.setPaymentPlanId(planId);
          Subscription updatedSub = repo.save(subs);
          return RespSubscription.from(updatedSub);
        } else {
          throw new RuntimeException("Unknown subscription manager " + subs.getManagedBy());
        }
      }
    } catch (Exception e) {
      log.error("Can't update {} subscription plan to {}", subs.getCbSubscriptionId(), planId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while updating subscription");
    }
  }

  @Transactional
  public RespSubscription updateSubscriptionForUser(ReqSubscriptionInfo info, User user) {
    if (user.getBelongsToOrg() == null) return null;
    return updateSubscription(info, user.getBelongsToOrg());
  }

  public Subscription getSubscriptionById(String subId) {
    return repo.getSubscriptionByCbSubscriptionId(subId);
  }

  @Async
  void updateNoOfSeatInSubscription(Long orgId) {
    final int newSeatQuantity = userRepo.countActiveUsersByBelongsToOrgWhoAreNotFableSupport(orgId);
    Subscription subs = repo.getSubscriptionByOrgId(orgId);
    String subsId = subs.getCbSubscriptionId();
    if (StringUtils.isBlank(subsId)) {
      log.error("Seat change requested but subscription id not found for org {}", orgId);
      return;
    }

    try {
      com.chargebee.models.Subscription.updateForItems(subsId)
        .subscriptionItemItemPriceId(0, subs.getPaymentPlanId())
        .subscriptionItemQuantity(0, newSeatQuantity)
        .request();
    } catch (Exception e) {
      log.error("Can't update {} subscription quantity to {}", subsId, newSeatQuantity);
      throw new RuntimeException(e);
    }
  }

  public void downgradeSubscriptionToFreePlan(com.chargebee.models.Subscription cbSub) {
    try {
      com.chargebee.models.Subscription.updateForItems(cbSub.id())
        .subscriptionItemItemPriceId(0, paymentConfig.getPlanId(PaymentTerms.Plan.SOLO, PaymentTerms.Interval.YEARLY))
        .request();
    } catch (Exception e) {
      log.error("Can't downgrade subscription", e);
      throw new RuntimeException(e);
    }
  }

  public String createHostedPage(User user, Optional<ReqSubscriptionInfo> info) {
    Subscription subs = repo.getSubscriptionByOrgId(user.getBelongsToOrg());
    if (subs == null) return null;

    String subId = subs.getCbSubscriptionId();
    String paymentPlanId = info.isPresent() ? paymentConfig.getPlanId(info.get().pricingPlan(), info.get().pricingInterval()) : subs.getPaymentPlanId();
    try {
      final int numberOfMembersInOrg = userRepo.countActiveUsersByBelongsToOrgWhoAreNotFableSupport(user.getBelongsToOrg());
      Result result = HostedPage.checkoutExistingForItems()
        .subscriptionId(subId)
        .subscriptionItemItemPriceId(0, paymentPlanId)
        .subscriptionItemQuantity(0, numberOfMembersInOrg)
        .request();
      HostedPage hostedPage = result.hostedPage();
      return hostedPage.toJson();
    } catch (Exception e) {
      log.error("Can't complete payment request for {} ", user.getEmail(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please try again");
    }
  }

  public void resyncSubscription(com.chargebee.models.Subscription cbSubs) {
    Subscription subs = repo.getSubscriptionByCbSubscriptionId(cbSubs.id());
    if (subs == null) return;
    if (subs.getManagedBy() != SubscriptionManagedBy.CHARGEBEE) return;

    subs.setTrialEndsOn(cbSubs.trialEnd());
    subs.setTrialStartedOn(cbSubs.trialStart());
    // WARN only one subscription item per org is allowed hence 0
    com.chargebee.models.Subscription.SubscriptionItem subscriptionItem = cbSubs.subscriptionItems().get(0);
    subs.setPaymentPlanId(subscriptionItem.itemPriceId());
    subs.setStatus(cbSubs.status());
    Pair<PaymentTerms.Plan, PaymentTerms.Interval> items = paymentConfig.getPlanItemsById(subscriptionItem.itemPriceId());
    if (items != null) {
      subs.setPaymentPlan(items.getValue0());
      subs.setPaymentInterval(items.getValue1());
    }
    repo.save(subs);
  }

  public RespSubsValidation validate(Long orgId) {
    RespSubsValidation validationResult = new RespSubsValidation();
    try {
      Subscription subs = repo.getSubscriptionByOrgId(orgId);
      if (subs.getManagedBy() == SubscriptionManagedBy.CHARGEBEE) {
        Result currentSub = com.chargebee.models.Subscription.retrieve(subs.getCbSubscriptionId()).request();
        JSONObject subJson = currentSub.jsonResponse();
        JSONObject customer = (JSONObject) subJson.get("customer");
        String cardStatus = (String) customer.get("card_status");
        if (StringUtils.equalsIgnoreCase(cardStatus, "no_card")) {
          validationResult.setCardPresent(false);
        }
        validationResult.setCardPresent(true);
      } else {
        validationResult.setCardPresent(true);
      }
    } catch (Exception e) {
      validationResult.setCardPresent(true);
      log.error("Error while getting subscription result", e);
      Sentry.captureException(e);
    }
    return validationResult;
  }

  public void sendUserDetailsWithPlans(User user, Subscription subs) {
    Map<String, String> payload = new HashMap<>();
    payload.put("email", user.getEmail());
    payload.put("firstName", user.getFirstName());
    if (!StringUtils.isBlank(user.getLastName())) payload.put("lastName", user.getLastName());
    payload.put("subs", subs.getPaymentPlan().name());
    nfHookService.sendNotification(NfEvents.NEW_USER_SIGNUP_WITH_SUBS, payload);
  }
}
