package com.sharefable.api.entity;

import com.sharefable.api.transport.resp.RespPlatformIntegration;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "platform_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TransportObjRef(cls = RespPlatformIntegration.class)
public class PlatformIntegration extends EntityBase {
  private String type;
  private String name;
  private String icon;
  private String description;
  private Boolean disabled;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Map<String, Object> platformConfig;
}
