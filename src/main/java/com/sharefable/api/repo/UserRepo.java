package com.sharefable.api.repo;

import com.sharefable.api.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {
  Optional<User> findUserByEmail(String email);

  @Query("select count(*) from User user where user.belongsToOrg=:orgId and user.active=true and user.email not like 'fablesupport@%'")
  Integer countActiveUsersByBelongsToOrgWhoAreNotFableSupport(Long orgId);

  Set<User> getUsersByBelongsToOrgAndActiveIsTrue(Long orgId);

  Set<User> getUsersByBelongsToOrgAndActiveIsFalse(Long orgId);
}
