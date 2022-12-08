package com.sharefable.api.transport;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.ProxyAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class ProxyAssetResp {
    private String proxyUri;

    public static ProxyAssetResp from(ProxyAsset asset, S3Config pathConfig) {
        try {
            return Utils.fromEntityToTransportObject(asset, ProxyAssetResp.class, (ProxyAsset entity, ProxyAssetResp transportObj, List<Field> failedFields) -> {
                AssetFilePath filePath = pathConfig.getQualifiedPathFor(S3Config.AssetType.ProxyAsset, asset.getProxyUri());
                transportObj.setProxyUri(filePath.getS3UriToFile());
            });
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static ProxyAssetResp Empty() {
        return new ProxyAssetResp();
    }
}
