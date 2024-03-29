package com.sharefable.api.repo;

import com.sharefable.api.entity.Lead360;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Lead360Repo extends CrudRepository<Lead360, Long> {
  List<Lead360> findLead360ByTourId(Long tourId);
}
