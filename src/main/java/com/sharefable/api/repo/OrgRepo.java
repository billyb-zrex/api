package com.sharefable.api.repo;

import com.sharefable.api.entity.Org;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface OrgRepo extends CrudRepository<Org, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Org o where o.id = :id")
  Optional<Org> lockForSubscription(@Param("id") Long id);

  Optional<Org> findFirstByRid(String rId);

  Set<Org> findOrgByDomain(String emailDomain);
}

