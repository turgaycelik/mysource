/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.ofbiz.core.entity.EntityUtil.getOnly;

public abstract class AbstractSchemeManager implements SchemeManager
{
    private static final Logger LOG = Logger.getLogger(AbstractSchemeManager.class);

    static final String PROJECT_ENTITY_NAME = "Project";

    // TODO we need to rewrite this cache to store Scheme instances instead of GVs. Migration needs a performant way of navigating from Scheme to GV to support old calls
    private final Cache<Long, List<GenericValue>> projectSchemeCache;
    protected final ProjectManager projectManager;
    protected final SecurityTypeManager securityTypeManager;
    private final PermissionContextFactory permissionContextFactory;
    protected final SchemeFactory schemeFactory;
    private final NodeAssociationStore nodeAssociationStore;
    private final OfBizDelegator ofBizDelegator;
    protected final GroupManager groupManager;
    protected final EventPublisher eventPublisher;

    protected AbstractSchemeManager(final ProjectManager projectManager, final SecurityTypeManager securityTypeManager,
            final PermissionContextFactory permissionContextFactory, final SchemeFactory schemeFactory,
            final NodeAssociationStore nodeAssociationStore, final OfBizDelegator ofBizDelegator,
            final GroupManager groupManager, final EventPublisher eventPublisher, CacheManager cacheManager)
    {
        this.projectManager = projectManager;
        this.securityTypeManager = securityTypeManager;
        this.permissionContextFactory = permissionContextFactory;
        this.schemeFactory = schemeFactory;
        this.nodeAssociationStore = nodeAssociationStore;
        this.ofBizDelegator = ofBizDelegator;
        this.groupManager = groupManager;
        this.eventPublisher = eventPublisher;
        // We construct this cache using getClass().getName() as we want a separate cache instance for each subclass.
        projectSchemeCache = cacheManager.getCache(getClass().getName() + ".projectSchemeCache",
                new ProjectSchemeCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    public void onClearCache(final ClearCacheEvent event)
    {
        flushProjectSchemes();
    }

    public abstract String getSchemeEntityName();

    public abstract String getEntityName();

    public abstract String getSchemeDesc();

    public abstract String getDefaultNameKey();

    public abstract String getDefaultDescriptionKey();

    /**
     * Identifies whether this scheme manager makes its schemes associated with {@link
     * com.atlassian.jira.project.Project projects} or something else. This is here for historic reasons as schemes are
     * now always associated with projects. This means you should not override this.
     *
     * @return @{link SchemeManager#PROJECT_ASSOCIATION}
     * @deprecated Just assume all schemes are project association schemes.
     */
    @Deprecated
    public String getAssociationType()
    {
        return PROJECT_ASSOCIATION;
    }

    protected abstract GenericValue createSchemeEntityNoEvent(final GenericValue scheme, final SchemeEntity schemeEntity)
            throws GenericEntityException;

    @Override
    public GenericValue getScheme(final Long id)
    {
        return ofBizDelegator.findById(getSchemeEntityName(), id);
    }

    @Nullable
    @Override
    public Scheme getSchemeObject(final Long id) throws DataAccessException
    {
        return toScheme(getScheme(id));
    }

    @Nullable
    @Override
    public Scheme getSchemeObject(final String name) throws DataAccessException
    {
        return toScheme(getScheme(name));
    }

    @Override
    public GenericValue getScheme(final String name) throws DataAccessException
    {
        final FieldMap fields = FieldMap.build("name", name);
        return getOnly(ofBizDelegator.findByAnd(getSchemeEntityName(), fields));
    }

    @Override
    public List<GenericValue> getSchemes() throws DataAccessException
    {
        @SuppressWarnings ("unchecked")
        final List<GenericValue> schemes = ofBizDelegator.findAll(getSchemeEntityName());
        Collections.sort(schemes, OfBizComparators.NAME_COMPARATOR);
        return schemes;
    }

    @Override
    public List<Scheme> getSchemeObjects() throws DataAccessException
    {
        final List<GenericValue> schemeGvs = ofBizDelegator.findAll(getSchemeEntityName());
        Collections.sort(schemeGvs, OfBizComparators.NAME_COMPARATOR);
        return schemeFactory.getSchemes(schemeGvs);
    }

    @Override
    public List<Scheme> getAssociatedSchemes(final boolean withEntitiesComparable) throws DataAccessException
    {
        try
        {
            final List<GenericValue> schemes = getSchemes();
            final List<Scheme> associatedSchemes = new ArrayList<Scheme>(schemes.size());
            for (final GenericValue schemeGV : schemes)
            {
                if (getProjects(schemeGV).size() != 0)
                {
                    if (withEntitiesComparable)
                    {
                        associatedSchemes.add(schemeFactory.getSchemeWithEntitiesComparable(schemeGV));
                    }
                    else
                    {
                        associatedSchemes.add(schemeFactory.getScheme(schemeGV));
                    }
                }
            }
            return associatedSchemes;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public List<Scheme> getUnassociatedSchemes() throws DataAccessException
    {
        final List<Scheme> unassociatedSchemes = new ArrayList<Scheme>();
        try
        {
            final List<GenericValue> schemes = getSchemes();
            for (final GenericValue schemeGV : schemes)
            {
                if (getProjects(schemeGV).isEmpty())
                {
                    unassociatedSchemes.add(schemeFactory.getScheme(schemeGV));
                }
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        Collections.sort(unassociatedSchemes, new SchemeComparator());
        return unassociatedSchemes;
    }

    @Override
    public List<GenericValue> getSchemes(final GenericValue project) throws GenericEntityException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Cannot get schemes for null project");
        }
        //only cache project associations
        return projectSchemeCache.get(project.getLong("id"));
    }

    public Scheme getSchemeFor(Project project)
    {
        try
        {
            final List<GenericValue> schemes = getSchemes(project.getGenericValue());
            if (schemes.isEmpty())
            {
                return null;
            }
            if (schemes.size() > 1)
            {
                throw new IllegalStateException("Too many " + getSchemeEntityName() + " schemes found for Project " + project.getKey());
            }
            return schemeFactory.getScheme(schemes.iterator().next());
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }


    @Override
    public GenericValue getEntity(final Long id) throws GenericEntityException
    {
        return ofBizDelegator.findById(getEntityName(), id);
    }

    @Override
    public List<GenericValue> getEntities(final String type, final String parameter) throws GenericEntityException
    {
        @SuppressWarnings ("unchecked")
        final List<GenericValue> result = ofBizDelegator.findByAnd(getEntityName(),
                EasyMap.build("type", type, "parameter", parameter));
        return result;
    }

    @Override
    public List<GenericValue> getEntities(final GenericValue scheme) throws GenericEntityException
    {
        return scheme.getRelated("Child" + getEntityName());
    }

    @Override
    public boolean schemeExists(final String name)
    {
        return (getScheme(name) != null);
    }

    @Override
    public GenericValue createScheme(final String name, final String description) throws GenericEntityException
    {
        final GenericValue newScheme = createSchemeNoEvent(name, description);
        eventPublisher.publish(createSchemeCreatedEvent(schemeFactory.getScheme(newScheme)));
        return newScheme;
    }

    protected GenericValue createSchemeNoEvent(final String name, final String description) throws GenericEntityException
    {
        if (!schemeExists(name))
        {
            flushProjectSchemes();
            final GenericValue scheme = createSchemeGenericValue(MapBuilder.<String, Object>build("name", name, "description", description));
            return scheme;
        }
        else
        {
            throw new GenericEntityException("Could not create " + getSchemeDesc() + " Scheme with name:" + name + " as it already exists.");
        }
    }

    protected abstract AbstractSchemeEvent createSchemeCreatedEvent(final Scheme scheme);

    @Override
    public Scheme createSchemeObject(final String name, final String description)
    {
        try
        {
            return schemeFactory.getScheme(createScheme(name, description));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Scheme createSchemeAndEntities(final Scheme scheme) throws DataAccessException
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException();
        }

        GenericValue schemeGV;

        try
        {
            schemeGV = createScheme(scheme.getName(), scheme.getDescription());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        final List<GenericValue> entityGVs = new ArrayList<GenericValue>();
        // Now create all the schemes entities
        @SuppressWarnings ("unchecked")
        final Collection<SchemeEntity> schemeEntities = scheme.getEntities();
        for (final SchemeEntity schemeEntity : schemeEntities)
        {
            try
            {
                entityGVs.add(createSchemeEntity(schemeGV, schemeEntity));
            }
            catch (final GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }

        return schemeFactory.getScheme(schemeGV, entityGVs);
    }

    protected abstract AbstractSchemeUpdatedEvent createSchemeUpdatedEvent(final Scheme scheme, final Scheme originalScheme);

    @Override
    public void updateScheme(final GenericValue entity) throws GenericEntityException
    {
        final Long schemeId = entity.getLong("id");
        final Scheme originalScheme = getSchemeObject(schemeId);

        entity.store();
        flushProjectSchemes();

        eventPublisher.publish(createSchemeUpdatedEvent(getSchemeObject(schemeId), originalScheme));
    }

    @Override
    public void updateScheme(final Scheme scheme) throws DataAccessException
    {
        try
        {
            final GenericValue schemeEntity = getScheme(scheme.getId());
            schemeEntity.setString("name", scheme.getName());
            schemeEntity.setString("description", scheme.getDescription());
            updateScheme(schemeEntity);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void deleteScheme(final Long id) throws GenericEntityException
    {
        // We want to make sure we never delete a scheme that has an id of 0, this means the scheme is a
        // default (i.e. DefaultPermissionScheme, we always want one in JIRA). JRA-11705
        if ((id != null) && (id != 0))
        {
            final GenericValue scheme = getScheme(id);
            nodeAssociationStore.removeAssociationsFromSink(scheme);
            ofBizDelegator.removeRelated("Child" + getEntityName(), scheme);
            ofBizDelegator.removeValue(scheme);
            flushProjectSchemes();
        }
    }

    @Deprecated
    @Override
    public void addSchemeToProject(final GenericValue project, final GenericValue scheme) throws GenericEntityException
    {
        addSchemeToProject(projectManager.getProjectObj(project.getLong("id")), schemeFactory.getScheme(scheme));
    }

    @Override
    public void addSchemeToProject(final Project project, final Scheme scheme) throws DataAccessException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("The project passed can not be null.");
        }
        if (scheme == null)
        {
            throw new IllegalArgumentException("The scheme passed can not be null.");
        }

        try
        {
            final List<GenericValue> schemes = getSchemes(project.getGenericValue());
            final GenericValue schemeGV = getScheme(scheme.getId());
            if (!schemes.contains(schemeGV))
            {
                nodeAssociationStore.createAssociation(project.getGenericValue(), schemeGV, getAssociationType());
            }
            flushProjectSchemes();

            eventPublisher.publish(createSchemeAddedToProjectEvent(scheme, project));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Nonnull
    protected abstract AbstractSchemeAddedToProjectEvent createSchemeAddedToProjectEvent(final Scheme scheme, final Project project);

    @Override
    public void removeSchemesFromProject(final Project project) throws DataAccessException
    {
        try
        {
            removeSchemesFromProject(project.getGenericValue());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Deprecated
    @Override
    public void removeSchemesFromProject(final GenericValue project) throws GenericEntityException
    {
        final List<GenericValue> schemes = getSchemes(project);
        for (final GenericValue scheme : schemes)
        {
            nodeAssociationStore.removeAssociation(project, scheme, getAssociationType());

            eventPublisher.publish(createSchemeRemovedFromProjectEvent(schemeFactory.getScheme(scheme), projectManager.getProjectObj(project.getLong("id"))));
        }
        flushProjectSchemes();
    }

    @Nonnull
    protected abstract AbstractSchemeRemovedFromProjectEvent createSchemeRemovedFromProjectEvent(
            final Scheme scheme, final Project project);

    @Override
    public void deleteEntity(final Long id) throws DataAccessException
    {
        try
        {
            final GenericValue entity = getEntity(id);

            entity.remove();

            final Object event = createSchemeEntityDeletedEvent(entity);
            if (event != null)
            {
                eventPublisher.publish(event);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    protected abstract SchemeEntity makeSchemeEntity(GenericValue schemeEntityGV);

    @Nullable
    protected abstract Object createSchemeEntityDeletedEvent(final GenericValue entity);

    @Deprecated
    @Override
    public List<GenericValue> getProjects(final GenericValue scheme) throws GenericEntityException
    {

        // it is faster to go the project manager (which is presumably cached) than to load it via association manager
        // if we ever do database joins, then we may be able to remove this
        final List<Long> projectIds = nodeAssociationStore.getSourceIdsFromSink(scheme, "Project", getAssociationType());
        final List<GenericValue> projects = new ArrayList<GenericValue>(projectIds.size());
        for (final Long projectId : projectIds)
        {
            projects.add(projectManager.getProject(projectId));
        }
        Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
    }

    @Override
    public List<Project> getProjects(final Scheme scheme) throws DataAccessException
    {
        if ((scheme == null) || (scheme.getId() == null))
        {
            throw new IllegalArgumentException("The scheme and the schemes id can not be null");
        }

        try
        {
            final GenericValue schemeGV = getScheme(scheme.getId());
            return new ArrayList<Project>(ComponentAccessor.getProjectFactory().getProjects(getProjects(schemeGV)));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue createDefaultScheme() throws GenericEntityException
    {
        if (getDefaultScheme() == null)
        {
            return createSchemeGenericValue(MapBuilder.<String, Object>build("id", 0L, "name", getI18nTextWithDefaultNullCheck(getDefaultNameKey(),
                    "Default " + getSchemeDesc() + " Scheme"), "description", getI18nTextWithDefaultNullCheck(getDefaultDescriptionKey(),
                    "This is the default " + getSchemeDesc() + " Scheme. Any new projects that are created will be assigned this scheme")));
        }
        else
        {
            return getDefaultScheme();
        }
    }

    @Override
    public boolean removeEntities(final String type, final String parameter) throws RemoveException
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Type passed must not be null");
        }

        if (parameter == null)
        {
            throw new IllegalArgumentException("Parameter passed must not be null");
        }

        try
        {
            final List<GenericValue> entities = getEntities(type, parameter);
            ofBizDelegator.removeAll(entities);
            return true;
        }
        catch (final GenericEntityException e)
        {
            throw new RemoveException(e);
        }
    }

    @Override
    public boolean removeEntities(final GenericValue scheme, final Long entityTypeId) throws RemoveException
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Scheme passed to this function must not be NULL");
        }

        try
        {
            final List<GenericValue> entities = getEntities(scheme, entityTypeId);
            ofBizDelegator.removeAll(entities);
            return true;
        }
        catch (final GenericEntityException e)
        {
            throw new RemoveException(e);
        }
    }

    @Override
    public GenericValue getDefaultScheme() throws GenericEntityException
    {
        return ofBizDelegator.findById(getSchemeEntityName(), 0L);
    }

    @Override
    public Scheme getDefaultSchemeObject()
    {
        try
        {
            final GenericValue defaultSchemeGV = getDefaultScheme();
            if (defaultSchemeGV == null)
            {
                return null;
            }
            return schemeFactory.getScheme(defaultSchemeGV);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void addDefaultSchemeToProject(final GenericValue project) throws GenericEntityException
    {
        final GenericValue scheme = getDefaultScheme();

        if (scheme != null)
        {
            final List<GenericValue> schemes = getSchemes(project);
            if (!schemes.contains(scheme))
            {
                nodeAssociationStore.createAssociation(project, scheme, getAssociationType());
            }
        }
        flushProjectSchemes();
    }

    @Override
    public void addDefaultSchemeToProject(final Project project) throws DataAccessException
    {
        try
        {
            addDefaultSchemeToProject(project.getGenericValue());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue copyScheme(final GenericValue oldScheme) throws GenericEntityException
    {
        if (oldScheme == null)
        {
            return null;
        }

        final String name = getNameForCopy(oldScheme.getString("name"), null);

        final GenericValue newScheme = createSchemeNoEvent(name, oldScheme.getString("description"));
        final List<GenericValue> origEntities = getEntities(oldScheme);
        for (final GenericValue entity : origEntities)
        {
            createSchemeEntityNoEvent(newScheme, makeSchemeEntity(entity));
        }

        eventPublisher.publish(createSchemeCopiedEvent(schemeFactory.getScheme(oldScheme), schemeFactory.getScheme(newScheme)));

        return newScheme;
    }

    public String getNameForCopy(String originalName, Integer abbreviateTo)
    {
        final I18nHelper i18n = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
        String name = i18n.getText("common.words.copyof", originalName);
        if (abbreviateTo != null)
        {
            name = abbreviate(name, abbreviateTo);
        }

        // check if the scheme already exists, and if it does, add a number to the name
        int j = 2;
        while (schemeExists(name))
        {
            name = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyxof",
                    String.valueOf(j++), originalName);
            if (abbreviateTo != null)
            {
                name = abbreviate(name, abbreviateTo);
            }
        }
        return name;
    }

    @Nonnull
    protected abstract AbstractSchemeCopiedEvent createSchemeCopiedEvent(@Nonnull Scheme oldScheme, @Nonnull Scheme newScheme);

    @Override
    public Scheme copyScheme(Scheme oldScheme)
    {
        try
        {
            final GenericValue newSchemeGv = copyScheme(getScheme(oldScheme.getId()));
            return schemeFactory.getScheme(newSchemeGv);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    protected void flushProjectSchemes()
    {
        projectSchemeCache.removeAll();
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final Project project)
    {
        return getGroups(entityTypeId, project.getGenericValue());
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final GenericValue project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed can NOT be null");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("Project passed must be a project not a " + project.getEntityName());
        }

        final Set<Group> groups = new HashSet<Group>();
        try
        {
            final List<GenericValue> schemes = getSchemes(project);
            for (final GenericValue scheme : schemes)
            {
                final List<GenericValue> entity = getEntities(scheme, GroupDropdown.DESC, entityTypeId);
                for (final GenericValue permission : entity)
                {
                    groups.add(groupManager.getGroup(permission.getString("parameter")));
                }
            }
        }
        catch (final GenericEntityException e)
        {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        return groups;
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final GenericValue projectOrIssue)
    {
        return getUsers(permissionId, permissionContextFactory.getPermissionContext(projectOrIssue));
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final Project project)
    {
        return getUsers(permissionId, permissionContextFactory.getPermissionContext(project));
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final Issue issue)
    {
        return getUsers(permissionId, permissionContextFactory.getPermissionContext(issue));
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final PermissionContext ctx)
    {
        final Set<User> users = new HashSet<User>();

        final Map<String, SecurityType> permTypes = securityTypeManager.getTypes();
        try
        {
            final List<GenericValue> schemes = getSchemes(ctx.getProject());
            for (final GenericValue scheme : schemes)
            {
                final List<GenericValue> entities = getEntities(scheme, permissionId);

                for (final GenericValue entity : entities)
                {
                    final SecurityType secType = permTypes.get(entity.getString("type"));
                    if (secType != null)
                    {
                        try
                        {
                            final Set<User> usersToAdd = secType.getUsers(ctx, entity.getString("parameter"));
                            for (User user : usersToAdd)
                            {
                                if (user.isActive())
                                {
                                    users.add(user);
                                }
                            }
                        }
                        catch (final IllegalArgumentException e)
                        {
                            // If the entered custom field id is incorrect
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        catch (final GenericEntityException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return users;
    }

    protected GenericValue createSchemeGenericValue(final Map<String, Object> values) throws GenericEntityException
    {
        return ofBizDelegator.createValue(getSchemeEntityName(), values);
    }

    private String getI18nTextWithDefaultNullCheck(final String key, final String defaultResult)
    {
        if (key == null)
        {
            return defaultResult;
        }
        final String result = getApplicationI18n().getText(key);
        if (result.equals(key))
        {
            return defaultResult;
        }
        else
        {
            return result;
        }
    }

    protected I18nHelper getApplicationI18n()
    {
        return new I18nBean();
    }

    @Nullable
    private Scheme toScheme(GenericValue schemeGv)
    {
        return (schemeGv != null) ? schemeFactory.getScheme(schemeGv) : null;
    }

    private class ProjectSchemeCacheLoader implements CacheLoader<Long, List<GenericValue>>
    {
        @Override
        public List<GenericValue> load(@Nonnull final Long projectId)
        {
            final List<GenericValue> sinkFromSource = nodeAssociationStore.getSinksFromSource(PROJECT_ENTITY_NAME, projectId, getSchemeEntityName(),
                        getAssociationType());
            return ImmutableList.copyOf(sinkFromSource);
        }
    }
}
