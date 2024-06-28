package com.sharefable.api.repo;

import com.sharefable.api.entity.AnalyticsUserAidMapping;
import com.sharefable.api.transport.TourLeads;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsUserAidMappingRepo extends CrudRepository<AnalyticsUserAidMapping, Long> {
  List<AnalyticsUserAidMapping> findByTourId(Long tourId);

  @Query(value = "SELECT NEW com.sharefable.api.transport.TourLeads(userIdMapping.aid, userIdMapping.primaryKey, userIdMapping.leadFormInfo) FROM AnalyticsUserAidMapping userIdMapping WHERE userIdMapping.tourId=:tourId AND userIdMapping.dateYmd > :ymd ORDER BY userIdMapping.dateYmd DESC")
  List<TourLeads> findLeadsByYmd(Long tourId, String ymd);

  @Query(value = "SELECT COUNT(DISTINCT userIdMapping.primaryKey) FROM AnalyticsUserAidMapping userIdMapping WHERE userIdMapping.tourId=:tourId AND userIdMapping.dateYmd > :ymd")
  Long findUniqueEmails(Long tourId, String ymd);

  List<AnalyticsUserAidMapping> getAnalyticsUserAidMappingsByTourIdAndPrimaryKeyOrderByUpdatedAtDesc(Long tourId, String emailId);
}
