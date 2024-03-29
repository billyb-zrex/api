package com.sharefable.api.repo;

import com.sharefable.api.entity.HouseLeadInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseLeadInfoRepo extends CrudRepository<HouseLeadInfo, Long> {
  Optional<HouseLeadInfo> findByOrgIdAndLeadEmailId(Long orgId, String leadEmailId);
}
