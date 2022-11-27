package com.sharefable.api.repo;

import com.sharefable.api.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepo extends PagingAndSortingRepository<User, Long> {
}
