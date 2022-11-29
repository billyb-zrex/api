package com.sharefable.api.repo;

import com.sharefable.api.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends PagingAndSortingRepository<User, Long> {
}
