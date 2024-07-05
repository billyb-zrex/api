package com.sharefable.api.repo;

import com.sharefable.api.common.EntityConfigConfigType;
import com.sharefable.api.common.TourWithConfig;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.transport.TourDeleted;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepo extends CrudRepository<Tour, Long> {
  List<Tour> findAllByBelongsToOrgAndDeletedEqualsOrderByUpdatedAtDesc(Long belongsToOrgId, TourDeleted deleted);

  List<Tour> findAllByBelongsToOrgOrderByUpdatedAtDesc(Long belongsToOrgId);

  Optional<Tour> findByRid(String rid);

  Optional<Tour> findByRidAndDeletedEquals(String rid, TourDeleted deleted);

  @Query("SELECT new com.sharefable.api.common.TourWithConfig(t, e) " +
    "FROM Tour t " +
    "LEFT OUTER JOIN EntityConfigKV e ON t.belongsToOrg = e.entityId AND e.configType = :type " +
    "WHERE t.rid = :rid AND t.deleted = :deleted ORDER BY t.id LIMIT 1")
  Optional<TourWithConfig> findTourWithConfigByRidAndDeleted(String rid, TourDeleted deleted, EntityConfigConfigType type);

  @Query(value = "SELECT new com.sharefable.api.common.TourWithConfig(t, e) " +
    "FROM Tour t " +
    "LEFT OUTER JOIN EntityConfigKV e ON t.belongsToOrg = e.entityId AND e.configType = :type " +
    "WHERE t.id = :id ORDER BY t.id LIMIT 1")
  Optional<TourWithConfig> findTourWithConfigById(Long id, EntityConfigConfigType type);

  List<Tour> findAllByIdIn(List<Long> id);

  List<Tour> findAllByRidInAndDeletedEquals(List<String> rids, TourDeleted deleted);
}
