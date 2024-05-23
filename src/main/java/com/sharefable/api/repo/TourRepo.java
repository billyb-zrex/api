package com.sharefable.api.repo;

import com.sharefable.api.entity.Tour;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepo extends CrudRepository<Tour, Long> {
  List<Tour> findAllByBelongsToOrgOrderByUpdatedAtDesc(Long belongsToOrgId);

  Optional<Tour> findByRid(String rid);

  List<Tour> findAllByIdIn(List<Long> id);

  List<Tour> findAllByRidIn(List<String> rids);
}
