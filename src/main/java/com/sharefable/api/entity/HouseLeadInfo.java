package com.sharefable.api.entity;

import com.sharefable.api.common.TransportObjRef;
import com.sharefable.api.transport.resp.RespHouseLeadInfo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "house_lead_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@TransportObjRef(cls = RespHouseLeadInfo.class)
public class HouseLeadInfo extends EntityBase {
  private Long orgId;

  private String leadEmailId;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "house_lead_id", nullable = false)
  private Set<Lead360> info360;
}
