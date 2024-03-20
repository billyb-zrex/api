package com.sharefable.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "analytics_cta_clicked")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class AnalyticsCtaClicked extends EntityBase {
    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private String tz;

    @Column(nullable = false)
    private Long uts;
    @Column(nullable = false)
    private String aid;
    @Column(nullable = false)
    private String sid;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private Long tourId;
    @Column(nullable = false)
    private String ctaFrom;
    @Column(nullable = false)
    private String btnId;
    @Column(nullable = false)
    private String url;
}
