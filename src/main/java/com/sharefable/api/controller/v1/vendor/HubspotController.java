package com.sharefable.api.controller.v1.vendor;

import com.sharefable.api.common.LeadInfoKey;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.vendor.HubspotConfig;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.AnalyticsUserAidMapping;
import com.sharefable.api.entity.Lead360;
import com.sharefable.api.entity.LeadInfoVendorMapping;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.repo.AnalyticsUserAidMappingRepo;
import com.sharefable.api.repo.LeadInfoVendorMappingRepo;
import com.sharefable.api.repo.TourRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class HubspotController {
  private final AppSettings appSettings;
  private final HubspotConfig hubspotConfig;
  private final LeadInfoVendorMappingRepo leadInfoVendorMappingRepo;
  private final AnalyticsUserAidMappingRepo analyticsUserAidMappingRepo;
  private final TourRepo tourRepo;

  private boolean isValidHubspotRequest(String signature, HttpServletRequest request) {
    String webhookUrl = appSettings.getPublicEndpoint() + request.getRequestURI() + "?" + request.getQueryString();
    String text = DigestUtils.sha256Hex(StringUtils.join(List.of(
      hubspotConfig.getClientSecret(),
      request.getMethod(),
      webhookUrl
    ), ""));
    boolean isValid = StringUtils.equals(text, signature);
    if (!isValid) {
      log.error("Hubspot request not valid method=[{}] webhook=[{}] gen_txt=[{}] sig=[{}]",
        request.getMethod(),
        webhookUrl,
        text,
        signature);
    }

    return isValid;
  }

  @RequestMapping(value = Routes.HUBSPOT_DATA_FETCH_URL_HOOK, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Lead360Data getLead360Data(
    @RequestParam(name = "associatedObjectId") String hubspotContactId,
    @RequestHeader("X-HubSpot-Signature") String hsSig,
    HttpServletRequest request
  ) {
    if (!isValidHubspotRequest(hsSig, request)) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Not verified");
    }

    log.info("Getting lead data for hubspotContactId {}", hubspotContactId);

    Optional<LeadInfoVendorMapping> maybeVendorLeadInfo = leadInfoVendorMappingRepo.getLeadInfoVendorMappingByInfoKeyAndInfoValue(LeadInfoKey.HUBSPOT_CONTACT_ID, hubspotContactId);
    if (maybeVendorLeadInfo.isEmpty()) {
      return new Lead360Data(new LeadResult[]{});
    }


    LeadInfoVendorMapping leadInfoVendorMapping = maybeVendorLeadInfo.get();
    Set<Lead360> lead360s = leadInfoVendorMapping.getHouseLeadInfo().getInfo360();
    List<Long> tourIds = lead360s.stream().map(Lead360::getTourId).toList();

    List<Tour> allTours = tourRepo.findAllByIdIn(tourIds);
    Map<Long, Tour> tourMap = new HashMap<>();
    for (Tour tour : allTours) {
      tourMap.put(tour.getId(), tour);
    }

    LeadResult[] leadResults = lead360s.stream().map(lead360 -> {
      Tour tour = tourMap.get(lead360.getTourId());
      if (tour == null) {
        log.error("tour is present in lead360 but not found in tours. id={}", lead360.getTourId());
        return null;
      }

      List<AnalyticsUserAidMapping> aidMapping = analyticsUserAidMappingRepo.getAnalyticsUserAidMappingsByTourIdAndEmailOrderByUpdatedAtDesc(tour.getId(), leadInfoVendorMapping.getHouseLeadInfo().getLeadEmailId());
      AnalyticsUserAidMapping mapping = aidMapping.get(0);

      if (mapping == null) {
        log.error("no aid mapping is found for tourId [{}] and emailId[{}] ", tour.getId(), leadInfoVendorMapping.getHouseLeadInfo().getLeadEmailId());
        return null;
      }

      return new LeadResult(
        lead360.getId(),
        tour.getDisplayName(),
        String.format("https://app.sharefable.com/demo/%s", tour.getRid()),
        lead360.getCompletionPercentage(),
        lead360.getCtaClickRate() == 1 ? "Yes" : "No",
        lead360.getLastInteractedAt().getTime(),
        lead360.getTimeSpentSec(),
        String.format("https://app.sharefable.com/a/demo/%s/leads#%s", tour.getRid(), mapping.getAid())
      );
    }).filter(Objects::nonNull).toArray(LeadResult[]::new);

    return new Lead360Data(leadResults);
  }

  public record Lead360Data(
    LeadResult[] results
  ) {
  }

  public record LeadResult(
    Long objectId,
    String title,
    String link,
    Integer completionPercentage,
    String ctaClicked,
    Long lastSeenAt,
    Integer timeSpent,
    String activityLink
  ) {
  }
}
