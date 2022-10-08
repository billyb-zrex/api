package com.sharefable.api.entity;

import com.sharefable.api.annotations.ESDocument;
import com.sharefable.api.annotations.ESQueryable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Any changes to this requires changes to the index creation process
 * Index creation details -> settings/es.http
 *
 * The fields names are case-sensitive.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ESDocument(schema = "es.index.schema.json")
public class AssetContent {
    @ESQueryable(exclude = true)
    private String id;

    @ESQueryable(type = ESQueryable.SearchType.Term)
    private Long assetId;

    @ESQueryable(type = ESQueryable.SearchType.Term)
    private String assetPath;

    @ESQueryable(type = ESQueryable.SearchType.Term)
    private String method;

    @ESQueryable(exclude = true)
    private Object reqParams;

    @ESQueryable(isAlsoKeyword = true)
    private String reqParamsStr;

    // Earlier the plan was to run flattened query to the reqBody field. This would require constructing JSON path
    // to each leaf node and run match queries. Also, simultaneously fire query in reqBodyStr.
    // Currently, we would just fire query with reqBodyStr.
    // TODO if the match quality is not high, then implement this as well. This field is indexed but not queried
    @ESQueryable(exclude = true)
    private Object reqBody;

    @ESQueryable(isAlsoKeyword = true)
    private String reqBodyStr;

    @ESQueryable(exclude = true)
    private String reqHeaders;

    @ESQueryable(exclude = true)
    private String respHeaders;

    @ESQueryable(exclude = true)
    private String respDataUri;
}
