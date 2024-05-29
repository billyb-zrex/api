package com.sharefable.api.repo;

import com.sharefable.api.common.ConfigEntityType;
import com.sharefable.api.common.EntityConfigConfigType;
import com.sharefable.api.entity.EntityConfigKV;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityConfigKVRepo extends CrudRepository<EntityConfigKV, Long> {
  List<EntityConfigKV> findEntityConfigKVSByEntityTypeAndEntityIdAndConfigType(
    ConfigEntityType entityType,
    Long entityId,
    EntityConfigConfigType configType
  );

  List<EntityConfigKV> findEntityConfigKVSByEntityTypeAndEntityIdAndConfigTypeAndConfigKey(
    ConfigEntityType entityType,
    Long entityId,
    EntityConfigConfigType configType,
    String configKey
  );
}
