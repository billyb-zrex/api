package com.sharefable.api.repo;

import com.sharefable.api.entity.AssetContent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetContentRepo extends ElasticsearchRepository<AssetContent, String> {
}
