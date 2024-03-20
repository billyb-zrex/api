package com.sharefable.api.repo;

import com.sharefable.api.entity.AnalyticsCtaClicked;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsCtaClickedRepo extends CrudRepository<AnalyticsCtaClicked, Long> {
}
