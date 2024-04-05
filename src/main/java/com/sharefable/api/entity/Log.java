package com.sharefable.api.entity;

import com.sharefable.api.common.ForObjectType;
import com.sharefable.api.common.LogType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Log extends EntityBase {
  private Long orgId;

  @Enumerated(value = EnumType.STRING)
  private LogType logType;

  @Enumerated(value = EnumType.STRING)
  private ForObjectType forObjectType;

  private Long forObjectId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Object logLine;
}
