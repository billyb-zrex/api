package com.sharefable.api.transport;

import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.PlatformIntegration;
import com.sharefable.api.entity.TenantIntegration;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@GenerateTSDef
public class RespFatTenantIntegration {
  private Org org;
  private PlatformIntegration platformIntegration;
  private TenantIntegration tenantIntegration;
}
