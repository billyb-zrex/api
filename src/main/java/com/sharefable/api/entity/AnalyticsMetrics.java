package com.sharefable.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "analytics_tour_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class AnalyticsMetrics extends AnalyticsBase {
    @Column(nullable = false)
    private Long viewsUnique;

    @Column(nullable = false)
    private Long viewsAll;
}
