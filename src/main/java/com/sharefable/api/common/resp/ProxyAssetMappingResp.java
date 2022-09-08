package com.sharefable.api.common.resp;


import com.sharefable.api.entity.AssetMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProxyAssetMappingResp {
    private byte[] body;
    private AssetMapping proxy;
    private boolean isFound;
}
