package com.sharefable.api.repo;

import com.sharefable.api.common.EntityConfigConfigType;
import com.sharefable.api.common.TopLevelEntityType;
import com.sharefable.api.common.TourWithConfig;
import com.sharefable.api.entity.DemoEntity;
import com.sharefable.api.transport.TourDeleted;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemoEntityRepo extends CrudRepository<DemoEntity, Long> {
  List<DemoEntity> findAllByBelongsToOrgAndDeletedAndEntityTypeEqualsOrderByUpdatedAtDesc(Long belongsToOrgId, TourDeleted deleted, TopLevelEntityType type);

  List<DemoEntity> findAllByBelongsToOrgOrderByUpdatedAtDesc(Long belongsToOrgId);

  Optional<DemoEntity> findByRid(String rid);

  Optional<DemoEntity> findByRidAndEntityType(String rid, TopLevelEntityType type);

  Optional<DemoEntity> findByRidAndDeletedEquals(String rid, TourDeleted deleted);

  Optional<DemoEntity> findByRidAndDeletedAndEntityType(String rid, TourDeleted deleted, TopLevelEntityType entityType);

  Optional<DemoEntity> findByRidAndDeleted(String rid, TourDeleted deleted);

  @Query("SELECT new com.sharefable.api.common.TourWithConfig(t, e) " +
    "FROM DemoEntity t " +
    "LEFT OUTER JOIN EntityConfigKV e ON t.belongsToOrg = e.entityId AND e.configType = :type " +
    "WHERE t.rid = :rid AND t.deleted = :deleted AND t.entityType = :entityType ORDER BY t.id LIMIT 1")
  Optional<TourWithConfig> findTourWithConfigByRidAndDeletedAndEntityType(String rid, TourDeleted deleted, EntityConfigConfigType type, TopLevelEntityType entityType);

  @Query(value = "SELECT new com.sharefable.api.common.TourWithConfig(t, e) " +
    "FROM DemoEntity t " +
    "LEFT OUTER JOIN EntityConfigKV e ON t.belongsToOrg = e.entityId AND e.configType = :type " +
    "WHERE t.id = :id ORDER BY t.id LIMIT 1")
  Optional<TourWithConfig> findTourWithConfigById(Long id, EntityConfigConfigType type);

  List<DemoEntity> findAllByIdIn(List<Long> id);

  List<DemoEntity> findAllByRidInAndDeletedEquals(List<String> rids, TourDeleted deleted);
}
