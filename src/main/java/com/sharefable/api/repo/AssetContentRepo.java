package com.sharefable.api.repo;

import com.sharefable.api.entity.AssetContent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AssetContentRepo extends ElasticsearchRepository<AssetContent, Long> {
    List<AssetContent> findAllByAssetIdAndAssetPathAndMethodAndReqParamsAndReqBody(
        Long assetId,
        String assetPath,
        String method,
        Map<String, String> reqParams,
        Object reqBody
    );
}
