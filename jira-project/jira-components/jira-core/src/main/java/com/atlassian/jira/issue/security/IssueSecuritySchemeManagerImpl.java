package com.atlassian.jira.issue.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssocationType;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesUtil;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.issue.security.IssueSecurityLevelAddedEvent;
import com.atlassian.jira.event.issue.security.IssueSecurityLevelDeletedEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeAddedToProjectEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeCopiedEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeCreatedEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeDeletedEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeUpdatedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.util.NameComparator;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class IssueSecuritySchemeManagerImpl extends AbstractSchemeManager implements IssueSecuritySchemeManager, Startable
{
    private static final Logger log = Logger.getLogger(IssueSecuritySchemeManagerImpl.class);

    private static final String SCHEME_ENTITY_NAME = "IssueSecurityScheme";
    private static final String ISSUE_SECURITY_ENTITY_NAME = "SchemeIssueSecurities";
    private static final String SCHEME_DESC = "Issue Security";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.security.default";
    private static final String DEFAULT_DESC_KEY = "admin.schemes.security.default.desc";
    private static final NodeAssocationType ISSUE_SECURITY_SCHEME_ASSOCIATION = new NodeAssocationType("ProjectScheme", "Project", "IssueSecurityScheme");

    private static final int DEFAULT_JIRA_SECURITY_LEVEL_PERMISSIONS_CACHE_MAX_SIZE = 256;

    private final Cache<CacheKey,List<GenericValue>> schemeIdToSecuritiesCache;
    private final Cache<Long, List<IssueSecurityLevelPermission>> securityLevelToPermissionsCache;

    private final ProjectManager projectManager;
    final OfBizDelegator ofBizDelegator;
    private final NodeAssociationStore nodeAssociationStore;

    public IssueSecuritySchemeManagerImpl(ProjectManager projectManager, SecurityTypeManager securityTypeManager,
            PermissionContextFactory permissionContextFactory, SchemeFactory schemeFactory, EventPublisher eventPublisher,
            final OfBizDelegator ofBizDelegator, final GroupManager groupManager, NodeAssociationStore nodeAssociationStore, CacheManager cacheManager,
            final ApplicationProperties applicationProperties)
    {
        super(projectManager, securityTypeManager, permissionContextFactory, schemeFactory,
                nodeAssociationStore, ofBizDelegator, groupManager, eventPublisher, cacheManager);
        this.projectManager = projectManager;
        this.nodeAssociationStore = nodeAssociationStore;
        this.ofBizDelegator = ofBizDelegator;

        this.schemeIdToSecuritiesCache = cacheManager.getCache(IssueSecuritySchemeManagerImpl.class.getName() + ".schemeIdToSecuritiesCache",
                new SecuritiesByFieldCacheLoader("scheme"),
                new CacheSettingsBuilder().maxEntries(64).build());

        final int maxEntries = PropertiesUtil.getIntProperty(applicationProperties, APKeys.JIRA_SECURITY_LEVEL_PERMISSIONS_CACHE_MAX_SIZE, DEFAULT_JIRA_SECURITY_LEVEL_PERMISSIONS_CACHE_MAX_SIZE);

        this.securityLevelToPermissionsCache = cacheManager.getCache(IssueSecuritySchemeManagerImpl.class.getName() + "securityLevelToPermissionsCache",
                new PermissionBySecurityLevelCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maxEntries(maxEntries).build());
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    @Override
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
        clearCache();
    }

    @Override
    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    @Override
    public String getEntityName()
    {
        return ISSUE_SECURITY_ENTITY_NAME;
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

    private void clearCache()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Clearing issue security scheme cache, had " + schemeIdToSecuritiesCache.getKeys().size() +
                    " scheme(s) and " + securityLevelToPermissionsCache.getKeys().size() + " security level(s)");
        }
        schemeIdToSecuritiesCache.removeAll();
        securityLevelToPermissionsCache.removeAll();
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme)
    {
        return schemeIdToSecuritiesCache.get(new CacheKey(scheme.getLong("id")));
    }

    @Override
    public List<GenericValue> getEntities(GenericValue scheme, Long securityLevelId) throws GenericEntityException
    {
        final List<GenericValue> securities = getEntitiesBySecurityLevel(securityLevelId);
        return (securities.isEmpty() || scheme.getLong("id").equals(securities.get(0).getLong("scheme")))
                ? securities
                : Collections.<GenericValue>emptyList();
    }

    @Override
    public IssueSecurityLevelScheme getIssueSecurityLevelScheme(Long issueSecuritySchemeId)
    {
        // TODO: We should probably cache this, but not cached in old code.
        return Select.from(Entity.ISSUE_SECURITY_LEVEL_SCHEME).whereEqual("id", issueSecuritySchemeId).runWith(ofBizDelegator).singleValue();
    }

    @Override
    public List<GenericValue> getEntitiesBySecurityLevel(Long securityLevelId)
    {
        return EntityUtils.convertToGenericValues(Entity.ISSUE_SECURITY_LEVEL_PERMISSION, getPermissionsBySecurityLevel(securityLevelId));
    }

    @Override
    public List<IssueSecurityLevelPermission> getPermissionsBySecurityLevel(Long securityLevelId)
    {
        return securityLevelToPermissionsCache.get(securityLevelId);
    }

    @Override
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter)
    {
        final Collection<GenericValue> entities = ofBizDelegator.findByAnd(ISSUE_SECURITY_ENTITY_NAME, FieldMap.build(
                "type", type,
                "parameter", parameter));
        if (entities.isEmpty())
        {
            return Collections.emptyList();
        }

        final Set<Long> schemeIds = new HashSet<Long>();
        for (GenericValue schemeEntity : entities)
        {
            // This is not needed if we can do a distinct select
            schemeIds.add(schemeEntity.getLong("scheme"));
        }
        return getSchemesByIds(schemeIds);
    }

    @Override
    public void setSchemeForProject(Project project, Long schemeId)
    {
        // remove any existing association
        nodeAssociationStore.removeAssociationsFromSource(ISSUE_SECURITY_SCHEME_ASSOCIATION, project.getId());
        // Add new association if not null
        if (schemeId != null)
            nodeAssociationStore.createAssociation(ISSUE_SECURITY_SCHEME_ASSOCIATION, project.getId(), schemeId);
        flushProjectSchemes();
    }

    @Override
    public List<Project> getProjectsUsingScheme(long schemeId)
    {
        final List<Long> projectIds = nodeAssociationStore.getSourceIdsFromSink(ISSUE_SECURITY_SCHEME_ASSOCIATION, schemeId);

        final List<Project> projects = new ArrayList<Project>();
        for (final Long projectId : projectIds)
        {
            projects.add(projectManager.getProjectObj(projectId));
        }
        Collections.sort(projects, NameComparator.COMPARATOR);
        return projects;
    }

    private Collection<GenericValue> getSchemesByIds(Set<Long> schemeIds)
    {
        List<EntityExpr> entityConditions = new ArrayList<EntityExpr>(schemeIds.size());
        for (Long schemeId : schemeIds)
        {
            entityConditions.add(new EntityExpr("id", EntityOperator.EQUALS, schemeId));
        }
        return entityConditions.isEmpty()
                ? Collections.<GenericValue>emptyList()
                : ofBizDelegator.findByOr(SCHEME_ENTITY_NAME, entityConditions, Collections.<String>emptyList());
    }


    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param schemeTypeId The security level Id
     * @param parameter    The permission parameter (group name etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(GenericValue scheme, Long schemeTypeId, String parameter) throws GenericEntityException
    {
        final List<GenericValue> securities = getEntities(scheme, schemeTypeId);
        return securities.isEmpty() ? securities : filter(securities, withField("parameter", parameter));
    }

    //This one is for Workflows as the entity type is a string
    @Override
    public List<GenericValue> getEntities(GenericValue scheme, String entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Issue Security scheme IDs must be Long values.");
    }

    /**
     * Get all Generic Value issue security records for a particular scheme, type and Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param type         The type of the permission(Group, Current Reporter etc)
     * @param schemeTypeId The security level Id
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(GenericValue scheme, String type, Long schemeTypeId) throws GenericEntityException
    {
        final List<GenericValue> securities = getEntities(scheme, schemeTypeId);
        return securities.isEmpty() ? securities : filter(securities, withField("type", type));
    }

    public GenericValue createSchemeEntity(GenericValue scheme, SchemeEntity schemeEntity) throws GenericEntityException
    {
        final GenericValue result = createSchemeEntityNoEvent(scheme, schemeEntity);
        eventPublisher.publish(new IssueSecurityLevelAddedEvent((scheme == null) ? null : scheme.getLong("id"), schemeEntity));
        return result;
    }

    @Override
    protected GenericValue createSchemeEntityNoEvent(GenericValue scheme, SchemeEntity schemeEntity) throws GenericEntityException
    {
        if (!(schemeEntity.getEntityTypeId() instanceof Long))
        {
            throw new IllegalArgumentException("Issue Security Level IDs must be a long value.");
        }

        try
        {
            final Long schemeId = (scheme == null) ? null : scheme.getLong("id");
            final GenericValue result = EntityUtils.createValue(ISSUE_SECURITY_ENTITY_NAME, FieldMap.build(
                    "scheme", schemeId,
                    "security", schemeEntity.getEntityTypeId(),
                    "type", schemeEntity.getType(),
                    "parameter", schemeEntity.getParameter() ));

            return result;
        }
        finally
        {
            clearCache();
        }
    }

    /**
     * This method overrides the AbstractSchemeManager because within Issue Security schemes there is an extra level, which
     * is the table that holds the Security Levels for that Scheme. This is because with Issue Security schemes you can add and delete
     * the different levels of security. With other schemes this is not possible
     */
    @Override
    public GenericValue copyScheme(GenericValue oldScheme) throws GenericEntityException
    {
        if (oldScheme == null)
        {
            return null;
        }

        try
        {
            final String name = getNameForCopy(oldScheme.getString("name"), null);

            //Copy the original scheme
            final GenericValue newScheme = createSchemeNoEvent(name, oldScheme.getString("description"));

            //for the scheme copy all security levels
            copySecurityLevels(newScheme, oldScheme);

            eventPublisher.publish(createSchemeCopiedEvent(schemeFactory.getScheme(oldScheme), schemeFactory.getScheme(newScheme)));

            return newScheme;
        }
        finally
        {
            clearCache();
        }
    }

    @Nonnull
    @Override
    protected AbstractSchemeCopiedEvent createSchemeCopiedEvent(@Nonnull final Scheme oldScheme, @Nonnull final Scheme newScheme)
    {
        return new IssueSecuritySchemeCopiedEvent(oldScheme, newScheme);
    }

    private void copySecurityLevels(GenericValue scheme, GenericValue oldScheme) throws GenericEntityException
    {
        //get all the security levels for this scheme
        List<GenericValue> levels = ofBizDelegator.findByAnd("SchemeIssueSecurityLevels", FieldMap.build("scheme", oldScheme.getLong("id")));

        //create the security levels for the new scheme
        for (GenericValue level : levels)
        {
            IssueSecurityLevel newSecurityLevel = ComponentAccessor.getIssueSecurityLevelManager().createIssueSecurityLevel(
                    scheme.getLong("id"),
                    level.getString("name"),
                    level.getString("description"));

            //if this level is the default level for the old scheme then make it the default level for the new scheme also
            if (level.getLong("id").equals(oldScheme.getLong("defaultlevel")))
            {
                scheme.set("defaultlevel", newSecurityLevel.getId());
                scheme.store();
            }

            //copy all the securities for this security level and scheme and copy them also
            List<GenericValue> securities = ofBizDelegator.findByAnd(getEntityName(), FieldMap.build(
                    "scheme", oldScheme.getLong("id"),
                    "security", level.getLong("id")));

            for (GenericValue security : securities)
            {
                createSchemeEntity(scheme, new SchemeEntity(security.getString("type"), security.getString("parameter"), newSecurityLevel.getId()));
            }
        }
    }

    @Override
    public boolean hasSchemeAuthority(Long entityType, GenericValue entity)
    {
        return hasPermission(entityType, entity, null);

    }


    /**
     * Checks to see if the user has access to issues of this security level.
     * If the user is not passed in then the check is made on the current user
     *
     * @param entityType    The security level to check against
     * @param issue         The issue
     * @param user          The user to check for the permission. User must NOT be null
     * @param issueCreation <code>true</code> if this is an attempt to create a new issue
     * @return true if the user is a member of the security level otherwise false
     */
    @Override
    public boolean hasSchemeAuthority(Long entityType, GenericValue issue, User user, boolean issueCreation)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User passed must NOT be null");
        }

        return hasPermission(entityType, issue, user);
    }

    private boolean hasPermission(Long entityType, GenericValue issue, User user)
    {
        //If the entity type is null then there is no security set on the issue
        if (entityType == null)
        {
            return true;
        }
        if (issue == null)
        {
            throw new IllegalArgumentException("GenericValue passed must NOT be null");
        }
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("GenericValue passed must be an Issue and not " + issue.getEntityName());
        }

        //Get the project for the entity
        GenericValue project = projectManager.getProject(issue);
        try
        {
            //Get the issue security scheme associated to the project for this project
            List<GenericValue> schemes = getSchemes(project);
            for (GenericValue scheme : schemes)
            {
                if (scheme != null)
                {
                    //try each type of Issue Security Type
                    return hasPermission(issue, user, getEntities(scheme, entityType));
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Could not retrieve entites from the database", e);
        }
        return false;
    }

    //Check to see if the permission exists
    private boolean hasPermission(GenericValue issue, User user, List<GenericValue> entities)
    {
        if (entities.isEmpty())
        {
            return false;
        }

        //loop through each issue security type to see if the user has access to the issue.
        //Once the permission is reached then we return right away
        for (SecurityType type : securityTypeManager.getTypes().values())
        {
            for (GenericValue perm : filter(entities, withField("type", type.getType())))
            {
                if (perm != null && hasPermission(issue, user, type, perm))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasPermission(GenericValue issue, User user, SecurityType type, GenericValue perm)
    {
        return (user == null)
                ? type.hasPermission(issue, perm.getString("parameter"))
                : type.hasPermission(issue, perm.getString("parameter"), user, false);
    }

    @Override
    protected AbstractSchemeUpdatedEvent createSchemeUpdatedEvent(final Scheme scheme, final Scheme originalScheme)
    {
        return new IssueSecuritySchemeUpdatedEvent(scheme, originalScheme);
    }

    /**
     * Deletes a scheme from the database
     *
     * @param id Id of the scheme to be deleted
     * @throws GenericEntityException
     */
    @Override
    public void deleteScheme(Long id) throws GenericEntityException
    {
        try
        {
            GenericValue scheme = getScheme(id);
            nodeAssociationStore.removeAssociationsFromSink(scheme);
            scheme.removeRelated("Child" + getEntityName());
            // Cache for this gets reset by the IssueSecuritySchemeDeletedEvent fired below.
            scheme.removeRelated("Child" + "SchemeIssueSecurityLevels");
            scheme.remove();

            eventPublisher.publish(new IssueSecuritySchemeDeletedEvent(id));
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    public void deleteEntity(Long id) throws DataAccessException
    {
        try
        {
            super.deleteEntity(id);
            eventPublisher.publish(new IssueSecurityLevelDeletedEvent(id));
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    protected SchemeEntity makeSchemeEntity(final GenericValue entity)
    {
        return new SchemeEntity(entity.getString("type"), entity.getString("parameter"), entity.getLong("security"));
    }

    @Override
    protected Object createSchemeEntityDeletedEvent(final GenericValue entity)
    {
        return null;
    }

    @Override
    public boolean removeEntities(GenericValue scheme, Long entityTypeId) throws RemoveException
    {
        try
        {
            return super.removeEntities(scheme, entityTypeId);
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    public GenericValue createScheme(String name, String description) throws GenericEntityException
    {
        try
        {
            return super.createScheme(name, description);
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    protected AbstractSchemeEvent createSchemeCreatedEvent(final Scheme scheme)
    {
        return new IssueSecuritySchemeCreatedEvent(scheme);
    }

    @Override
    protected void flushProjectSchemes()
    {
        try
        {
            super.flushProjectSchemes();
        }
        finally
        {
            clearCache();
        }
    }

    /**
     * This method overrides the super implemntation in order to clear cache.
     *
     * @param type      type
     * @param parameter parameter
     * @return the original result of the call to super method
     * @throws RemoveException if super method throws it
     */
    @Override
    public boolean removeEntities(String type, String parameter) throws RemoveException
    {
        try
        {
            return super.removeEntities(type, parameter);
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    protected AbstractSchemeAddedToProjectEvent createSchemeAddedToProjectEvent(final Scheme scheme, final Project project)
    {
        return new IssueSecuritySchemeAddedToProjectEvent(scheme, project);
    }

    @Override
    protected AbstractSchemeRemovedFromProjectEvent createSchemeRemovedFromProjectEvent(final Scheme scheme, final Project project)
    {
        return new IssueSecuritySchemeRemovedFromProjectEvent(scheme, project);
    }

    private static List<GenericValue> filter(List<GenericValue> list, Predicate<GenericValue> predicate)
    {
        return ImmutableList.copyOf(Iterables.filter(list, predicate));
    }

    private static Predicate<GenericValue> withField(final String field, final String value)
    {
        if (value == null)
        {
            return new Predicate<GenericValue>()
            {
                @Override
                public boolean apply(GenericValue input)
                {
                    return input.getString(field) == null;
                }
            };
        }

        return new Predicate<GenericValue>()
        {
            @Override
            public boolean apply(GenericValue input)
            {
                return value.equals(input.getString(field));
            }
        };
    }

    class SecuritiesByFieldCacheLoader implements CacheLoader<CacheKey,List<GenericValue>>
    {
        private final String key;

        SecuritiesByFieldCacheLoader(final String key)
        {
            this.key = notNull("key", key);
        }

        public List<GenericValue> load(CacheKey cacheKey)
        {
            final List<GenericValue> result = ofBizDelegator.findByAnd(getEntityName(), FieldMap.build(key, cacheKey.id));
            return (result != null) ? result : Collections.<GenericValue>emptyList();
        }
    }

    static class CacheKey implements Serializable
    {
        final Long id;

        CacheKey(final Long id)
        {
            this.id = id;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final CacheKey other = (CacheKey)o;
            return (id == null) ? (other.id == null) : id.equals(other.id);
        }

        @Override
        public int hashCode()
        {
            return (id != null) ? id.hashCode() : 0;
        }
    }

    private final class PermissionBySecurityLevelCacheLoader implements CacheLoader<Long, List<IssueSecurityLevelPermission>>
    {
        @Override
        public List<IssueSecurityLevelPermission> load(@Nullable Long input)
        {
            return Select.from(Entity.ISSUE_SECURITY_LEVEL_PERMISSION)
                    .whereEqual("security", input)
                    .runWith(ofBizDelegator).asList();
        }
    }
}

