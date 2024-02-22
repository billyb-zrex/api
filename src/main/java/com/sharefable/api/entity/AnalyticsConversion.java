package com.sharefable.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "analytics_conversion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class AnalyticsConversion extends AnalyticsBase {
    @Column(nullable = false)
    private String btnId;

    @Column(nullable = false)
    private Long clicks;

}
