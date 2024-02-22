package com.sharefable.api.repo;

import com.sharefable.api.entity.AnalyticsConversion;
import com.sharefable.api.transport.ButtonClicks;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsConversionRepo extends CrudRepository<AnalyticsConversion, Long> {
    List<AnalyticsConversion> findByTourId(Long tourId);

    @Query(value = "SELECT NEW com.sharefable.api.transport.ButtonClicks(conversion.btnId, SUM(conversion.clicks)) FROM AnalyticsConversion conversion WHERE conversion.tourId=:tourId AND conversion.dateYmd > :ymd GROUP BY conversion.btnId")
    List<ButtonClicks> findClicksForAllButtonIdInATour(Long tourId, String ymd);
}
