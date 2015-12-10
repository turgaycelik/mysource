/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.util.profiling.UtilTimerStack;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;

/**
 * This is a very basic cache that stores projects and components
 * <p/>
 * When constructed, or when you call refresh() - it will find and cache all projects, components
 */
public class ProjectCache
{
    private static final Logger LOG = Logger.getLogger(ProjectCache.class);
    private static final Comparator<GenericValue> PROJECT_NAME_COMPARATOR = OfBizComparators.NAME_COMPARATOR;

    private final OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
    private final ProjectManager projectManager;
    private final NodeAssociationStore nodeAssociationStore;

    private final ProjectKeyStore projectKeyStore;
    // maps of projects by ID and key
    private volatile ImmutableMap<Long, GenericValue> projectsById;
    private volatile ImmutableMap<String, GenericValue> projectsByCurrentKey;

    private volatile ImmutableSortedMap<String, GenericValue> projectsByCurrentKeyIgnoreCase;

    // list of projectCategories
    private volatile ImmutableMap<Long, GenericValue> projectCategories;

    /** Map of Project Key to Project Category ID */
    private volatile ImmutableMap<String, Long> projectToProjectCategories;

    // Map of projectCategory ID to List of Project IDs.
    private volatile ImmutableMap<Long, List<Long>> projectCategoriesToProjects;

    private volatile ImmutableList<Long> projectsWithNoCategory;

    // List of all Projects
    private volatile ImmutableList<Project> allProjectObjects;

    public ProjectCache(ProjectManager projectManager, ProjectKeyStore projectKeyStore, final NodeAssociationStore nodeAssociationStore)
    {
        this.projectManager = projectManager;
        this.projectKeyStore = projectKeyStore;
        this.nodeAssociationStore = nodeAssociationStore;
        init();
    }

    private void init()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("ProjectCache.refresh");
        }

        final long start = System.currentTimeMillis();
        UtilTimerStack.push("ProjectCache.refresh");
        try
        {
            refreshProjectList();
            refreshProjectCategories();
            refreshCategoryProjectMappings();
            refreshProjectsWithNoCategory();
        }
        finally
        {
            UtilTimerStack.pop("ProjectCache.refresh");
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("ProjectCache.refresh took " + (System.currentTimeMillis() - start));
        }
    }

    /**
     * Refresh the list of projects
     * <p/>
     * IMPACT: Should perform only one SQL select statement
     */
    private void refreshProjectList()
    {
        final List<GenericValue> dbProjects = newArrayList(projectManager.getProjects());
        sort(dbProjects, PROJECT_NAME_COMPARATOR);

        final Map<Long, GenericValue> tmpById = Maps.newLinkedHashMap();
        final Map<String, GenericValue> tmpByCurrentKey = Maps.newLinkedHashMap();
        final List<Project> tmpAllProjects = Lists.newArrayListWithExpectedSize(dbProjects.size());

        for (GenericValue projectGV: dbProjects)
        {
            // Old-school GenericValue caches
            tmpById.put(projectGV.getLong("id"), projectGV);
            tmpByCurrentKey.put(projectGV.getString("key"), projectGV);
            // New School Project object cache
            tmpAllProjects.add(new ProjectImpl(projectGV));
        }

        // the caches are immutable, we recreate from scratch on refresh.
        projectsById = ImmutableMap.copyOf(tmpById);
        projectsByCurrentKey = ImmutableMap.copyOf(tmpByCurrentKey);
        projectsByCurrentKeyIgnoreCase = ImmutableSortedMap.copyOf(projectsByCurrentKey, String.CASE_INSENSITIVE_ORDER);
        allProjectObjects = ImmutableList.copyOf(tmpAllProjects);
    }

    protected void refreshProjectCategories()
    {
        List<GenericValue> dbCategories = delegator.findAll("ProjectCategory");
        sort(dbCategories, PROJECT_NAME_COMPARATOR);
        Map<Long, GenericValue> tmpById = new LinkedHashMap<Long, GenericValue>(dbCategories.size() * 2);
        for (final GenericValue projectCategory : dbCategories)
        {
            tmpById.put(projectCategory.getLong("id"), projectCategory);
        }

        projectCategories = ImmutableMap.copyOf(tmpById);
    }

    // PROJECT CACHING --------------------------------
    public GenericValue getProject(Long id)
    {
        return projectsById.get(id);
    }

    @Nullable
    public GenericValue getProjectByName(String name)
    {
        for (final GenericValue project : getProjects())
        {
            if (project.getString("name").equalsIgnoreCase(name))
            {
                return project;
            }
        }

        return null;
    }

    @Nullable
    public GenericValue getProjectByKey(String key)
    {
        // JDEV-24899 we want to avoid potential NPE if ProjectKey table contains keys referring to non-existent projects
        final Long projectId = projectKeyStore.getProjectId(key);
        return projectId != null ? getProject(projectId) : null;
    }

    public Collection<GenericValue> getProjects()
    {
        return projectsById.values();
    }

    /**
     * Returns a list of all Projects ordered by name.
     * @return a list of all Projects ordered by name.
     */
    public List<Project> getProjectObjects()
    {
        return allProjectObjects;
    }

    public Collection<GenericValue> getProjectCategories()
    {
        return projectCategories.values();
    }

    public GenericValue getProjectCategory(Long id)
    {
        return projectCategories.get(id);
    }

    private void refreshCategoryProjectMappings()
    {

        final Collection<GenericValue> categories = getProjectCategories();
        final Map<Long, List<Long>> tmpProjectCategoriesToProjects = Maps.newHashMapWithExpectedSize(categories.size());
        final Map<String, Long> tmpProjectToProjectCategories = new HashMap<String, Long>(64);
        for (final GenericValue category : categories)
        {
            try
            {
                List<GenericValue> projects = nodeAssociationStore.getSourcesFromSink(category, "Project", ProjectRelationConstants.PROJECT_CATEGORY);
                sort(projects, PROJECT_NAME_COMPARATOR);

                tmpProjectCategoriesToProjects.put(category.getLong("id"), getIdsFromGenericValues(projects));
                for (final GenericValue project : projects)
                {
                    tmpProjectToProjectCategories.put(project.getString("key"), category.getLong("id"));
                }
            }
            catch (DataAccessException ex)
            {
                LOG.error("Error getting projects for category " + category + ": " + ex, ex); //TODO: What should this really do?
            }
        }

        // Cached values are Immutable. We overwrite with a new cache on update.
        projectToProjectCategories = ImmutableMap.copyOf(tmpProjectToProjectCategories);
        projectCategoriesToProjects = ImmutableMap.copyOf(tmpProjectCategoriesToProjects);
    }

    private static List<Long> getIdsFromGenericValues(Collection<GenericValue> genericValues)
    {
        if (genericValues == null)
        {
            return Collections.emptyList();
        }
        return newArrayList(transform(genericValues, new Function<GenericValue, Long>()
        {
            @Nullable
            @Override
            public Long apply(@Nullable final GenericValue gv)
            {
                return (gv != null) ? gv.getLong("id") : null;
            }
        }));
    }

    private List<GenericValue> getProjectsFromProjectIds(Collection<Long> projectIds)
    {
        if (projectIds == null)
        {
            return Collections.emptyList();
        }
        return newArrayList(transform(projectIds, new Function<Long,GenericValue>()
        {
            @Nullable
            @Override
            public GenericValue apply(@Nullable final Long projectId)
            {
                return (projectId != null) ? getProject(projectId) : null;
            }
        }));
    }

    public Collection<GenericValue> getProjectsFromProjectCategory(GenericValue projectCat)
    {
        if (projectCat != null)
        {
            final List<Long> projectIds = projectCategoriesToProjects.get(projectCat.getLong("id"));
            if (projectIds != null)
            {
                return getProjectsFromProjectIds(projectIds);
            }
        }
        return Collections.emptyList();
    }



    @Nullable
    public GenericValue getProjectCategoryForProject(Project project)
    {
        return (project != null) ? getProjectCategoryForProjectKey(project.getKey()) : null;
    }

    @Nullable
    public GenericValue getProjectCategoryFromProject(GenericValue project)
    {
        return (project != null) ? getProjectCategoryForProjectKey(project.getString("key")) : null;
    }

    @Nullable
    private GenericValue getProjectCategoryForProjectKey(String projectKey)
    {
        final Long projectCategoryId = projectToProjectCategories.get(projectKey);
        return (projectCategoryId != null) ? getProjectCategory(projectCategoryId) : null;
    }

    public Collection<GenericValue> getProjectsWithNoCategory()
    {
        return getProjectsFromProjectIds(projectsWithNoCategory);
    }

    protected void refreshProjectsWithNoCategory()
    {
        final List<GenericValue> projectsWithNoCategoryGVs = new ArrayList<GenericValue>(64);
        for (final GenericValue project : getProjects())
        {
            if (getProjectCategoryFromProject(project) == null)
            {
                projectsWithNoCategoryGVs.add(project);
            }
        }

        //alphabetic order on the project name
        sort(projectsWithNoCategoryGVs, PROJECT_NAME_COMPARATOR);
        projectsWithNoCategory = ImmutableList.copyOf(getIdsFromGenericValues(projectsWithNoCategoryGVs));
    }

    public GenericValue getProjectByCurrentKeyIgnoreCase(final String projectKey)
    {
        return projectsByCurrentKeyIgnoreCase.get(projectKey);
    }

    @Nullable
    public GenericValue getProjectByKeyIgnoreCase(final String projectKey)
    {
        // JDEV-24899 we want to avoid potential NPE if ProjectKey table contains keys referring to non-existent projects
        final Long projectId = projectKeyStore.getProjectIdByKeyIgnoreCase(projectKey);
        return projectId != null ? getProject(projectId) : null;
    }

    public GenericValue getProjectByCurrentKey(final String projectKey)
    {
        return projectsByCurrentKey.get(projectKey);
    }

    public Set<String> getAllProjectKeys(final Long projectId)
    {
        return projectKeyStore.getProjectKeys(projectId);
    }
}
