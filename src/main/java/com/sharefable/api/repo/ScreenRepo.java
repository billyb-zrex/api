package com.sharefable.api.repo;

import com.sharefable.api.entity.Screen;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepo extends CrudRepository<Screen, Long> {
    List<Screen> findAllByBelongsToOrgOrderByUpdatedAtDesc(Long belongsToOrgId);
}
