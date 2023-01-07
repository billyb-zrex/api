package com.sharefable.api.repo;

import com.sharefable.api.entity.Screen;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRepo extends CrudRepository<Screen, Long> {
    List<Screen> findAllByBelongsToOrgOrderByUpdatedAtDesc(Long belongsToOrgId);

    Optional<Screen> findByRid(String rid);
}
