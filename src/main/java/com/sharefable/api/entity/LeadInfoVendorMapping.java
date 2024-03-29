package com.sharefable.api.entity;

import com.sharefable.api.common.LeadInfoKey;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "lead_info_vendor_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
public class LeadInfoVendorMapping extends EntityBase {
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "house_lead_id", nullable = false)
  private HouseLeadInfo houseLeadInfo;


  @Enumerated(EnumType.STRING)
  private LeadInfoKey infoKey;

  private String infoValue;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Object auxData;
}
