package com.sharefable.api.repo;

import com.sharefable.api.entity.Project;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepo extends PagingAndSortingRepository<Project, Long> {
}
