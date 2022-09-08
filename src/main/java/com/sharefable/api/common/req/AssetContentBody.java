package com.sharefable.api.common.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetContentBody {
    private boolean base64Encoded;
    private Object body;
}
