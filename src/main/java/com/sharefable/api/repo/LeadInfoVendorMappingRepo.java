package com.sharefable.api.repo;

import com.sharefable.api.common.LeadInfoKey;
import com.sharefable.api.entity.HouseLeadInfo;
import com.sharefable.api.entity.LeadInfoVendorMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadInfoVendorMappingRepo extends CrudRepository<LeadInfoVendorMapping, Long> {
  Optional<LeadInfoVendorMapping> findLeadInfoVendorMappingByHouseLeadInfoAndInfoKey(
    HouseLeadInfo houseLeadInfo,
    LeadInfoKey leadInfoKey
  );
}
