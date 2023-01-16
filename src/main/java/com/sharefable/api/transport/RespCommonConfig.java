package com.sharefable.api.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@GenerateTSDef
public class RespCommonConfig extends ResponseBase {
    private String commonAssetPath;
    private String screenAssetPath;
    private String tourAssetPath;
    private String dataFileName;
    private String editFileName;
    private SchemaVersion latestSchemaVersion;
}
