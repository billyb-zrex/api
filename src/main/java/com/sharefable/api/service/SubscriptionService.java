package com.sharefable.api.service;

import com.chargebee.Result;
import com.chargebee.models.Customer;
import com.chargebee.models.HostedPage;
import com.sharefable.api.config.PaymentConfig;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.Subscription;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.repo.SubscriptionRepo;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.req.ReqSubscriptionInfo;
import com.sharefable.api.transport.resp.RespSubscription;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepo repo;
    private final PaymentConfig paymentConfig;
    private final OrgRepo orgRepo;
    private final UserRepo userRepo;

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

        try {
            final int numberOfMembersInOrg = userRepo.countUsersByBelongsToOrgAndActiveIsTrue(org.getId());

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
                .status(cbSubs.status())
                .orgId(org.getId())
                .cbCustomerId(customer.id())
                .build();
            repo.save(subs);

            return RespSubscription.from(subs);
        } catch (Exception e) {
            log.error("Can't create account subscription for user {}.  Error: {}", user.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while creating subscription");
        }
    }

    public RespSubscription updateSubscription(ReqSubscriptionInfo info, User user) {
        if (user.getBelongsToOrg() == null) return null;
        Subscription subs = repo.getSubscriptionByOrgId(user.getBelongsToOrg());
        String planId = paymentConfig.getPlanId(info.pricingPlan(), info.pricingInterval());
        final int numberOfMembersInOrg = userRepo.countUsersByBelongsToOrgAndActiveIsTrue(user.getBelongsToOrg());

        try {
            com.chargebee.models.Subscription.updateForItems(subs.getCbSubscriptionId())
                .subscriptionItemItemPriceId(0, planId)
                .subscriptionItemQuantity(0, numberOfMembersInOrg)
                .request();

            subs.setPaymentPlan(info.pricingPlan());
            subs.setPaymentInterval(info.pricingInterval());
            subs.setPaymentPlanId(planId);
            Subscription updatedSub = repo.save(subs);
            return RespSubscription.from(updatedSub);
        } catch (Exception e) {
            log.error("Can't update {} subscription plan to {} with error {}", subs.getCbSubscriptionId(), planId, e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while updating subscription");
        }
    }

    @Async
    void updateNoOfSeatInSubscription(Long orgId) {
        final int newSeatQuantity = userRepo.countUsersByBelongsToOrgAndActiveIsTrue(orgId);
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

    public String createHostedPage(User user) {
        Subscription subs = repo.getSubscriptionByOrgId(user.getBelongsToOrg());
        if (subs == null) return null;
        String subId = subs.getCbSubscriptionId();
        try {
            final int numberOfMembersInOrg = userRepo.countUsersByBelongsToOrgAndActiveIsTrue(user.getBelongsToOrg());
            Result result = HostedPage.checkoutExistingForItems()
                .subscriptionId(subId)
                .subscriptionItemItemPriceId(0, subs.getPaymentPlanId())
                .subscriptionItemQuantity(0, numberOfMembersInOrg)
                .request();
            HostedPage hostedPage = result.hostedPage();
            return hostedPage.toJson();
        } catch (Exception e) {
            log.error("Can't complete payment request for {} with error {}", user.getEmail(), e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please try again");
        }
    }

    public void resyncSubscription(com.chargebee.models.Subscription cbSubs) {
        Subscription subs = repo.getSubscriptionByCbSubscriptionId(cbSubs.id());
        if (subs == null) return;

        subs.setTrialEndsOn(cbSubs.trialEnd());
        subs.setTrialStartedOn(cbSubs.trialStart());
        // WARN only one subscription item per org is allowed hence 0
        com.chargebee.models.Subscription.SubscriptionItem subscriptionItem = cbSubs.subscriptionItems().get(0);
        subs.setPaymentPlanId(subscriptionItem.itemPriceId());
        subs.setStatus(cbSubs.status());
        repo.save(subs);
    }
}
