package com.sharefable.api.repo;

import com.sharefable.api.entity.TenantIntegration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantIntegrationRepo extends CrudRepository<TenantIntegration, Long> {
  List<TenantIntegration> getTenantIntegrationsByOrgIdAndIntegrationIdIn(
    Long orgId,
    List<Long> integrationIds
  );

  void deleteTenantIntegrationByOrgIdAndId(Long orgId, Long tenantIntegrationId);

  List<TenantIntegration> getTenantIntegrationsByOrgIdAndEvent(Long orgId, String event);
}
