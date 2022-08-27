package com.sharefable.appserver.service;

import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.repo.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAssetService {
    private final ProjectRepo projectRepo;

    @Autowired
    public ProjectAssetService(ProjectRepo projectRepo) {
        this.projectRepo = projectRepo;
    }

    @Transactional
    public Project newProject(String displayName) {
        String name = Utils.normalizeProjectName(displayName);

        Project project = Project.builder()
            .name(name)
            .displayName(displayName)
            .build();

        return projectRepo.save(project);
    }
}
