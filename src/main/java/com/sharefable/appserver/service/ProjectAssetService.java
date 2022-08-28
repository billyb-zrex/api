package com.sharefable.appserver.service;

import com.sharefable.appserver.common.UpdateLog;
import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.repo.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjectAssetService {
    private final ProjectRepo projectRepo;

    private final List<Project.FieldRef> updatableFields = Project.UPDATABLE_FIELDS;

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

    @Transactional
    public Optional<Project> updateProject(Long id, Stream<UpdateLog<Project.FieldRef>> changeLog) {
        List<UpdateLog<Project.FieldRef>> collection = changeLog.filter(log -> updatableFields.contains(log.getField()))
            .collect(Collectors.toList());

        // WARN Extra db call to find the entity. Fix this later. Make the update work with partially constructed
        //  entity i.e. null values for non updatable field
        Optional<Project> projectWrap = projectRepo.findById(id);
        if (projectWrap.isPresent()){
            Project project = projectWrap.get();
            for (UpdateLog<Project.FieldRef> log : collection) {
                switch (log.getField()) {
                    case DisplayName:
                        project.setDisplayName((String)log.getValue());
                        break;

                    case Thumbnail:
                        project.setThumbnail((String)log.getValue());
                        break;

                    default:
                        break;
                }
            }
            return Optional.of(projectRepo.save(project));
        }
        return Optional.empty();
    }
}
