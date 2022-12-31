package com.sharefable.api.repo;

import com.sharefable.api.entity.Org;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgRepo extends CrudRepository<Org, Long> {
}
