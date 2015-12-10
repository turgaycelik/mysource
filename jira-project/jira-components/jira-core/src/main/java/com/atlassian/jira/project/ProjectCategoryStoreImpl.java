package com.atlassian.jira.project;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.ProjectCategoryFactory;

import java.util.List;

public class ProjectCategoryStoreImpl implements ProjectCategoryStore
{
    public static final String ENTITY_NAME = "ProjectCategory";

    private final EntityEngine entityEngine;

    public ProjectCategoryStoreImpl(EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Override
    public ProjectCategory getProjectCategory(Long id)
    {
        return entityEngine.selectFrom(Entity.PROJECT_CATEGORY).findById(id);
    }

    @Override
    public List<ProjectCategory> getAllProjectCategories()
    {
        return entityEngine.selectFrom(Entity.PROJECT_CATEGORY).findAll().orderBy("name");
    }

    @Override
    public ProjectCategory createProjectCategory(String name, String description)
    {
        ProjectCategory projectCategory = new ProjectCategoryFactory.Builder()
                .name(name)
                .description(description)
                .build();
        return entityEngine.createValue(Entity.PROJECT_CATEGORY, projectCategory);
    }

    @Override
    public void removeProjectCategory(Long id)
    {
        entityEngine.removeValue(Entity.PROJECT_CATEGORY, id);
    }

    @Override
    public void updateProjectCategory(ProjectCategory projectCategory)
    {
        entityEngine.updateValue(Entity.PROJECT_CATEGORY, projectCategory);
    }
}
