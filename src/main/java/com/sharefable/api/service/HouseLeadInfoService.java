package com.sharefable.api.service;

import com.sharefable.api.entity.HouseLeadInfo;
import com.sharefable.api.repo.HouseLeadInfoRepo;
import com.sharefable.api.transport.resp.RespHouseLeadInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class HouseLeadInfoService {
  private final HouseLeadInfoRepo houseLeadInfoRepo;

  @Autowired
  public HouseLeadInfoService(HouseLeadInfoRepo houseLeadInfoRepo) {
    this.houseLeadInfoRepo = houseLeadInfoRepo;
  }

  @Transactional
  public RespHouseLeadInfo getHouseLeadInfo(Long orgId, String email) {
    Optional<HouseLeadInfo> maybeHouseLeadInfo = houseLeadInfoRepo.findByOrgIdAndLeadEmailId(orgId, email);
    if (maybeHouseLeadInfo.isEmpty()) {
      log.warn("House lead is not present for this org id {} and email {} ", orgId, email);
      return RespHouseLeadInfo.Empty();
    }
    return RespHouseLeadInfo.from(maybeHouseLeadInfo.get());
  }
}
