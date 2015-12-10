/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

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
import com.atlassian.jira.event.permission.PermissionAddedEvent;
import com.atlassian.jira.event.permission.PermissionDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeAddedToProjectEvent;
import com.atlassian.jira.event.permission.PermissionSchemeCopiedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeCreatedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.permission.PermissionSchemeUpdatedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getIdAsLong;
import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getKey;
import static com.google.common.collect.Maps.newHashMap;

/**
 * This class is used to handle Permission Schemes.
 * <p>
 * Permission Schemes are created, removed and edited through this class
 */
public class DefaultPermissionSchemeManager extends AbstractSchemeManager implements PermissionSchemeManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultPermissionSchemeManager.class);

    @VisibleForTesting
    static final String SCHEME_ENTITY_NAME = "PermissionScheme";
    private static final String PERMISSION_ENTITY_NAME = "SchemePermissions";
    private static final String SCHEME_DESC = "Permission";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.permissions.default";
    private static final String DEFAULT_DESC_KEY = "admin.schemes.permissions.default.desc";

    private final Cache<Long, SchemeEntityCacheEntry> schemeEntityCache;

    private final PermissionTypeManager permissionTypeManager;
    private final OfBizDelegator delegator;

    public DefaultPermissionSchemeManager(final ProjectManager projectManager, final PermissionTypeManager permissionTypeManager,
            final PermissionContextFactory permissionContextFactory, final OfBizDelegator delegator,
            final SchemeFactory schemeFactory, final NodeAssociationStore nodeAssociationStore, final GroupManager groupManager,
            final EventPublisher eventPublisher, final CacheManager cacheManager)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, schemeFactory,
                nodeAssociationStore, delegator, groupManager, eventPublisher, cacheManager);
        this.permissionTypeManager = permissionTypeManager;
        this.delegator = delegator;
        schemeEntityCache = cacheManager.getCache(DefaultPermissionSchemeManager.class.getName() + ".schemeEntityCache",
                new SchemeEntityCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    /**
     * Registers this CachingFieldConfigContextPersister's cache in the JIRA instrumentation.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception
    {
    }

    @Override
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
        flushSchemeEntities();
    }

    @Override
    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    @Override
    public String getEntityName()
    {
        return PERMISSION_ENTITY_NAME;
    }

    @Override
    public String getSchemeDesc()
    {
        return SCHEME_DESC;
    }

    @Override
    public String getDefaultNameKey()
    {
        return DEFAULT_NAME_KEY;
    }

    @Override
    public String getDefaultDescriptionKey()
    {
        return DEFAULT_DESC_KEY;
    }

    @Override
    protected AbstractSchemeEvent createSchemeCreatedEvent(final Scheme scheme)
    {
        return new PermissionSchemeCreatedEvent(scheme);
    }

    @Nonnull
    @Override
    protected AbstractSchemeCopiedEvent createSchemeCopiedEvent(@Nonnull final Scheme oldScheme, @Nonnull final Scheme newScheme)
    {
        return new PermissionSchemeCopiedEvent(oldScheme, newScheme);
    }

    @Override
    protected AbstractSchemeUpdatedEvent createSchemeUpdatedEvent(final Scheme scheme, final Scheme originalScheme)
    {
        return new PermissionSchemeUpdatedEvent(scheme, originalScheme);
    }

    @Override
    public void deleteScheme(Long id) throws GenericEntityException
    {
        final Scheme scheme = getSchemeObject(id);

        super.deleteScheme(id);

        eventPublisher.publish(new PermissionSchemeDeletedEvent(id, scheme.getName()));
    }

    @Override
    protected AbstractSchemeAddedToProjectEvent createSchemeAddedToProjectEvent(final Scheme scheme, final Project project)
    {
        return new PermissionSchemeAddedToProjectEvent(scheme, project);
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final Long permissionId) throws GenericEntityException
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return getEntities(scheme, permissionKey);
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @param parameter    The permission parameter (group name etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final Long permissionId, final String parameter) throws GenericEntityException
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return getEntities(scheme, permissionKey, parameter);
    }

    public List<GenericValue> getEntities(GenericValue scheme, ProjectPermissionKey permissionKey, String parameter) throws GenericEntityException
    {
        return EntityUtil.filterByAnd(getEntities(scheme, permissionKey), Collections.singletonMap("parameter", parameter));
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @param parameter    The permission parameter (group name etc)
     * @param type         The type of the permission(Group, Current Reporter etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final Long permissionId, final String type, final String parameter) throws GenericEntityException
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return getEntities(scheme, permissionKey, type, parameter);
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, ProjectPermissionKey permissionKey, String type, String parameter) throws GenericEntityException
    {
        return EntityUtil.filterByAnd(getEntities(scheme, permissionKey), MapBuilder.build("type", type, "parameter", parameter));
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param type         The type of the permission(Group, Current Reporter etc)
     * @param permissionId The Id of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final String type, final Long permissionId) throws GenericEntityException
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return getEntitiesByType(scheme, permissionKey, type);
    }

    @Override
    public List<GenericValue> getEntitiesByType(GenericValue scheme, ProjectPermissionKey permissionKey, String type) throws GenericEntityException
    {
        return EntityUtil.filterByAnd(getEntities(scheme, permissionKey), Collections.singletonMap("type", type));
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, ProjectPermissionKey permissionKey) throws GenericEntityException
    {
        return getEntities(scheme, permissionKey.permissionKey());
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, String permissionKey) throws GenericEntityException
    {
        Long key = getSchemeEntityCacheKey(scheme);
        List<GenericValue> genericValues = schemeEntityCache.get(key).getCacheByPermission().get(permissionKey);
        if (genericValues == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(genericValues);
    }

    /**
     * Create a new permission record in the database
     *
     * @param scheme       The scheme that the permission record is associated with
     * @param schemeEntity The scheme entity object that is to be added to the scheme
     * @return The permission object
     * @throws GenericEntityException
     */
    @Override
    public GenericValue createSchemeEntity(final GenericValue scheme, final SchemeEntity schemeEntity) throws GenericEntityException
    {
        final GenericValue result = createSchemeEntityNoEvent(scheme, schemeEntity);

        SchemeEntity entityToPublish = withFixedEntityTypeId(schemeEntity);
        if (entityToPublish != null)
        {
            eventPublisher.publish(new PermissionAddedEvent(scheme.getLong("id"), entityToPublish));
        }

        return result;
    }

    private SchemeEntity withFixedEntityTypeId(SchemeEntity original)
    {
        if (!(original.getEntityTypeId() instanceof ProjectPermissionKey))
        {
            return original;
        }

        ProjectPermissionKey permissionKey = (ProjectPermissionKey) original.getEntityTypeId();

        // This will be null for non-system permissions, but at the moment only system permissions are used.
        Long permissionId = getIdAsLong(permissionKey);
        if (permissionId == null)
        {
            return null;
        }

        return new SchemeEntity(original.getId(), original.getType(), original.getParameter(),
                permissionId, original.getTemplateId(), original.getSchemeId());
    }

    protected GenericValue createSchemeEntityNoEvent(final GenericValue scheme, final SchemeEntity schemeEntity) throws GenericEntityException
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Scheme passed must NOT be null");
        }

        if (schemeEntity.getType() == null)
        {
            throw new IllegalArgumentException("Type in SchemeEntity can NOT be null");
        }

        ProjectPermissionKey permissionKey = getPermissionKeyEntityTypeId(schemeEntity);

        final GenericValue perm = EntityUtils.createValue(PERMISSION_ENTITY_NAME,
                MapBuilder.<String, Object>newBuilder("scheme", scheme.getLong("id"))
                .add("permissionKey", permissionKey.permissionKey())
                .add("type", schemeEntity.getType())
                .add("parameter", schemeEntity.getParameter()).toMap());

        schemeEntityCache.remove(getSchemeEntityCacheKey(scheme));

        return perm;
    }

    private ProjectPermissionKey getPermissionKeyEntityTypeId(SchemeEntity schemeEntity)
    {
        Object entityTypeId = schemeEntity.getEntityTypeId();

        if (entityTypeId instanceof ProjectPermissionKey)
        {
            return (ProjectPermissionKey) entityTypeId;
        }
        if (entityTypeId instanceof String)
        {
            return new ProjectPermissionKey((String) entityTypeId);
        }
        if (entityTypeId instanceof Long)
        {
            return getKey((Long) entityTypeId);
        }
        if (entityTypeId instanceof Integer)
        {
            return getKey((Integer) entityTypeId);
        }

        throw new IllegalArgumentException("Permission scheme IDs must be ProjectPermissionKey, String, long or int values, not: " + entityTypeId.getClass());
    }

    /**
     * Deletes a permission from the database
     *
     * @param id The id of the permission to be deleted
     */
    @Override
    public void deleteEntity(final Long id) throws DataAccessException
    {
        super.deleteEntity(id);

        flushSchemeEntities();
    }

    @Override
    protected AbstractSchemeEntityEvent createSchemeEntityDeletedEvent(final GenericValue entity)
    {
        SchemeEntity entityToPublish = withFixedEntityTypeId(makeSchemeEntity(entity));
        return (entityToPublish == null) ? null : new PermissionDeletedEvent(entity.getLong("scheme"), entityToPublish);
    }

    @Override
    protected SchemeEntity makeSchemeEntity(final GenericValue entity)
    {
        ProjectPermissionKey permissionKey = new ProjectPermissionKey(entity.getString("permissionKey"));
        SchemeEntity schemeEntity = new SchemeEntity(entity.getString("type"), entity.getString("parameter"), permissionKey);
        schemeEntity.setSchemeId(entity.getLong("scheme"));
        return schemeEntity;
    }

    @Override
    public List<GenericValue> getEntities(final GenericValue scheme) throws GenericEntityException
    {
        final Long key = getSchemeEntityCacheKey(scheme);
        return Collections.unmodifiableList(schemeEntityCache.get(key).getCache());
    }

    /**
     * Removes all scheme entities with this parameter
     *
     * @param type      the type of scheme entity you wish to remove 'user', 'group', 'projectrole'
     * @param parameter must NOT be null
     */
    @Override
    public boolean removeEntities(final String type, final String parameter) throws RemoveException
    {
        final boolean result = super.removeEntities(type, parameter);
        flushSchemeEntities();
        return result;
    }

    /**
     * Retrieves all the entites for this permission and then removes them.
     *
     * @param scheme       to remove entites from must NOT be null
     * @param permissionId to remove must NOT be a global permission
     * @return True is all the entities are removed
     * @throws RemoveException
     */
    @Override
    public boolean removeEntities(final GenericValue scheme, final Long permissionId) throws RemoveException
    {
        if (Permissions.isGlobalPermission(permissionId.intValue()))
        {
            throw new IllegalArgumentException("PermissionId passed must not be a global permissions " + permissionId.toString() + " is global");
        }

        final boolean result = super.removeEntities(scheme, permissionId);
        schemeEntityCache.remove(getSchemeEntityCacheKey(scheme));
        return result;
    }

    /**
     * Checks to see if there is an anyone permission for that permission type.
     * Specific permissions can things such as Current Reporter, Project Lead, Single User etc.
     *
     * @param permissionId The permission to check against, must not be global permission
     * @param project      The entity to check for the permission. This entity must be a project
     * @return true if the user has the permission otherwise false
     */
    @Override
    public boolean hasSchemeAuthority(final Long permissionId, final GenericValue project)
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return hasSchemeAuthority(permissionKey, project);
    }

    /**
     * Checks to see if there is an anyone permission for that permission type.
     * Specific permissions can things such as Current Reporter, Project Lead, Single User etc.
     *
     * @param permissionKey The permission to check against, must not be global permission
     * @param project      The entity to check for the permission. This entity must be a project
     * @return true if the user has the permission otherwise false
     */
    @Override
    public boolean hasSchemeAuthority(ProjectPermissionKey permissionKey, GenericValue project)
    {
        return hasPermission(permissionKey, project, null, false);
    }

    /**
     * Checks to see if the user has any specific permissions for that permission type.
     * Specific permissions can things such as Current Reporter, Project Lead, Single User etc.
     *
     * @param permissionId  The permission to check against, must not be global permission
     * @param project       The entity to check for the permission. This entity must be a project
     * @param user          The user to check for the permission. The user must NOT be null
     * @param issueCreation true if this call is for a "Create Issue" permission.
     * @return true if the user has the permission otherwise false
     */
    @Override
    public boolean hasSchemeAuthority(final Long permissionId, final GenericValue project, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return hasSchemeAuthority(permissionKey, project, user, issueCreation);
    }

    @Override
    public boolean hasSchemeAuthority(ProjectPermissionKey permissionKey, final GenericValue project, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("The user passed must not be null");
        }

        return hasPermission(permissionKey, project, user, issueCreation);
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final Project project)
    {
        ProjectPermissionKey permissionKey = getKey(entityTypeId);
        return getGroups(permissionKey, project);
    }

    @Override
    public Collection<Group> getGroups(ProjectPermissionKey permissionKey, Project project)
    {
        return getGroups(permissionKey, project.getGenericValue());
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final GenericValue project)
    {
        ProjectPermissionKey permissionKey = getKey(entityTypeId);
        return getGroups(permissionKey, project);
    }

    private Collection<Group> getGroups(ProjectPermissionKey permissionKey, final GenericValue project)
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
                final List<GenericValue> entity = getEntitiesByType(scheme, permissionKey, GroupDropdown.DESC);
                for (final GenericValue permission : entity)
                {
                    groups.add(groupManager.getGroup(permission.getString("parameter")));
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        return groups;
    }

    @Override
    public Collection<User> getUsers(ProjectPermissionKey permissionKey, PermissionContext ctx)
    {
        final Set<User> users = new HashSet<User>();

        final Map<String, SecurityType> permTypes = securityTypeManager.getTypes();
        try
        {
            final List<GenericValue> schemes = getSchemes(ctx.getProject());
            for (final GenericValue scheme : schemes)
            {
                final List<GenericValue> entities = getEntities(scheme, permissionKey);

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
                            log.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error(e.getMessage(), e);
        }
        return users;
    }

    @Override
    protected AbstractSchemeRemovedFromProjectEvent createSchemeRemovedFromProjectEvent(final Scheme scheme, final Project project)
    {
        return new PermissionSchemeRemovedFromProjectEvent(scheme, project);
    }

    /////////////// Private methods /////////////////////////////////////////////////////
    private boolean hasPermission(final ProjectPermissionKey permissionKey, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException("The entity passed must not be null");
        }
        if (!("Project".equals(entity.getEntityName()) || "Issue".equals(entity.getEntityName())))
        {
            throw new IllegalArgumentException("The entity passed must be a Project or an Issue not a " + entity.getEntityName());
        }

        try
        {
            final Map<?, ?> types = permissionTypeManager.getTypes();
            List<GenericValue> schemes = Collections.emptyList();
            if ("Project".equals(entity.getEntityName()))
            {
                //Get the permission scheme associated to the project for this project
                schemes = ComponentAccessor.getPermissionSchemeManager().getSchemes(entity);
            }
            else if ("Issue".equals(entity.getEntityName()))
            {
                final GenericValue project = ComponentAccessor.getProjectManager().getProject(entity);
                schemes = ComponentAccessor.getPermissionSchemeManager().getSchemes(project);
            }

            for (final GenericValue scheme : schemes)
            {
                if (hasSchemePermission(permissionKey, scheme, entity, user, issueCreation, types))
                {
                    return true;
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error(e, e);
            return false;
        }
        return false;
    }

    @VisibleForTesting
    boolean hasSchemePermission(final Long entityTypeId, final GenericValue scheme, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation) throws GenericEntityException
    {
        ProjectPermissionKey permissionKey = getKey(entityTypeId);
        return hasSchemePermission(permissionKey, scheme, entity, user, issueCreation);
    }

    @VisibleForTesting
    boolean hasSchemePermission(final ProjectPermissionKey permissionKey, final GenericValue scheme, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation) throws GenericEntityException
    {
        //Retrieve all scheme permissions
        final Map<?, ?> types = permissionTypeManager.getTypes();
        return hasSchemePermission(permissionKey, scheme, entity, user, issueCreation, types);
    }

    private boolean hasSchemePermission(final ProjectPermissionKey permissionKey, final GenericValue scheme,
            final GenericValue entity, final User user, final boolean issueCreation, final Map<?, ?> types)
            throws GenericEntityException
    {
        final List<GenericValue> entities = getEntities(scheme, permissionKey);
        for (final GenericValue perm : entities)
        {
            if (perm != null)
            {
                final SchemeType schemeType = (SchemeType) types.get(perm.getString("type"));
                if (schemeType != null)
                {
                    if (user == null)
                    {
                        if (schemeType.hasPermission(entity, perm.getString("parameter")))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        if (schemeType.isValidForPermission(permissionKey))
                        {
                            if (schemeType.hasPermission(entity, perm.getString("parameter"), user, issueCreation))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private Long getSchemeEntityCacheKey(final GenericValue scheme)
    {
        return scheme.getLong("id");
    }

    public void flushSchemeEntities()
    {
        schemeEntityCache.removeAll();
    }

    public Collection<GenericValue> getSchemesContainingEntity(final String type, final String parameter)
    {
        Collection<GenericValue> schemes;

        final Collection<GenericValue> entities = delegator.findByAnd(PERMISSION_ENTITY_NAME, EasyMap.build("type", type, "parameter", parameter));
        final Set<Long> schemeIds = new HashSet<Long>();
        final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>();
        for (final GenericValue schemeEntity : entities)
        {
            // This is not needed if we can do a distinct select
            schemeIds.add(schemeEntity.getLong("scheme"));
        }
        for (final Long id : schemeIds)
        {
            entityConditions.add(new EntityExpr("id", EntityOperator.EQUALS, id));
        }

        if (!entityConditions.isEmpty())
        {
            schemes = delegator.findByOr(SCHEME_ENTITY_NAME, entityConditions, Collections.<String>emptyList());
        }
        else
        {
            schemes = Collections.emptyList();
        }
        return schemes;

    }

    private class SchemeEntityCacheEntry
    {
        private List<GenericValue> cache = null;
        private final Map<String, List<GenericValue>> cacheByPermission = newHashMap();

        public List<GenericValue> getCache()
        {
            return cache;
        }

        public Map<String, List<GenericValue>> getCacheByPermission()
        {
            return cacheByPermission;
        }

        public void load(Long key)
        {
            List<GenericValue> schemeEntities;
            try
            {
                GenericValue scheme = DefaultPermissionSchemeManager.super.getScheme(key);
                schemeEntities = DefaultPermissionSchemeManager.super.getEntities(scheme);
            }
            catch (GenericEntityException e)
            {
                throw new RuntimeException(e);
            }
            cache = schemeEntities;

            for (GenericValue entity : schemeEntities)
            {
                String permissionKey = entity.getString("permissionKey");

                List<GenericValue> entitiesForPermission = cacheByPermission.get(permissionKey);
                if (entitiesForPermission == null)
                {
                    entitiesForPermission = new ArrayList<GenericValue>();
                    cacheByPermission.put(permissionKey, entitiesForPermission);
                }
                entitiesForPermission.add(entity);
            }
        }
    }

    private class SchemeEntityCacheLoader implements CacheLoader<Long, SchemeEntityCacheEntry>
    {
        @Override
        public SchemeEntityCacheEntry load(Long key)
        {
            SchemeEntityCacheEntry cacheEntry = new SchemeEntityCacheEntry();
            cacheEntry.load(key);
            return cacheEntry;
        }
    }
}
