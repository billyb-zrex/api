package com.sharefable.appserver.repo;

import com.sharefable.appserver.entity.AssetMapping;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProxyAssetRepo extends PagingAndSortingRepository<AssetMapping, Long> {
    List<AssetMapping> findAssetMappingByProjectIdAndIsActiveIsTrue(Long projectId);
}
