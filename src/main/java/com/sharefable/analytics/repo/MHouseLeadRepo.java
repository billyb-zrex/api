package com.sharefable.analytics.repo;

import com.sharefable.analytics.entity.MHouseLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MHouseLeadRepo extends JpaRepository<MHouseLead, Long> {
  List<MHouseLead> getMHouseLeadsByEntityId(Long entityId);
}
