package com.sharefable.api.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "analytics_tour_ann_clicks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class AnalyticsAnnClick extends AnalyticsBase {
    @Column(nullable = false)
    private String annId;

    @Column(nullable = false)
    private Long viewsUnique;

    @Column(nullable = false)
    private Long viewsAll;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<Long> timeSpentDist;
}
