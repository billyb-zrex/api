package com.sharefable.api.repo;


import com.sharefable.api.entity.AnalyticsMetrics;
import com.sharefable.api.transport.TotalVisitorsByYmd;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsMetricsRepo extends CrudRepository<AnalyticsMetrics, Long> {

    @Query(value = "SELECT SUM(metrics.viewsAll) FROM AnalyticsMetrics metrics WHERE metrics.tourId=:tourId AND metrics.dateYmd > :ymd")
    Long findSumOfAllVisitorsForTourId(Long tourId, String ymd);

    @Query(value = "SELECT NEW com.sharefable.api.transport.TotalVisitorsByYmd( metrics.viewsAll, metrics.dateYmd ) FROM AnalyticsMetrics metrics WHERE metrics.tourId=:tourId AND metrics.dateYmd > :ymd ORDER BY metrics.dateYmd DESC ")
    List<TotalVisitorsByYmd> findTotalVisitorsByYmd(Long tourId, String ymd);

    List<AnalyticsMetrics> findByTourId(Long tourId);
}
