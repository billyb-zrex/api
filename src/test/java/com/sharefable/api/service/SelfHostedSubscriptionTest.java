package com.sharefable.api.service;

import com.sharefable.api.common.SubscriptionManagedBy;
import com.sharefable.api.config.PaymentConfig;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.Subscription;
import com.sharefable.api.repo.*;
import com.sharefable.api.service.vendor.SlackMsgService;
import com.sharefable.api.transport.PaymentTerms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelfHostedSubscriptionTest {
  @Mock SubscriptionRepo repo;
  @Mock PaymentConfig paymentConfig;
  @Mock OrgService orgService;
  @Mock OrgRepo orgRepo;
  @Mock LogService logService;
  @Mock SlackMsgService slackMsgService;
  @Mock NfHookService nfHookService;
  @Mock EntityConfigKVRepo entityConfigKVRepo;
  @Mock QMsgService qMsgService;
  @InjectMocks SubscriptionService service;

  @Test void coreModeCreatesLocalSubscriptionForExistingOrgWithoutBilling() {
    ReflectionTestUtils.setField(service, "selfHostedCore", true);
    when(orgRepo.lockForSubscription(7L)).thenReturn(Optional.of(new Org()));
    when(repo.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));
    service.getSubscriptionWithCreditInfo(7L);
    ArgumentCaptor<Subscription> saved = ArgumentCaptor.forClass(Subscription.class);
    verify(repo).save(saved.capture());
    assertEquals(7L, saved.getValue().getOrgId());
    assertEquals(SubscriptionManagedBy.SELF_HOSTED, saved.getValue().getManagedBy());
    assertEquals(PaymentTerms.Plan.BUSINESS, saved.getValue().getPaymentPlan());
    assertEquals(com.chargebee.models.Subscription.Status.ACTIVE, saved.getValue().getStatus());
    verifyNoInteractions(paymentConfig, orgService, nfHookService, qMsgService, slackMsgService);
  }

  @Test void existingSubscriptionIsNotRecreatedOrOverwritten() {
    ReflectionTestUtils.setField(service, "selfHostedCore", true);
    when(orgRepo.lockForSubscription(7L)).thenReturn(Optional.of(new Org()));
    when(repo.getSubscriptionByOrgId(7L)).thenReturn(new Subscription());
    service.getSubscriptionWithCreditInfo(7L);
    verify(repo, never()).save(any());
    verifyNoInteractions(entityConfigKVRepo);
  }

  @Test void ordinaryModeDoesNotProvisionLocalSubscription() {
    service.getSubscriptionWithCreditInfo(7L);
    verifyNoInteractions(orgRepo);
    verify(repo, never()).save(any());
  }

  @Test void missingOrgCannotGetLocalSubscription() {
    ReflectionTestUtils.setField(service, "selfHostedCore", true);
    assertThrows(ResponseStatusException.class, () -> service.getSubscriptionWithCreditInfo(99L));
    verifyNoInteractions(repo);
  }

  @Test void coreModeSkipsSeatBillingAndRejectsCheckout() {
    ReflectionTestUtils.setField(service, "selfHostedCore", true);
    service.updateNoOfSeatInSubscription(7L);
    assertThrows(ResponseStatusException.class, () -> service.createHostedPage(null, Optional.empty()));
    assertThrows(ResponseStatusException.class, () -> service.createHostedPageForAiCredit(null));
    verifyNoInteractions(repo, orgService, paymentConfig);
  }
}

