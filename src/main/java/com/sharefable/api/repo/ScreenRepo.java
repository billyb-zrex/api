package com.sharefable.api.repo;

import com.sharefable.api.entity.Screen;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenRepo extends CrudRepository<Screen, Long> {
}
