package com.sharefable.api.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "analytics_user_aid_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class AnalyticsUserAidMapping extends EntityBase {
  @Column(nullable = false)
  private String aid;

  @Column(nullable = false)
  private Long tourId;

  @Column(nullable = false)
  private String primaryKey;
  @Column(nullable = false)
  private String dateYmd;
  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Object leadFormInfo;
}
