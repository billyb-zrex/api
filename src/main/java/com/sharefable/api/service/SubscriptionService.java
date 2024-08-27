package com.sharefable.api.service;

import com.chargebee.Result;
import com.chargebee.models.Customer;
import com.chargebee.models.Event;
import com.chargebee.models.HostedPage;
import com.chargebee.models.Invoice;
import com.chargebee.models.enums.EventType;
import com.chargebee.org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.*;
import com.sharefable.api.config.PaymentConfig;
import com.sharefable.api.entity.*;
import com.sharefable.api.repo.EntityConfigKVRepo;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.repo.SubscriptionRepo;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.service.vendor.SlackMsgService;
import com.sharefable.api.transport.NfEvents;
import com.sharefable.api.transport.PaymentTerms;
import com.sharefable.api.transport.req.ReqSubscriptionInfo;
import com.sharefable.api.transport.resp.RespSubsValidation;
import com.sharefable.api.transport.resp.RespSubscription;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String FABLE_GIVEN_CREDIT = "FABLE_GIVEN_CREDIT";
  private static final String TOPUP_CREDIT = "TOPUP_CREDIT";
  private final SubscriptionRepo repo;
  private final PaymentConfig paymentConfig;
  private final OrgRepo orgRepo;
  private final UserRepo userRepo;
  private final LogService logService;
  private final SlackMsgService slackMsgService;
  private final NfHookService nfHookService;
  private final EntityConfigKVRepo entityConfigKVRepo;

  @Transactional
  public RespSubscription getSubscriptionForUser(User user) {
    Long orgId = user.getBelongsToOrg();
    if (orgId == null) return null;
    Optional<Org> maybeOrg = orgRepo.findById(orgId);
    if (maybeOrg.isEmpty()) return null;

    Pair<Subscription, List<EntityConfigKV>> subscriptionAndCredits = getSubscriptionWithCreditInfo(orgId);
    Subscription subs = subscriptionAndCredits.getValue0();
    List<EntityConfigKV> entityConfigKVS = subscriptionAndCredits.getValue1();

    if (subs == null) return null;
    return RespSubscription.from(subs, entityConfigKVS);
  }

  @Transactional
  public RespSubscription newSubscription(ReqSubscriptionInfo info, User user) {
    if (user.getBelongsToOrg() == null) return null;
    Optional<Org> maybeOrg = orgRepo.findById(user.getBelongsToOrg());
    if (maybeOrg.isEmpty()) return null;
    Org org = maybeOrg.get();

    String planId = paymentConfig.getPlanId(info.pricingPlan(), info.pricingInterval());
    List<EntityConfigKV> entityConfigKVS = setCreditsForOrg(planId, org.getId());

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
            case 2 -> PaymentTerms.Plan.LIFETIME_TIER2;
            case 3 -> PaymentTerms.Plan.LIFETIME_TIER3;
            case 4 -> PaymentTerms.Plan.LIFETIME_TIER4;
            case 5 -> PaymentTerms.Plan.LIFETIME_TIER5;
            // case 1
            default -> PaymentTerms.Plan.LIFETIME_TIER1;
          };
          planId = paymentConfig.getPlanId(plan, info.pricingInterval());
          entityConfigKVS = setCreditsForOrg(planId, org.getId());

          builder
            .paymentPlanId(planId)
            .paymentPlan(plan)
            .status(com.chargebee.models.Subscription.Status.ACTIVE);
        }
      } else {
        builder
          .paymentPlanId(planId)
          .paymentPlan(info.pricingPlan())
          .status(com.chargebee.models.Subscription.Status.FUTURE);
      }

      // If license is deactivated start saas plan without breaking the flow
      // if license is active then create appsumo subscription activation
      if (!isDeactivated) {
        Subscription subs = builder.build();
        repo.save(subs);

        sendUserDetailsWithPlans(user, subs);

        return RespSubscription.from(subs, entityConfigKVS);
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

      return RespSubscription.from(subs, entityConfigKVS);
    } catch (Exception e) {
      log.error("Can't create account subscription for user {}.  Error: {}", user.getEmail(), e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while creating subscription");
    }
  }

  @Transactional
  public RespSubscription updateSubscription(ReqSubscriptionInfo info, Long orgId) {
    Pair<Subscription, List<EntityConfigKV>> subscriptionWithCreditInfo = getSubscriptionWithCreditInfo(orgId);

    Subscription subs = subscriptionWithCreditInfo.getValue0();
    List<EntityConfigKV> entityConfigKVS = subscriptionWithCreditInfo.getValue1();

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

        try {
          slackMsgService.sendAppSumoSubsMsgs(
            Map.of(
              "__st", "ASSOCIATION",
              "email", user.isPresent() ? user.get().getEmail() : "na",
              "EVENT", event,
              "licenseInfo", licenseInfo
            )
          );
        } catch (Exception e) {
          log.error("Couldn't send message to slack", e);
          Sentry.captureException(e);
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
          return RespSubscription.from(updatedSub, entityConfigKVS);
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

  @Transactional
  public String createHostedPageForAiCredit(User user) {
    Subscription subs = repo.getSubscriptionByOrgId(user.getBelongsToOrg());
    Org org = orgRepo.findById(subs.getOrgId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    String custId = subs.getCbCustomerId();

    if (subs.getManagedBy().equals(SubscriptionManagedBy.APPSUMO)) {
      if (StringUtils.isBlank(custId)) {
        try {
          Result cusomerResult = Customer.create()
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .company(org.getDisplayName())
            .request();
          Customer customer = cusomerResult.customer();
          custId = customer.id();

          subs.setCbCustomerId(customer.id());
          repo.save(subs);
        } catch (Exception e) {
          log.error("Something went wrong while trying to create customer for appsumo customer with org {}", org.getId());
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while trying to create customer for appsumo customer with org " + org.getId());
        }
      }
    }

    try {
      Result result = HostedPage.checkoutOneTimeForItems()
        .customerId(custId)
        .itemPriceItemPriceId(0, paymentConfig.getAiChargeId())
        .itemPriceQuantity(0, PaymentConfig.PLAN_DEFAULT_AI_CREDIT.get(subs.getPaymentPlanId()))
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

  @Transactional
  public void updateCredit(Event event) {
    try {
      Event.Content content = event.content();
      EventType eventType = event.eventType();
      boolean isPaymentForAI = checkIfPaymentForAI(content);
      String customerId = content.customer().id();

      if (isPaymentForAI && eventType.equals(EventType.PAYMENT_SUCCEEDED)) {
        Integer quantity = content.invoice().lineItems().get(0).quantity();
        Subscription subs = repo.getSubscriptionByCbCustomerId(customerId);

        List<EntityConfigKV> entityConfigKVS = entityConfigKVRepo.findEntityConfigKVSByEntityTypeAndEntityIdAndConfigType(ConfigEntityType.Org, subs.getOrgId(), EntityConfigConfigType.AI_CREDIT);

        Map<String, EntityConfigKV> entityConfigKVMap = entityConfigKVS.stream()
          .collect(Collectors.toMap(EntityConfigKV::getConfigKey, Function.identity()));

        EntityConfigKV entityConfigKV = entityConfigKVMap.getOrDefault(TOPUP_CREDIT, EntityConfigKV.builder()
          .entityId(subs.getOrgId())
          .entityType(ConfigEntityType.Org)
          .configType(EntityConfigConfigType.AI_CREDIT)
          .configKey(TOPUP_CREDIT)
          .configVal(new CreditInfo(quantity, Utils.getCurrentUtcTimestamp()))
          .build());

        CreditInfo creditInfo = mapper.convertValue(entityConfigKV.getConfigVal(), CreditInfo.class);

        creditInfo.setValue(creditInfo.getValue() + quantity);
        creditInfo.setUpdatedAt(Utils.getCurrentUtcTimestamp());

        entityConfigKV.setConfigVal(creditInfo);
        entityConfigKVRepo.save(entityConfigKV);

      } else if (isPaymentForAI && eventType.equals(EventType.PAYMENT_FAILED)) {
        log.error("The payment failed for AI credit for the customer {}", customerId);
        Sentry.captureMessage("The payment failed for AI credit for the customer " + customerId, SentryLevel.WARNING);
      }
    } catch (Exception e) {
      log.error("Something went wrong while updating credits for organisation ", e);
      Sentry.captureException(e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while updating credits for organisation " + e);
    }
  }

  private boolean checkIfPaymentForAI(Event.Content content) {
    Invoice.LineItem.EntityType entityType = content.invoice().lineItems().get(0).entityType();
    String entityId = content.invoice().lineItems().get(0).entityId();
    return entityType.equals(Invoice.LineItem.EntityType.CHARGE_ITEM_PRICE)
      && entityId.equals(paymentConfig.getAiChargeId());
  }

  @Transactional
  private List<EntityConfigKV> setCreditsForOrg(String planId, Long orgId) {

    List<EntityConfigKV> entityConfigKVS = new ArrayList<>();

    try {
      EntityConfigKV fableGivenCredit = EntityConfigKV.builder()
        .entityId(orgId)
        .entityType(ConfigEntityType.Org)
        .configType(EntityConfigConfigType.AI_CREDIT)
        .configKey(FABLE_GIVEN_CREDIT)
        .configVal(new CreditInfo(PaymentConfig.PLAN_DEFAULT_AI_CREDIT.get(planId), Utils.getCurrentUtcTimestamp()))
        .build();

      EntityConfigKV topUpCredit = EntityConfigKV.builder()
        .entityId(orgId)
        .entityType(ConfigEntityType.Org)
        .configType(EntityConfigConfigType.AI_CREDIT)
        .configKey(TOPUP_CREDIT)
        .configVal(new CreditInfo(0, Utils.getCurrentUtcTimestamp()))
        .build();

      entityConfigKVS.add(fableGivenCredit);
      entityConfigKVS.add(topUpCredit);

      entityConfigKVRepo.saveAll(entityConfigKVS);
      return entityConfigKVS;
    } catch (Exception e) {
      log.warn("Something went wrong while trying to create CREDIT config for Org {}", orgId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while trying to create CREDIT config for Org " + orgId);
    }
  }

  @Transactional
  protected Pair<Subscription, List<EntityConfigKV>> getSubscriptionWithCreditInfo(Long orgId) {
    List<SubscriptionWithCredit> subscriptionWithCredits = repo.getSubscriptionAndCredit(orgId, ConfigEntityType.Org, EntityConfigConfigType.AI_CREDIT);

    Subscription subscription = subscriptionWithCredits.stream()
      .map(SubscriptionWithCredit::getSubscription)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null);

    List<EntityConfigKV> entityConfigKVS = subscriptionWithCredits.stream()
      .map(SubscriptionWithCredit::getEntityConfigKV)
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());

    return Pair.with(subscription, entityConfigKVS);
  }
}
