/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.comparator.ComponentComparator;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.ofbiz.core.entity.GenericValue;

@EventComponent
public class CachingProjectManager extends AbstractProjectManager
{
    private final ProjectManager delegateProjectManager;
    private final ProjectComponentManager projectComponentManager;
    private final ProjectFactory projectFactory;
    private final ProjectKeyStore projectKeyStore;
    private final CachedReference<ProjectCache> cache;
    private final NodeAssociationStore nodeAssociationStore;

    public CachingProjectManager(ProjectManager delegateProjectManager, ProjectComponentManager projectComponentManager,
            ProjectFactory projectFactory, UserManager userManager, ApplicationProperties applicationProperties,
            ProjectKeyStore projectKeyStore, CacheManager cacheManager, NodeAssociationStore nodeAssociationStore)
    {
        super(userManager, applicationProperties);
        this.delegateProjectManager = delegateProjectManager;
        this.projectComponentManager = projectComponentManager;
        this.projectFactory = projectFactory;
        this.projectKeyStore = projectKeyStore;
        this.nodeAssociationStore = nodeAssociationStore;
        this.cache = cacheManager.getCachedReference(CachingProjectManager.class.getName() + ".cache",
                new ProjectCacheSupplier());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        updateCache();
        //make sure the project count cache is reloaded (the delegate may also do some caching)
        delegateProjectManager.refresh();
    }

    public void updateCache()
    {
        // refresh the project cache
        cache.reset();
    }

    // Business Methods ------------------------------------------------------------------------------------------------
    // Create Methods --------------------------------------------------------------------------------------------------
    @Override
    public long getNextId(Project project)
    {
        return delegateProjectManager.getNextId(project);
    }

    @Override
    public void refresh()
    {
        updateCache();
        delegateProjectManager.refresh();
    }

    // Get / Finder Methods --------------------------------------------------------------------------------------------
    @Override
    public GenericValue getProject(Long id)
    {
        return cache.get().getProject(id);
    }

    @Override
    public Project getProjectObj(Long id)
    {
        GenericValue projectGv = cache.get().getProject(id);
        return genericValueToProject(projectGv);
    }

    @Override
    public GenericValue getProjectByName(String name)
    {
        return cache.get().getProjectByName(name);
    }

    @Override
    public GenericValue getProjectByKey(String key)
    {
        return cache.get().getProjectByKey(key);
    }

    @Override
    public Project getProjectObjByKey(String projectKey)
    {
        GenericValue projectGv = getProjectByKey(projectKey);
        return genericValueToProject(projectGv);
    }

    @Nullable
    protected Project genericValueToProject(@Nullable final GenericValue projectGv)
    {
        if (projectGv != null)
        {
            return projectFactory.getProject(projectGv);
        }
        return null;
    }

    @Override
    public Project getProjectByCurrentKey(final String projectKey)
    {
        return genericValueToProject(cache.get().getProjectByCurrentKey(projectKey));
    }

    @Override
    public Project getProjectByCurrentKeyIgnoreCase(final String projectKey)
    {
        return genericValueToProject(cache.get().getProjectByCurrentKeyIgnoreCase(projectKey));
    }

    @Override
    public Project getProjectObjByKeyIgnoreCase(final String projectKey)
    {
        return genericValueToProject(cache.get().getProjectByKeyIgnoreCase(projectKey));
    }

    @Override
    public Set<String> getAllProjectKeys(Long projectId)
    {
        return cache.get().getAllProjectKeys(projectId);
    }

    @Override
    public Project getProjectObjByName(String projectName)
    {
        GenericValue projectGv = getProjectByName(projectName);
        return genericValueToProject(projectGv);
    }

    /**
     * Retrieves the Component with the given name in the given Project or null
     * if none match.
     *
     * @param project the Project.
     * @param name    the Component name.
     * @return the Component as a GenericValue or null if there is no match.
     * @deprecated use ProjectComponentManager and ProjectComponent
     */
    @Override
    public GenericValue getComponent(GenericValue project, String name)
    {
        return projectComponentManager.convertToGenericValue(projectComponentManager.findByComponentName(project.getLong("id"), name));
    }

    /**
     * Retrieves the Component (as a GenericValue) with the given id or null
     * if none match.
     *
     * @param id the id of the component to retrieve
     * @return the Component as a GenericValue or null if there is no match.
     * @deprecated use ProjectComponentManager and ProjectComponent
     */
    @Override
    public GenericValue getComponent(Long id)
    {
        try
        {
            return projectComponentManager.convertToGenericValue(projectComponentManager.find(id));
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
    }

    /**
     * Retrieve the collection of Components (as GenericValues) associated with the specified
     * project.
     *
     * @param project the project (as a GenericValue) used to search on
     * @return collection of components (as GenericValues) or null if there is no match.
     * @deprecated use ProjectComponentManager and ProjectComponent
     */
    @Override
    public Collection<GenericValue> getComponents(GenericValue project)
    {
        final Collection<ProjectComponent> allComponentsForProject = projectComponentManager.findAllForProject(project.getLong("id"));
        List<GenericValue> componentGVs = new ArrayList<GenericValue>(projectComponentManager.convertToGenericValues(allComponentsForProject));
        Collections.sort(componentGVs, new ComponentComparator());
        return componentGVs;
    }

    @Override
    public Collection<GenericValue> getProjects()
    {
        return noNull(cache.get().getProjects());
    }

    @Override
    public List<Project> getProjectObjects()
    {
        List<Project> projects = cache.get().getProjectObjects();
        if (projects == null)
        {
            return Collections.emptyList();
        }
        return projects;
    }

    @Override
    public long getProjectCount() throws DataAccessException
    {
        return getProjectObjects().size();
    }

    protected static <T> Collection<T> noNull(Collection<T> col)
    {
        if (col == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return col;
        }
    }

    @Override
    public Project createProject(final String name, final String key, final String description, final String leadKey,
            final String url, final Long assigneeType, final Long avatarId)
    {
        try
        {
            return delegateProjectManager.createProject(name, key, description, leadKey, url, assigneeType, avatarId);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public Project updateProject(final Project updatedProject, final String name, final String description,
            final String leadKey, final String url, final Long assigneeType, final Long avatarId, final String projectKey)
    {
        try
        {
            return delegateProjectManager.updateProject(updatedProject, name, description, leadKey, url, assigneeType, avatarId, projectKey);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public void removeProjectIssues(final Project project) throws RemoveException
    {
        delegateProjectManager.removeProjectIssues(project);
    }

    @Override
    public void removeProject(final Project project)
    {
        try
        {
            delegateProjectManager.removeProject(project);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public Collection<GenericValue> getProjectCategories()
    {
        return noNull(cache.get().getProjectCategories());
    }

    @Override
    public Collection<ProjectCategory> getAllProjectCategories()
    {
        return Entity.PROJECT_CATEGORY.buildList(getProjectCategories());
    }

    @Override
    public GenericValue getProjectCategory(Long id)
    {
        return cache.get().getProjectCategory(id);
    }

    @Override
    public ProjectCategory getProjectCategoryObject(Long id)
    {
        return Entity.PROJECT_CATEGORY.build(cache.get().getProjectCategory(id));
    }

    @Override
    public void updateProjectCategory(GenericValue projectCat)
    {
        try
        {
            delegateProjectManager.updateProjectCategory(projectCat);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public void updateProjectCategory(ProjectCategory projectCategory) throws DataAccessException
    {
        try
        {
            delegateProjectManager.updateProjectCategory(projectCategory);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public Collection<GenericValue> getProjectsFromProjectCategory(GenericValue projectCategory)
    {
        return cache.get().getProjectsFromProjectCategory(projectCategory);
    }

    @Override
    public Collection<Project> getProjectsFromProjectCategory(ProjectCategory projectCategory)
            throws DataAccessException
    {
        return getProjectObjectsFromProjectCategory(projectCategory.getId());
    }

    @Override
    public Collection<Project> getProjectObjectsFromProjectCategory(final Long projectCategoryId)
    {
        return projectFactory.getProjects(getProjectsFromProjectCategory(getProjectCategory(projectCategoryId)));
    }

    @Override
    public GenericValue getProjectCategoryFromProject(GenericValue project)
    {
        return cache.get().getProjectCategoryFromProject(project);
    }

    @Override
    public ProjectCategory getProjectCategoryForProject(Project project)
    {
        GenericValue projectCategoryForProject = cache.get().getProjectCategoryForProject(project);
        if (projectCategoryForProject != null)
        {
            return Entity.PROJECT_CATEGORY.build(projectCategoryForProject);
        }
        return null;
    }

    @Override
    public Collection<GenericValue> getProjectsWithNoCategory()
    {
        return cache.get().getProjectsWithNoCategory();
    }

    @Override
    public Collection<Project> getProjectObjectsWithNoCategory() throws DataAccessException
    {
        return projectFactory.getProjects(getProjectsWithNoCategory());
    }

    @Override
    public void setProjectCategory(GenericValue project, GenericValue category)
    {
        try
        {
            delegateProjectManager.setProjectCategory(project, category);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public void setProjectCategory(Project project, ProjectCategory category) throws DataAccessException
    {
        try
        {
            delegateProjectManager.setProjectCategory(project, category);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public List<Project> getProjectsLeadBy(User leadUser)
    {
        return delegateProjectManager.getProjectsLeadBy(leadUser);
    }

    @Override
    public List<Project> getProjectsLeadBy(ApplicationUser leadUser)
    {
        return delegateProjectManager.getProjectsLeadBy(leadUser);
    }

    @Override
    public Collection<GenericValue> getProjectsByLead(User leadUser)
    {
        return delegateProjectManager.getProjectsByLead(leadUser);
    }

    @Override
    public ProjectCategory createProjectCategory(String name, String description)
    {
        try
        {
            return delegateProjectManager.createProjectCategory(name, description);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public void removeProjectCategory(Long id)
    {
        try
        {
            delegateProjectManager.removeProjectCategory(id);
        }
        finally
        {
            updateCache();
        }
    }

    @Override
    public long getCurrentCounterForProject(Long id)
    {
        return delegateProjectManager.getCurrentCounterForProject(id);
    }

    @Override
    public void setCurrentCounterForProject(Project project, long counter)
    {
        delegateProjectManager.setCurrentCounterForProject(project, counter);
    }

    private class ProjectCacheSupplier implements Supplier<ProjectCache>
    {
        @Override
        public ProjectCache get()
        {
            return new ProjectCache(delegateProjectManager, projectKeyStore, nodeAssociationStore);
        }
    }
}
