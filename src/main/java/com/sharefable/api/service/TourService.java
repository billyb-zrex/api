package com.sharefable.api.service;

import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.transport.ReqNewTour;
import com.sharefable.api.transport.RespTour;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TourService {
    private final TourRepo tourRepo;

    @Autowired
    public TourService(TourRepo tourRepo) {
        this.tourRepo = tourRepo;
    }

    @Transactional
    public List<RespTour> getAllToursForOrg(Long orgId) {
        List<Tour> tours = tourRepo.findAllByBelongsToOrgOrderByUpdatedAtDesc(orgId);
        return tours.stream().map(RespTour::from).collect(Collectors.toList());
    }

    public RespTour createNewTour(ReqNewTour req, User createdByUser) {
        String prefixHash = Utils.createUuidWord();
        Tour tour = Tour.builder()
            .createdBy(createdByUser)
            .displayName(req.name())
            .description(req.description())
            .rid(Utils.createReadableId(req.name()))
            .assetPrefixHash(prefixHash)
            .belongsToOrg(createdByUser.getBelongsToOrg().getId())
            .build();

        Tour storedTour = tourRepo.save(tour);
        return RespTour.from(storedTour);
    }
}
