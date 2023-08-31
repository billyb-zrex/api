package com.sharefable.api.repo;

import com.sharefable.api.entity.Subscription;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionRepo extends CrudRepository<Subscription, Long> {
    Subscription getSubscriptionByOrgId(Long orgId);

    Subscription getSubscriptionByCbSubscriptionId(String id);
}
