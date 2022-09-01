package com.sharefable.appserver.common.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProxyAssetBody {
    private boolean base64Encoded;
    private Object body;
}
