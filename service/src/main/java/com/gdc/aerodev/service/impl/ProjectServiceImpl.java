package com.gdc.aerodev.service.impl;

import com.gdc.aerodev.dao.ProjectDao;
import com.gdc.aerodev.dao.UserDao;
import com.gdc.aerodev.dao.exception.DaoException;
import com.gdc.aerodev.dao.postgres.PostgresProjectDao;
import com.gdc.aerodev.model.Project;
import com.gdc.aerodev.model.ProjectType;
import com.gdc.aerodev.model.User;
import com.gdc.aerodev.service.ProjectService;
import com.gdc.aerodev.service.logging.LoggingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDao dao;
    private final UserDao userDao;

    public ProjectServiceImpl(ProjectDao dao, UserDao userDao) {
        this.dao = dao;
        this.userDao = userDao;
    }

    @Override
    public Long createProject(String projectName, Long projectOwner, ProjectType projectType) {
        if (projectName.equals("") || projectOwner == null) {
            return null;
        }
        if (isExistentName(projectName)) {
            log.error("Project with name '" + projectName + "' is already exists.");
            return null;
        }
        try {
            Long id = dao.save(new Project(projectName, projectOwner, projectType));
            log.info("Project '" + projectName + "' created with id " + id + ".");
            return id;
        } catch (DaoException e) {
            return null;
            //FIXME: fix exception handling
        }
    }

    @Override
    public Long updateProject(Long projectId, String projectName, ProjectType projectType) {
        Project project = dao.getById(projectId);
        if (!projectName.equals("")) {
            if (isExistentName(projectName)) {
                log.error("Project with name '" + projectName + "' is already exists.");
                return null;
            }
            project.setProjectName(projectName);
        }
        project.setProjectType(projectType);
        try {
            log.info("Project '" + projectName + "' successfully updated.");
            return dao.save(project);
        } catch (DaoException e) {
            return null;
        }
    }

    @Override
    public Project getProject(String name) {
        return dao.getByName(name);
    }

    @Override
    public Project getProject(Long id) {
        return dao.getById(id);
    }

    @Override
    public boolean isOwner(Project project, Long userId) {
        return project.getProjectOwner().equals(userId);
    }

    public List<Project> getByUserId(Long id) {
        return dao.getByUserId(id);
    }

    public int countProjects() {
        return dao.count();
    }

    private boolean isExistentName(String projectName) {
        return dao.getByName(projectName) != null;
    }

    public Map<Integer, Map<User, Project>> getTopThree() {
        int n = 3;
        Map<Integer, Map<User, Project>> map = new HashMap<>(n);
        List<Project> projects = dao.getTopThree();
        for (int i = 0; i < n; i++) {
            Map<User, Project> uPMap = new HashMap<>(1);
            Project project = projects.get(i);
            uPMap.put(userDao.getById(project.getProjectOwner()), project);
            map.put(i, uPMap);
        }
        return map;
    }
}