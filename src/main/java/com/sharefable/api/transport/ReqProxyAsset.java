package com.sharefable.api.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@GenerateTSDef
public class ReqProxyAsset {
    private String origin;
    private String clientInfo;
}
