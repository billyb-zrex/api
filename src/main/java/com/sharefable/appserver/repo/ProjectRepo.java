package com.sharefable.appserver.repo;

import com.sharefable.appserver.entity.Project;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepo extends PagingAndSortingRepository<Project, Long> {
}
