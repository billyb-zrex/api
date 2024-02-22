package com.sharefable.api.repo;

import com.sharefable.api.entity.AnalyticsAnnClick;
import com.sharefable.api.transport.TourAnnViewsWithPercentile;
import com.sharefable.api.transport.TourAnnWithViews;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsAnnClickRepo extends CrudRepository<AnalyticsAnnClick, Long> {

    @Query(value = "SELECT NEW com.sharefable.api.transport.TourAnnWithViews(annClick.annId, SUM(annClick.viewsAll)) FROM AnalyticsAnnClick annClick WHERE annClick.tourId=:tourId AND annClick.dateYmd > :ymd GROUP BY annClick.annId")
    List<TourAnnWithViews> findTotalViewsForAnn(Long tourId, String ymd);

    List<AnalyticsAnnClick> findByTourId(Long tourId);

    @Query(value = "SELECT NEW com.sharefable.api.transport.TourAnnViewsWithPercentile (annClick.annId, SUM(annClick.viewsAll), CAST(ROUND(AVG(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(annClick.timeSpentDist, ',', 5), ',', -1) AS DOUBLE)), 2) AS DOUBLE)) FROM AnalyticsAnnClick annClick WHERE annClick.tourId=:tourId and annClick.dateYmd >:ymd GROUP BY annClick.annId")
    List<TourAnnViewsWithPercentile> findTotalViewWithPercentile(Long tourId, String ymd);
}
