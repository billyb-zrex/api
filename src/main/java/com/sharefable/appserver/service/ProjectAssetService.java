package com.sharefable.appserver.service;

import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.repo.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<Project> getAllProjects(){
        return Streamable.of(projectRepo.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))).toList();
    }
}
