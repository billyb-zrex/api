package com.sharefable.api.service;

import com.sharefable.api.entity.EntityBaseWithReadableId;
import com.sharefable.api.entity.EntityHolding;
import com.sharefable.api.repo.EntityHoldingRepo;
import com.sharefable.api.repo.ScreenRepo;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.transport.EntityHoldingInfoBase;
import com.sharefable.api.transport.EntityType;
import com.sharefable.api.transport.req.ReqEntityAssetAssn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Slf4j
public class EntityHoldingService {
    private final TourRepo tourRepo;

    private final ScreenRepo screenRepo;

    private final EntityHoldingRepo entityHoldingRepo;

    public EntityHoldingService(TourRepo tourRepo, ScreenRepo screenRepo, EntityHoldingRepo entityHoldingRepo) {
        this.tourRepo = tourRepo;
        this.screenRepo = screenRepo;
        this.entityHoldingRepo = entityHoldingRepo;
    }

    EntityHolding addAssociation(ReqEntityAssetAssn body, String assetKey, EntityHoldingInfoBase info) {
        Optional<? extends EntityBaseWithReadableId> maybeEntity;
        if (body.getEntityType() == EntityType.Screen) {
            maybeEntity = screenRepo.findByRid(body.getEntityRid());
        } else if (body.getEntityType() == EntityType.Tour) {
            maybeEntity = tourRepo.findByRid(body.getEntityRid());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No association is mentioned");
        }

        if (maybeEntity.isEmpty()) {
            log.error("Media processing is requested without explicit association with entity. Entity type {}, Entity rid {}",
                body.getEntityType().name(),
                body.getEntityRid());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No association is mentioned");
        }

        EntityHolding entityHolding = EntityHolding.builder()
            .entityType(body.getEntityType())
            .entityKey(maybeEntity.get().getId())
            .assetKey(assetKey)
            .info(info)
            .build();

        return entityHoldingRepo.save(entityHolding);
    }
}
