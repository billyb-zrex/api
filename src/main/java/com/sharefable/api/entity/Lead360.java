package com.sharefable.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@Entity
@Table(name = "lead_360")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class Lead360 extends EntityBase {
  private Long tourId;
  private Integer demoVisited;
  private Integer sessionsCreated;
  private Integer timeSpentSec;
  private Timestamp lastInteractedAt;
  private Integer completionPercentage;
  private Double ctaClickRate;

  @Transient
  public static Lead360 Empty(Long tourId) {
    return Lead360.builder()
      .tourId(tourId)
      .demoVisited(0)
      .sessionsCreated(0)
      .timeSpentSec(0)
      .lastInteractedAt(null)
      .completionPercentage(0)
      .ctaClickRate(0.0)
      .build();
  }

  @Transient
  public static Lead360 Empty() {
    return Lead360.builder()
      .tourId(0L)
      .demoVisited(0)
      .sessionsCreated(0)
      .timeSpentSec(0)
      .lastInteractedAt(null)
      .completionPercentage(0)
      .ctaClickRate(0.0)
      .build();
  }
}
