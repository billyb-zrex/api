package com.sharefable.api.repo;

import com.sharefable.api.entity.Tour;
import com.sharefable.api.transport.TourDeleted;
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

  List<Tour> findAllByIdIn(List<Long> id);

  List<Tour> findAllByRidInAndDeletedEquals(List<String> rids, TourDeleted deleted);
}
