package com.sharefable.api.common;

import com.sharefable.api.transport.GenerateTSDef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenerateTSDef
public class CreditInfo {
  private Integer value;
  private Date updatedAt;
}
