package com.sharefable.api.repo;

import com.sharefable.api.entity.AssetMapping;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProxyAssetRepo extends PagingAndSortingRepository<AssetMapping, Long> {
    List<AssetMapping> findAssetMappingByProjectIdAndIsActiveIsTrue(Long projectId);

    List<AssetMapping> findAssetMappingByProjectIdAndAssetNameAndIsActiveIsTrueOrderByUpdatedAtDesc(Long projectId, String assetName);
}
