package com.sharefable.api.transport;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.ProxyAsset;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class RespProxyAsset extends ResponseBase {
    private String proxyUri;

    public static RespProxyAsset from(ProxyAsset asset, S3Config pathConfig) {
        try {
            RespProxyAsset resp = (RespProxyAsset) Utils.fromEntityToTransportObject(asset);
            AssetFilePath filePath = pathConfig.getQualifiedPathFor(S3Config.AssetType.ProxyAsset, asset.getProxyUri());
            resp.setProxyUri(filePath.getS3UriToFile());
            return resp;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Can't convert entity to transport object. Error: " + e.getMessage());
            e.printStackTrace();
            return Empty();
        }
    }

    public static RespProxyAsset Empty() {
        return new RespProxyAsset();
    }
}
