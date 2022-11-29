package com.sharefable.api.repo;

import com.sharefable.api.entity.ProxyAsset;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProxyAssetRepo extends PagingAndSortingRepository<ProxyAsset, Long> {
    Optional<ProxyAsset> findProxyAssetByBelongsToProjAndRid(Long projectId, String rid);
}
