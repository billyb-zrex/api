package com.sharefable.api.service;

import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Project;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.ProjectRepo;
import com.sharefable.api.transport.NewProjectReq;
import com.sharefable.api.transport.ProjectResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final WorkspaceService wsService;

    @Autowired
    public ProjectService(ProjectRepo projectRepo, WorkspaceService wsService) {
        this.projectRepo = projectRepo;
        this.wsService = wsService;
    }


    @Transactional
    public ProjectResp newProject(NewProjectReq body) {
        Optional<User> user = wsService.getUserEntity(body.getCreatedBy());
        if (!user.isPresent()) {
            log.error("Can't create project as user with id {} not present", body.getCreatedBy());
            return ProjectResp.Empty();
        }

        Project project = Project.builder()
            .rid(Utils.createReadableId(body.getDisplayName()))
            .displayName(body.getDisplayName())
            .thumbnail(body.getThumbnail())
            .createdBy(user.get())
            .belongsToOrg(user.get().getBelongsToOrg())
            .build();

        Project savedProject = projectRepo.save(project);
        return ProjectResp.from(savedProject);
    }
}
