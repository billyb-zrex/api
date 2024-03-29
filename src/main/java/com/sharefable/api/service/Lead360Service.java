package com.sharefable.api.service;

import com.sharefable.api.entity.HouseLeadInfo;
import com.sharefable.api.entity.Lead360;
import com.sharefable.api.repo.HouseLeadInfoRepo;
import com.sharefable.api.transport.req.ReqHouseLeadInfoWithInfo360;
import com.sharefable.api.transport.req.ReqLead360;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class Lead360Service {
  private final HouseLeadInfoRepo houseLeadInfoRepo;

  @Autowired
  public Lead360Service(HouseLeadInfoRepo houseLeadInfoRepo) {
    this.houseLeadInfoRepo = houseLeadInfoRepo;
  }

  @Transactional
  public void saveLead360(ReqHouseLeadInfoWithInfo360 body) {
    Optional<HouseLeadInfo> maybeHouseLeadInfo = houseLeadInfoRepo.findByOrgIdAndLeadEmailId(body.getOrgId(), body.getLeadEmailId());
    if (maybeHouseLeadInfo.isEmpty()) {
      log.warn("House lead info is not present for lead with org id [ {} ] and email [ {} ] ", body.getOrgId(), body.getLeadEmailId());
      return;
    }
    HouseLeadInfo houseLeadInfo = maybeHouseLeadInfo.get();
    Set<Lead360> houseLead360 = houseLeadInfo.getInfo360();

    for (ReqLead360 reqLead360 : body.getInfo360()) {
      Optional<Lead360> maybeTargetLead360 = houseLead360.stream()
        .filter(lead360 -> Objects.equals(reqLead360.getTourId(), lead360.getTourId()))
        .findFirst();

      Lead360 lead360;
      if (maybeTargetLead360.isEmpty()) {
        lead360 = Lead360.builder()
          .tourId(reqLead360.getTourId())
          .demoVisited(reqLead360.getDemoVisited())
          .sessionsCreated(reqLead360.getSessionsCreated())
          .lastInteractedAt(reqLead360.getLastInteractedAt())
          .timeSpentSec(reqLead360.getTimeSpentSec())
          .completionPercentage(reqLead360.getCompletionPercentage())
          .ctaClickRate(reqLead360.getCtaClickRate())
          .build();
      } else {
        lead360 = maybeTargetLead360.get();
        lead360.setDemoVisited(reqLead360.getDemoVisited());
        lead360.setSessionsCreated(reqLead360.getSessionsCreated());
        lead360.setLastInteractedAt(reqLead360.getLastInteractedAt());
        lead360.setTimeSpentSec(reqLead360.getTimeSpentSec());
        lead360.setCtaClickRate(reqLead360.getCtaClickRate());
        lead360.setCompletionPercentage(reqLead360.getCompletionPercentage());
      }
      houseLead360.add(lead360);
    }
    houseLeadInfoRepo.save(houseLeadInfo);
  }
}
