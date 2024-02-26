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

    @Query(value = "SELECT NEW com.sharefable.api.transport.TourAnnWithViews(" +
        "annClick.annId, "
        + "SUM(annClick.viewsAll), "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[4]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[5]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[7]') AS DOUBLE))  "
        + ") FROM AnalyticsAnnClick annClick WHERE annClick.tourId=:tourId AND annClick.dateYmd > :ymd GROUP BY annClick.annId")
    List<TourAnnWithViews> findTotalViewsForAnn(Long tourId, String ymd);

    List<AnalyticsAnnClick> findByTourId(Long tourId);

    @Query(value = "SELECT NEW com.sharefable.api.transport.TourAnnViewsWithPercentile ("
        + "annClick.annId, "
        + "SUM(annClick.viewsAll), "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[0]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[1]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[2]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[3]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[4]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[5]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[6]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[7]') AS DOUBLE)),  "
        + "AVG(CAST(FUNCTION('JSON_EXTRACT', annClick.timeSpentDist, '$[8]') AS DOUBLE)))  "
        + "FROM AnalyticsAnnClick annClick WHERE annClick.tourId=:tourId and annClick.dateYmd >:ymd GROUP BY annClick.annId")
    List<TourAnnViewsWithPercentile> findTotalViewWithPercentile(Long tourId, String ymd);
}
