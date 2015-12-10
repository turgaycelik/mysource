package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.map.CacheObject;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@EventComponent
public class DefaultIssueTypeScreenSchemeManager implements IssueTypeScreenSchemeManager
{
    private final OfBizDelegator ofBizDelegator;
    private final ConstantsManager constantsManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final NodeAssociationStore nodeAssociationStore;

    // Caches the actual scheme object using scheme id as a key
    private final Cache<Long,CacheObject<Long>> projectAssociationCache;
    private final Cache<Long, CacheObject<IssueTypeScreenScheme>> schemeCache;

    public DefaultIssueTypeScreenSchemeManager(final OfBizDelegator ofBizDelegator, final ConstantsManager constantsManager,
            final FieldScreenSchemeManager fieldScreenSchemeManager, final NodeAssociationStore nodeAssociationStore, final CacheManager cacheManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.constantsManager = constantsManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.nodeAssociationStore = nodeAssociationStore;

        projectAssociationCache = cacheManager.getCache(DefaultIssueTypeScreenSchemeManager.class.getName() + ".projectAssociationCache",
                new ProjectAssociationCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        schemeCache = cacheManager.getCache(DefaultIssueTypeScreenSchemeManager.class.getName() + ".schemeCache",
                new IssueTypeScreenSchemeCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes()
    {
        return buildIssueTypeScreenSchemes(ofBizDelegator.findAll(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, EasyList.build("name")));
    }

    private Collection<IssueTypeScreenScheme> buildIssueTypeScreenSchemes(final List<GenericValue> issueTypeScreenSchemeGVs)
    {
        final List<IssueTypeScreenScheme> issueTypeScreenSchemes = new LinkedList<IssueTypeScreenScheme>();
        for (final Object element : issueTypeScreenSchemeGVs)
        {
            issueTypeScreenSchemes.add(buildIssueTypeScreenScheme((GenericValue) element));
        }
        return issueTypeScreenSchemes;
    }

    protected IssueTypeScreenScheme buildIssueTypeScreenScheme(final GenericValue genericValue)
    {
        return new IssueTypeScreenSchemeImpl(this, genericValue);
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme(final Long id)
    {
        return schemeCache.get(id).getValue();
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final GenericValue project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        final Long schemeId = projectAssociationCache.get(getProjectId(project)).getValue();
        if (schemeId != null)
        {
            return getIssueTypeScreenScheme(schemeId);
        }
        else
        {
            return null;
        }
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(Project project)
    {
        return getIssueTypeScreenScheme(project.getGenericValue());
    }

    public FieldScreenScheme getFieldScreenScheme(final Issue issue)
    {
        final Project project = issue.getProjectObject();
        if (project == null)
        {
            throw new RuntimeException("Issue '" + issue + "' has no project");
        }
        final IssueTypeScreenScheme issueTypeScreenScheme = getIssueTypeScreenScheme(project.getGenericValue());
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issue.getIssueTypeObject().getId());
        if (issueTypeScreenSchemeEntity == null)
        {
            // Try default entry
            issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(null);
            if (issueTypeScreenSchemeEntity == null)
            {
                throw new IllegalStateException("No default entity for issue type screen scheme with id '" + issueTypeScreenScheme.getId() + "'.");
            }
        }

        return issueTypeScreenSchemeEntity.getFieldScreenScheme();
    }

    public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        List issueTypeScreenSchemeEntities = new LinkedList();
        List<GenericValue> issueTypeScreenSchemeEntitymGVs = getOfBizDelegator().findByAnd(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME, EasyMap.build("scheme", issueTypeScreenScheme.getId()));
        for (final GenericValue issueTypeScreenSchemeEntityGV : issueTypeScreenSchemeEntitymGVs)
        {
            IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = buildIssueTypeScreenSchemeEntity(issueTypeScreenSchemeEntityGV);
            issueTypeScreenSchemeEntity.setIssueTypeScreenScheme(issueTypeScreenScheme);
            issueTypeScreenSchemeEntities.add(issueTypeScreenSchemeEntity);
        }

        return issueTypeScreenSchemeEntities;
    }

    protected IssueTypeScreenSchemeEntity buildIssueTypeScreenSchemeEntity(final GenericValue genericValue)
    {
        final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(this, genericValue,
            fieldScreenSchemeManager, constantsManager);
        issueTypeScreenSchemeEntity.setIssueTypeId(genericValue.getString("issuetype"));
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(genericValue.getLong("fieldscreenscheme")));
        return issueTypeScreenSchemeEntity;
    }

    public void createIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        // Used by upgarde tasks - so needs to stay here
        final Map params = EasyMap.build("name", issueTypeScreenScheme.getName(), "description", issueTypeScreenScheme.getDescription());
        if (issueTypeScreenScheme.getId() != null)
        {
            params.put("id", issueTypeScreenScheme.getId());
        }

        final GenericValue fieldScreenSchemeGV = ofBizDelegator.createValue(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, params);
        issueTypeScreenScheme.setGenericValue(fieldScreenSchemeGV);
        schemeCache.remove(issueTypeScreenScheme.getId());
    }

    public void updateIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        // Used by upgarde tasks - so needs to stay here
        ofBizDelegator.store(issueTypeScreenScheme.getGenericValue());
        schemeCache.remove(issueTypeScreenScheme.getId());
    }

    public void removeIssueTypeSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        getOfBizDelegator().removeByAnd(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME, EasyMap.build("scheme", issueTypeScreenScheme.getId()));
    }

    public void removeIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        getOfBizDelegator().removeValue(issueTypeScreenScheme.getGenericValue());
        schemeCache.remove(issueTypeScreenScheme.getId());
    }

    public void createIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        final String issueTypeId = issueTypeScreenSchemeEntity.getIssueTypeId();
        final GenericValue issueTypeScreenSchemeEntityGV = ofBizDelegator.createValue(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME, MapBuilder.<String, Object>build(
                "issuetype", issueTypeId, "fieldscreenscheme", issueTypeScreenSchemeEntity.getFieldScreenScheme().getId(), "scheme",
                issueTypeScreenSchemeEntity.getIssueTypeScreenScheme().getId()));
        issueTypeScreenSchemeEntity.setGenericValue(issueTypeScreenSchemeEntityGV);
        schemeCache.remove(issueTypeScreenSchemeEntity.getFieldScreenScheme().getId());
    }

    public void updateIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        ofBizDelegator.store(issueTypeScreenSchemeEntity.getGenericValue());
        schemeCache.remove(issueTypeScreenSchemeEntity.getFieldScreenScheme().getId());
    }

    public void removeIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        ofBizDelegator.removeValue(issueTypeScreenSchemeEntity.getGenericValue());
        schemeCache.remove(issueTypeScreenSchemeEntity.getIssueTypeScreenScheme().getId());
    }

    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes(final FieldScreenScheme fieldScreenScheme)
    {
        final List<IssueTypeScreenScheme> issueTypeScreenSchemes = new LinkedList<IssueTypeScreenScheme>();
        final Set<Long> issueTypeScreenSchemeIds = new HashSet<Long>();
        final List<GenericValue> issueTypeScreenSchemeEntityGVs = ofBizDelegator.findByAnd(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME,
            EasyMap.build("fieldscreenscheme", fieldScreenScheme.getId()));
        for (final GenericValue issueTypeScreenSchemeEntityGV : issueTypeScreenSchemeEntityGVs)
        {
            issueTypeScreenSchemeIds.add(issueTypeScreenSchemeEntityGV.getLong("scheme"));
        }

        for (final Long element : issueTypeScreenSchemeIds)
        {
            issueTypeScreenSchemes.add(getIssueTypeScreenScheme(element));
        }

        return issueTypeScreenSchemes;
    }

    @Override
    public void addSchemeAssociation(final GenericValue project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        try
        {
        // Get old association
            final IssueTypeScreenScheme oldIssueTypeScreenScheme = getIssueTypeScreenScheme(project);
            if (oldIssueTypeScreenScheme != null)
            {
                // Only do anything if the schemes are different
                if (!oldIssueTypeScreenScheme.equals(issueTypeScreenScheme))
                {
                    // Remove old association
                    removeSchemeAssociation(project, oldIssueTypeScreenScheme);
                    if ((issueTypeScreenScheme != null) && (issueTypeScreenScheme.getGenericValue() != null))
                    {
                        nodeAssociationStore.createAssociation(project, issueTypeScreenScheme.getGenericValue(), SchemeManager.PROJECT_ASSOCIATION);
                    }
                }
            }
            else
            {
                if ((issueTypeScreenScheme != null) && (issueTypeScreenScheme.getGenericValue() != null))
                {
                    nodeAssociationStore.createAssociation(project, issueTypeScreenScheme.getGenericValue(), SchemeManager.PROJECT_ASSOCIATION);
                }
            }
        }
        finally
        {
            projectAssociationCache.remove(getProjectId(project));
        }
    }

    @Override
    public void addSchemeAssociation(Project project, IssueTypeScreenScheme issueTypeScreenScheme)
    {
        addSchemeAssociation(project.getGenericValue(), issueTypeScreenScheme);
    }

    @Override
    public void removeSchemeAssociation(final GenericValue project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        try
        {
            if ((issueTypeScreenScheme != null) && (issueTypeScreenScheme.getGenericValue() != null))
            {
                nodeAssociationStore.removeAssociation(project, issueTypeScreenScheme.getGenericValue(), SchemeManager.PROJECT_ASSOCIATION);
            }
        }
        finally
        {
            projectAssociationCache.remove(getProjectId(project));
        }
    }

    @Override
    public void removeSchemeAssociation(Project project, IssueTypeScreenScheme issueTypeScreenScheme)
    {
        removeSchemeAssociation(project.getGenericValue(), issueTypeScreenScheme);
    }

    public Collection<GenericValue> getProjects(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        final List<GenericValue> projects = nodeAssociationStore.getSourcesFromSink(issueTypeScreenScheme.getGenericValue(), "Project",
                SchemeManager.PROJECT_ASSOCIATION);
        Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
    }

    @Override
    public void associateWithDefaultScheme(final GenericValue project)
    {
        addSchemeAssociation(project, getDefaultScheme());
    }

    @Override
    public void associateWithDefaultScheme(Project project)
    {
        associateWithDefaultScheme(project.getGenericValue());
    }

    public IssueTypeScreenScheme getDefaultScheme()
    {
        return getIssueTypeScreenScheme(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);
    }

    public void refresh()
    {
        schemeCache.removeAll();
        projectAssociationCache.removeAll();
    }

    protected OfBizDelegator getOfBizDelegator()
    {
        return ofBizDelegator;
    }

    private class ProjectAssociationCacheLoader implements CacheLoader<Long, CacheObject<Long>>
    {
        @Override
        public CacheObject<Long> load(@Nonnull final Long projectId)
        {
            final GenericValue issueTypeScreenSchemeGV = EntityUtil.getOnly(nodeAssociationStore.getSinksFromSource("Project", projectId,
                    ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, SchemeManager.PROJECT_ASSOCIATION));
            if (issueTypeScreenSchemeGV != null)
            {
                return CacheObject.wrap(buildIssueTypeScreenScheme(issueTypeScreenSchemeGV).getId());
            }
            return CacheObject.NULL();
        }
    }

    public Long getProjectId(final GenericValue project)
    {
        return project == null ? null : project.getLong("id");
    }
    private class IssueTypeScreenSchemeCacheLoader implements CacheLoader<Long, CacheObject<IssueTypeScreenScheme>>
    {
        @Override
        public CacheObject<IssueTypeScreenScheme> load(@Nonnull final Long id)
        {
            final GenericValue issueTypeScreenSchemeGV = getOfBizDelegator().findById(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, id);
            if (issueTypeScreenSchemeGV != null)
            {
                return CacheObject.wrap(buildIssueTypeScreenScheme(issueTypeScreenSchemeGV));
            }
            return CacheObject.NULL();
        }
    }
}
