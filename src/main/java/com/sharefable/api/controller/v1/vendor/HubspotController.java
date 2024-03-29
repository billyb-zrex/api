package com.sharefable.api.controller.v1.vendor;

import com.sharefable.api.controller.Routes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class HubspotController {
  @RequestMapping(value = Routes.HUBSPOT_DATA_FETCH_URL_HOOK, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Lead360Data getLead360Data(
    @RequestParam(name = "associatedObjectId") String hsLeadId,
    @RequestHeader("X-HubSpot-Signature") String hsSig,
    HttpServletRequest request
  ) {
    System.out.println(hsLeadId);
    System.out.println(hsSig);
    System.out.println(request.getRequestURI());
    System.out.println(request.getPathInfo());
    System.out.println(request.getQueryString());
//    System.out.println(body); // 5283919377


//    Prop prop = new Prop("12");
    LeadResult result1 = new LeadResult(
      1235L,
      "Hubspot demo",
      "https://app.sharefable.com",
      12,
      "No",
      1711045915271L,
      120,
      "https://app.sharefable.com/link2"
    );

    LeadResult result2 = new LeadResult(
      1236L,
      "ABM Campaign 1",
      "https://app.sharefable.com",
      85,
      "Yes",
      1711045915271L,
      300,
      "https://app.sharefable.com/link"
    );

    return new Lead360Data(new LeadResult[]{result1, result2});
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
