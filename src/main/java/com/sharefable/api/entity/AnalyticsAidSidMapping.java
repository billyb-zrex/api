package com.sharefable.api.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "analytics_aid_sid_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class AnalyticsAidSidMapping extends EntityBase {
    @Column(nullable = false)
    private String aid;
    @Column(nullable = false)
    private String sid;
}
