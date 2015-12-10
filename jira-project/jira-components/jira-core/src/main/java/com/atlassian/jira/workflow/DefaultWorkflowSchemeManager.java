package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeAddedToProjectEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeCopiedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeCreatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeDeletedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationTaskAccessor;
import com.atlassian.util.concurrent.ManagedLocks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.workflow.DefaultWorkflowSchemeManager.WorkflowAction.DELETE_ENTITY;
import static com.atlassian.jira.workflow.DefaultWorkflowSchemeManager.WorkflowAction.DELETE_SCHEME;
import static com.atlassian.jira.workflow.DefaultWorkflowSchemeManager.WorkflowAction.DELETE_WORKFLOW_SCHEME;
import static com.atlassian.jira.workflow.DefaultWorkflowSchemeManager.WorkflowAction.UPDATE_DRAFT_WORKFLOW_SCHEME;
import static com.atlassian.jira.workflow.DefaultWorkflowSchemeManager.WorkflowAction.UPDATE_SCHEME;
import static com.atlassian.jira.workflow.DefaultWorkflowSchemeManager.WorkflowAction.UPDATE_WORKFLOW_SCHEME;
import static java.lang.String.format;

public class DefaultWorkflowSchemeManager extends AbstractSchemeManager implements WorkflowSchemeManager, Startable
{
    /**
     * The workflow actions that we guard with locks.
     */
    enum WorkflowAction {

        DELETE_ENTITY,
        DELETE_SCHEME,
        DELETE_WORKFLOW_SCHEME,
        UPDATE_DRAFT_WORKFLOW_SCHEME,
        UPDATE_SCHEME,
        UPDATE_WORKFLOW_SCHEME;

        /**
         * Returns the name of this action's lock.
         *
         * @param id the ID of the thing being acted upon
         * @return a non-blank name
         */
        private String getLockName(final Long id)
        {
            return WorkflowAction.class.getName() + "." + this + "_" + id;
        }
    }

    private static final Logger log = Logger.getLogger(DefaultWorkflowSchemeManager.class);
    private static final String ALL_ISSUE_TYPES = "0";
    private static final String SCHEME_ENTITY_NAME = "WorkflowScheme";
    private static final String WORKFLOW_ENTITY_NAME = "WorkflowSchemeEntity";
    private static final String SCHEME_DESC = "Workflow";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.workflows.default";
    private static final String DEFAULT_DESC_KEY = "admin.schemes.workflows.default.desc";

    private static final String COLUMN_ISSUETYPE = "issuetype";
    private static final String COLUMN_WORKFLOW = "workflow";

    private static final String WORKFLOW_CACHE_NAME = DefaultWorkflowSchemeManager.class.getName() + ".workflows";
    private static final String ENTITY_CACHE_NAME = DefaultWorkflowSchemeManager.class.getName() + ".workflowSchemeEntityCache";

    // Fields
    private final AssignableWorkflowScheme defaultScheme;

    // Stores {WorkflowScheme ID} -> {{issuetype} -> {WorkflowSchemeEntity}}.
    private Cache<Long, Map<String, GenericValue>> workflowSchemeEntityCache;

    private final WorkflowManager workflowManager;
    private final ConstantsManager constantsManager;
    private final OfBizDelegator ofBizDelegator;
    private final DraftWorkflowSchemeStore draftWorkflowSchemeStore;
    private final UserManager userManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final AssignableWorkflowSchemeStore assignableWorkflowSchemeStore;
    private final CacheManager cacheManager;
    private final ClusterLockService clusterLockService;
    private CachedReference<Set<String>> activeWorkflowCache;

    public DefaultWorkflowSchemeManager(final ProjectManager projectManager, final PermissionTypeManager permissionTypeManager,
            final PermissionContextFactory permissionContextFactory, final SchemeFactory schemeFactory,
            final WorkflowManager workflowManager, final ConstantsManager constantsManager,
            final OfBizDelegator ofBizDelegator, final EventPublisher eventPublisher,
            final NodeAssociationStore nodeAssociationStore, final GroupManager groupManager,
            final DraftWorkflowSchemeStore draftWorkflowSchemeStore,
            final JiraAuthenticationContext context, final UserManager userManager, final I18nHelper.BeanFactory i18nFactory,
            final AssignableWorkflowSchemeStore assignableWorkflowSchemeStore, final CacheManager cacheManager,
            final ClusterLockService clusterLockService)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, schemeFactory,
                nodeAssociationStore, ofBizDelegator, groupManager, eventPublisher, cacheManager);
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
        this.ofBizDelegator = ofBizDelegator;
        this.draftWorkflowSchemeStore = draftWorkflowSchemeStore;
        this.userManager = userManager;
        this.i18nFactory = i18nFactory;
        this.assignableWorkflowSchemeStore = assignableWorkflowSchemeStore;
        this.cacheManager = cacheManager;
        this.clusterLockService = clusterLockService;
        this.defaultScheme = new DefaultWorkflowScheme(context);
    }

    public void start()
    {
        this.activeWorkflowCache = cacheManager.getCachedReference(WORKFLOW_CACHE_NAME, new WorkflowSupplier());
        this.workflowSchemeEntityCache = cacheManager.getCache(ENTITY_CACHE_NAME, new WorkflowSchemeEntitySupplier());
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
        clearWorkflowCache();
    }

    @Override
    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    @Override
    public String getEntityName()
    {
        return WORKFLOW_ENTITY_NAME;
    }

    public void clearWorkflowCache()
    {
        activeWorkflowCache.reset();
        clearWorkflowSchemeEntityCache();
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

    public GenericValue getWorkflowScheme(final GenericValue project) throws GenericEntityException
    {
        return EntityUtil.getOnly(getSchemes(project));
    }

    private Map<String, String> toWorkflowMap(Iterable<GenericValue> related)
    {
        Map<String, String> mapping = Maps.newHashMap();
        for (GenericValue value : related)
        {
            String issuetype = value.getString(COLUMN_ISSUETYPE);
            String workflow = value.getString(COLUMN_WORKFLOW);

            if (issuetype != null)
            {
                if (ALL_ISSUE_TYPES.equals(issuetype))
                {
                    mapping.put(null, workflow);
                }
                else
                {
                    mapping.put(issuetype,  workflow);
                }
            }
        }
        return mapping;
    }

    @Override
    public boolean hasDraft(@Nonnull AssignableWorkflowScheme scheme)
    {
        notNull("scheme", scheme);

        return !scheme.isDefault() && scheme.getId() != null && draftWorkflowSchemeStore.hasDraftForParent(scheme.getId());
    }

    @Nonnull
    @Override
    public AssignableWorkflowScheme createScheme(@Nonnull AssignableWorkflowScheme workflowScheme)
    {
        notNull("wokflowScheme", workflowScheme);

        AssignableWorkflowSchemeStore.AssignableState.Builder builder = assignableWorkflowSchemeStore.builder();
        builder.setName(workflowScheme.getName())
                .setDescription(workflowScheme.getDescription())
                .setMappings(workflowScheme.getMappings());

        AssignableWorkflowSchemeStore.AssignableState savedState = assignableWorkflowSchemeStore.create(builder.build());
        eventPublisher.publish(createSchemeCreatedEvent(getSchemeObject(savedState.getId())));
        return toWorkflowScheme(savedState);
    }

    public GenericValue createSchemeEntity(final GenericValue scheme, final SchemeEntity schemeEntity)
            throws GenericEntityException
    {
        return createSchemeEntity(scheme.getLong("id"), schemeEntity);
    }

    protected GenericValue createSchemeEntityNoEvent(final GenericValue scheme, final SchemeEntity schemeEntity)
            throws GenericEntityException
    {
        return createSchemeEntity(scheme.getLong("id"), schemeEntity);
    }

    public GenericValue createSchemeEntity(long schemeId, final SchemeEntity schemeEntity)
            throws GenericEntityException
    {
        if (!(schemeEntity.getEntityTypeId() instanceof String))
        {
            throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
        }

        try
        {
            return EntityUtils.createValue(getEntityName(), FieldMap.build("scheme", schemeId, COLUMN_WORKFLOW, schemeEntity.getType(),
                    COLUMN_ISSUETYPE, schemeEntity.getEntityTypeId().toString()));
        }
        finally
        {
            clearWorkflowSchemeEntityCache();
        }
    }

    private void clearWorkflowSchemeEntityCache()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Clearing workflow scheme entity cache");
        }
        workflowSchemeEntityCache.removeAll();
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final String issuetype)
            throws GenericEntityException
    {
        final Map<String, GenericValue> genericValueMap = workflowSchemeEntityCache.get(getCacheKeyForScheme(scheme));
        final GenericValue value = genericValueMap.get(issuetype);
        if (value == null)
        {
            return Collections.emptyList();
        }
        return Collections.singletonList(value);
    }

    public Map<String, String> getWorkflowMap(Project project)
    {
        final GenericValue schemeForProject = getSchemeForProject(project);
        return getWorkflowMap(schemeForProject);
    }

    @VisibleForTesting
    Map<String, String> getWorkflowMap(GenericValue schemeForProject)
    {
        if (schemeForProject == null)
        {
            return MapBuilder.build(null, JiraWorkflow.DEFAULT_WORKFLOW_NAME);
        }
        else
        {
            final Long cacheKeyForScheme = getCacheKeyForScheme(schemeForProject);
            final Map<String, GenericValue> schemeMap = workflowSchemeEntityCache.get(cacheKeyForScheme);
            return toWorkflowMap(schemeMap.values());
        }
    }

    public String getWorkflowName(Project project, String issueType)
    {
        return getWorkflowName(getSchemeForProject(project), issueType);
    }

    public String getWorkflowName(GenericValue scheme, String issueType)
    {
        if (scheme != null)
        {
            final Map<String, GenericValue> map = workflowSchemeEntityCache.get(getCacheKeyForScheme(scheme));
            GenericValue value = map.get(issueType);
            if (value == null)
            {
                value = map.get(ALL_ISSUE_TYPES);
            }
            if (value != null)
            {
                return value.getString(COLUMN_WORKFLOW);
            }
        }

        // otherwise always return the default workflow
        return JiraWorkflow.DEFAULT_WORKFLOW_NAME;
    }

    @Override
    public boolean isUsingDefaultScheme(Project project)
    {
        return getSchemeForProject(project) == null;
    }

    @VisibleForTesting
    GenericValue getSchemeForProject(final Project project)
    {
        return getSchemeForProject(project.getGenericValue());
    }

    @VisibleForTesting
    GenericValue getSchemeForProject(final GenericValue project)
    {
        try
        {
            return EntityUtil.getOnly(getSchemes(project));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private static Long getCacheKeyForScheme(final GenericValue scheme)
    {
        return scheme.getLong("id");
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId, final String parameter) throws GenericEntityException
    {
        throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final String type, final Long entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
    }

    public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity)
    {
        return false;
    }

    public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        return false;
    }

    public GenericValue getDefaultEntity(final GenericValue scheme) throws GenericEntityException
    {
        return EntityUtil.getOnly(getEntities(scheme, ALL_ISSUE_TYPES));
    }

    @Override
    public AssignableWorkflowScheme getDefaultWorkflowScheme()
    {
        return defaultScheme;
    }

    public List<GenericValue> getNonDefaultEntities(final GenericValue scheme) throws GenericEntityException
    {
        final List<GenericValue> entities = getEntities(scheme);

        // remove the default entity
        for (final Iterator<GenericValue> iterator = entities.iterator(); iterator.hasNext();)
        {
            final GenericValue genericValue = iterator.next();
            if (ALL_ISSUE_TYPES.equals(genericValue.getString(COLUMN_ISSUETYPE)))
            {
                iterator.remove();
                break;
            }
        }
        return entities;
    }

    public Collection<String> getActiveWorkflowNames() throws GenericEntityException, WorkflowException
    {
        return activeWorkflowCache.get();
    }

    public void addWorkflowToScheme(final GenericValue scheme, final String workflowName, final String issueTypeId) throws GenericEntityException
    {
        try
        {
            final SchemeEntity schemeEntity = new SchemeEntity(workflowName, issueTypeId);

            // prevent adding the same workflow multiple times to one scheme
            if (getEntities(scheme, issueTypeId).isEmpty())
            {
                createSchemeEntity(scheme, schemeEntity);
            }
        }
        finally
        {
            clearWorkflowCache();
        }
    }

    @Override
    @Nonnull
    public DraftWorkflowScheme createDraftOf(ApplicationUser creator, @Nonnull AssignableWorkflowScheme workflowScheme)
    {
        notNull("workflowScheme", workflowScheme);
        Assertions.not("workflowScheme.default", workflowScheme.isDefault());
        notNull("workflowScheme.id", workflowScheme.getId());
        Assertions.not("scheme already has draft.", hasDraft(workflowScheme));

        final DraftWorkflowSchemeStore.DraftState.Builder builder = draftWorkflowSchemeStore.builder(workflowScheme.getId());
        builder.setMappings(workflowScheme.getMappings());
        builder.setLastModifiedUser(creator != null ? creator.getKey() : null);

        final DraftWorkflowSchemeStore.DraftState state = draftWorkflowSchemeStore.create(builder.build());
        return toWorkflowScheme(state);
    }

    @Nonnull
    @Override
    public DraftWorkflowScheme createDraft(ApplicationUser creator, @Nonnull DraftWorkflowScheme workflowScheme)
    {
        notNull("workflowScheme", workflowScheme);
        AssignableWorkflowScheme parentScheme = workflowScheme.getParentScheme();
        notNull("workflowScheme.parentScheme", parentScheme);
        notNull("workflowScheme.parentScheme.id", parentScheme.getId());

        Assertions.not("scheme already has draft.", hasDraft(parentScheme));

        final DraftWorkflowSchemeStore.DraftState.Builder builder = draftWorkflowSchemeStore.builder(parentScheme.getId());
        builder.setMappings(workflowScheme.getMappings());
        builder.setLastModifiedUser(creator != null ? creator.getKey() : null);

        final DraftWorkflowSchemeStore.DraftState state = draftWorkflowSchemeStore.create(builder.build());
        return toWorkflowScheme(state);
    }

    @Override
    @Nonnull
    public Iterable<AssignableWorkflowScheme> getAssignableSchemes()
    {
        Iterable<AssignableWorkflowScheme> workflowSchemes = Iterables.transform(assignableWorkflowSchemeStore.getAll(), toAssignableFunction());
        return WorkflowSchemes.nameOrdering().immutableSortedCopy(workflowSchemes);
    }

    public void updateSchemesForRenamedWorkflow(final String oldWorkflowName, final String newWorkflowName)
    {
        if (StringUtils.isBlank(oldWorkflowName))
        {
            throw new IllegalArgumentException("oldWorkflowName must not be null or empty string");
        }
        if (StringUtils.isBlank(newWorkflowName))
        {
            throw new IllegalArgumentException("newWorkflowName must not be null or empty string");
        }

        //TODO: Will have to fix this for workflow schemes.
        ofBizDelegator.bulkUpdateByAnd(getEntityName(), ImmutableMap.of(COLUMN_WORKFLOW, newWorkflowName), ImmutableMap.of(COLUMN_WORKFLOW, oldWorkflowName));
        draftWorkflowSchemeStore.renameWorkflow(oldWorkflowName, newWorkflowName);
        clearWorkflowCache();
    }

    public Collection<GenericValue> getSchemesForWorkflow(final JiraWorkflow workflow)
    {
        // TODO This does not cater for default workflow which is used by schemes with no default for all issue types and
        // by projects that do not have a workflow shceme assigned
        final Collection<GenericValue> schemes = new LinkedList<GenericValue>();
        final Set<Long> schemeIds = new HashSet<Long>();
        // Find all scheme entities with the workflow name
        final List<GenericValue> schemeEntities = ofBizDelegator.findByAnd(getEntityName(),
                MapBuilder.build(COLUMN_WORKFLOW, workflow.getName()));
        // Loop through all the entities and retrieve the scheme ids
        for (final GenericValue schemeEntity : schemeEntities)
        {
            final Long schemeId = schemeEntity.getLong("scheme");
            // Only retrieve schemes that we have not retrieved already
            if (!schemeIds.contains(schemeId))
            {
                schemes.add(getScheme(schemeId));
                schemeIds.add(schemeId);
            }
        }

        return schemes;
    }

    @Override
    public Iterable<WorkflowScheme> getSchemesForWorkflowIncludingDrafts(JiraWorkflow workflow)
    {
        if (workflow.isSystemWorkflow())
        {
            throw new IllegalArgumentException("Can't get schemes for system workflow");
        }

        Collection<GenericValue> schemes = getSchemesForWorkflow(workflow);
        Iterable<DraftWorkflowSchemeStore.DraftState> draftSchemeStates = draftWorkflowSchemeStore.getSchemesUsingWorkflow(workflow);

        Collection<WorkflowScheme> allSchemes = Lists.newArrayListWithCapacity(schemes.size() + Iterables.size(draftSchemeStates));

        for (GenericValue schemeGenericValue : schemes)
        {
            allSchemes.add(toWorkflowScheme(schemeGenericValue));
        }

        for (DraftWorkflowSchemeStore.DraftState draftState : draftSchemeStates)
        {
            allSchemes.add(toWorkflowScheme(draftState));
        }

        return allSchemes;
    }

    @Override
    public void deleteEntity(final Long id) throws DataAccessException
    {
        ManagedLocks.manage(getLock(id, DELETE_ENTITY)).withLock(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    DefaultWorkflowSchemeManager.super.deleteEntity(id);
                }
                finally
                {
                    clearWorkflowSchemeEntityCache();
                }
            }
        });
    }

    private ClusterLock getLock(final Long id, final WorkflowAction action)
    {
        return clusterLockService.getLockForName(action.getLockName(id));
    }

    @Override
    protected SchemeEntity makeSchemeEntity(final GenericValue entity)
    {
        return new SchemeEntity(entity.getString(COLUMN_WORKFLOW), entity.getString(COLUMN_ISSUETYPE));
    }

    @Override
    protected Object createSchemeEntityDeletedEvent(final GenericValue entity)
    {
        return null;
    }

    @Override
    public boolean removeEntities(final GenericValue scheme, final Long entityTypeId) throws RemoveException
    {
        try
        {
            return super.removeEntities(scheme, entityTypeId);
        }
        finally
        {
            clearWorkflowSchemeEntityCache();
        }
    }

    @Override
    public GenericValue createScheme(final String name, final String description) throws GenericEntityException
    {
        try
        {
            return super.createScheme(name, description);
        }
        finally
        {
            clearWorkflowSchemeEntityCache();
        }
    }

    @Override
    protected AbstractSchemeEvent createSchemeCreatedEvent(final Scheme scheme)
    {
        return new WorkflowSchemeCreatedEvent(scheme);
    }

    @Override
    @Nonnull
    protected AbstractSchemeCopiedEvent createSchemeCopiedEvent(@Nonnull final Scheme oldScheme, @Nonnull final Scheme newScheme)
    {
        return new WorkflowSchemeCopiedEvent(oldScheme, newScheme);
    }

    @Override
    public void deleteScheme(final Long id)
    {
        if (id == null)
        {
            return;
        }
        ManagedLocks.manage(getLock(id, DELETE_SCHEME)).withLock(new Runnable()
        {
            @Override
            public void run()
            {
                final AssignableWorkflowScheme scheme = getWorkflowSchemeObj(id);
                doDeleteScheme(scheme);
            }
        });
    }

    @Nonnull
    @Override
    protected AbstractSchemeAddedToProjectEvent createSchemeAddedToProjectEvent(final Scheme scheme, final Project project)
    {
        return new WorkflowSchemeAddedToProjectEvent(scheme, project);
    }

    void doDeleteScheme(AssignableWorkflowScheme scheme)
    {
        checkMigration(scheme);

        try
        {
            super.deleteScheme(scheme.getId());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        draftWorkflowSchemeStore.deleteByParentId(scheme.getId());
        eventPublisher.publish(new WorkflowSchemeDeletedEvent(scheme.getId(), scheme.getName()));
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
            clearWorkflowSchemeEntityCache();
        }
    }

    @VisibleForTesting
    protected List<GenericValue> getAllIssueTypes()
    {
        return constantsManager.getAllIssueTypes();
    }

    @VisibleForTesting
    protected JiraWorkflow getWorkflowFromScheme(final GenericValue workflowScheme, final String issueTypeId)
    {
        return workflowManager.getWorkflowFromScheme(workflowScheme, issueTypeId);
    }

    @Nullable
    @Override
    public AssignableWorkflowScheme getWorkflowSchemeObj(long id)
    {
        return toWorkflowScheme(getScheme(id));
    }

    @Override
    public AssignableWorkflowScheme getWorkflowSchemeObj(String name)
    {
        return toWorkflowScheme(getScheme(name));
    }

    @Override
    public DraftWorkflowScheme getDraftForParent(@Nonnull AssignableWorkflowScheme scheme)
    {
        notNull("scheme", scheme);

        if (scheme.isDefault() || scheme.getId() == null)
        {
            return null;
        }
        else
        {
            return getDraftForParent(scheme.getId());
        }
    }

    private DraftWorkflowScheme getDraftForParent(long parentId)
    {
        DraftWorkflowSchemeStore.DraftState draftScheme = draftWorkflowSchemeStore.getDraftForParent(parentId);
        return toWorkflowScheme(draftScheme);
    }

    @Override
    public DraftWorkflowScheme getDraft(long id)
    {
        DraftWorkflowSchemeStore.DraftState draftScheme = draftWorkflowSchemeStore.get(id);
        return toWorkflowScheme(draftScheme);
    }

    public AssignableWorkflowScheme getParentForDraft(long draftSchemeId)
    {
        long parentId = draftWorkflowSchemeStore.getParentId(draftSchemeId);

        return getWorkflowSchemeObj(parentId);
    }

    @Override
    public boolean isActive(@Nonnull WorkflowScheme scheme)
    {
        notNull("scheme", scheme);

        if (scheme.isDraft())
        {
            return false;
        }

        //The default is a bugger. We don't store anything in the database so we have to do a loop.
        if (scheme.isDefault())
        {
            final List<Project> projectObjects = projectManager.getProjectObjects();
            for (Project projectObject : projectObjects)
            {
                if (isUsingDefaultScheme(projectObject))
                {
                    return true;
                }
            }
            return false;
        }
        else
        {
            if (scheme.getId() == null)
            {
                return false;
            }

            try
            {
                GenericValue genericValue = getScheme(scheme.getId());
                if (genericValue != null)
                {
                    List<?> projects = getProjects(genericValue);
                    return !projects.isEmpty();
                }
                else
                {
                    return false;
                }
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    @Override
    public boolean deleteWorkflowScheme(@Nonnull final WorkflowScheme scheme)
    {
        notNull("scheme", scheme);
        Assertions.not("scheme.default", scheme.isDefault());
        notNull("scheme.id", scheme.getId());

        final long lockSchemeId;
        if (scheme.isDraft())
        {
            lockSchemeId = ((DraftWorkflowScheme) scheme).getParentScheme().getId();
        }
        else
        {
            lockSchemeId = scheme.getId();
        }

        final Lock lock = getLock(lockSchemeId, DELETE_WORKFLOW_SCHEME);
        return ManagedLocks.manage(lock).withLock(new com.atlassian.util.concurrent.Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                if (scheme.isDraft())
                {
                    DraftWorkflowScheme draftScheme = (DraftWorkflowScheme) scheme;

                    WorkflowSchemeMigrationTaskAccessor taskAccessor = getTaskAccessor();
                    if (taskAccessor.getActiveByProjects(draftScheme, true, true) != null)
                    {
                        throw new SchemeIsBeingMigratedException();
                    }

                    return draftWorkflowSchemeStore.delete(scheme.getId());
                }
                else
                {
                    Assertions.not("Cannot delete active scheme.", isActive(scheme));
                    doDeleteScheme((AssignableWorkflowScheme) scheme);
                    return true;
                }
            }
        });
    }

    private void checkMigration(AssignableWorkflowScheme scheme)
    {
        if (scheme == null)
        {
            return;
        }

        WorkflowSchemeMigrationTaskAccessor taskAccessor = getTaskAccessor();

        if (taskAccessor.getActive(scheme) != null)
        {
            throw new SchemeIsBeingMigratedException();
        }
    }

    @Override
    public DraftWorkflowScheme updateDraftWorkflowScheme(final ApplicationUser user, @Nonnull final DraftWorkflowScheme scheme)
    {
        notNull("scheme", scheme);
        notNull("scheme.id", scheme.getId());

        // We lock the parent scheme in case of drafts
        final Lock lock = getLock(scheme.getParentScheme().getId(), UPDATE_DRAFT_WORKFLOW_SCHEME);
        return ManagedLocks.manage(lock).withLock(new com.atlassian.util.concurrent.Supplier<DraftWorkflowScheme>()
        {
            @Override
            public DraftWorkflowScheme get()
            {
                WorkflowSchemeMigrationTaskAccessor taskAccessor = getTaskAccessor();
                if (taskAccessor.getActiveByProjects(scheme, true) != null)
                {
                    throw new SchemeIsBeingMigratedException();
                }

                DraftWorkflowSchemeStore.DraftState savedState = draftWorkflowSchemeStore.get(scheme.getId());

                notNull(format("scheme with id %d does not exist.", scheme.getId()), savedState);

                DraftWorkflowSchemeStore.DraftState.Builder builder = savedState.builder();
                builder.setMappings(scheme.getMappings());
                builder.setLastModifiedUser(user == null ? null : user.getKey());

                return toWorkflowScheme(draftWorkflowSchemeStore.update(builder.build()));
            }
        });
    }

    @Override
    public AssignableWorkflowScheme updateWorkflowScheme(@Nonnull final AssignableWorkflowScheme workflowScheme)
    {
        notNull("scheme", workflowScheme);
        notNull("scheme.id", workflowScheme.getId());

        final Lock lock = getLock(workflowScheme.getId(), UPDATE_WORKFLOW_SCHEME);
        return ManagedLocks.manage(lock).withLock(new com.atlassian.util.concurrent.Supplier<AssignableWorkflowScheme>()
        {
            @Override
            public AssignableWorkflowScheme get()
            {
                checkMigration(workflowScheme);

                Scheme scheme = getSchemeObject(workflowScheme.getId());

                // Delete all existing entitites.
                Collection<SchemeEntity> entities = scheme.getEntities();
                for (SchemeEntity entity : entities)
                {
                    deleteEntity(entity.getId());
                }

                scheme = new Scheme(workflowScheme.getId(), getSchemeEntityName(), workflowScheme.getName(),
                        workflowScheme.getDescription(), Collections.<SchemeEntity>emptyList());

                doUpdateScheme(scheme);

                createSchemeEntities(workflowScheme);

                return getWorkflowSchemeObj(workflowScheme.getId());
            }
        });
    }

    @Override
    public void updateScheme(final Scheme scheme) throws DataAccessException
    {
        ManagedLocks.manage(getLock(scheme.getId(), UPDATE_SCHEME)).withLock(new Runnable()
        {
            @Override
            public void run()
            {
                final AssignableWorkflowScheme schemeObj = toWorkflowScheme(getScheme(scheme.getId()));
                checkMigration(schemeObj);
                doUpdateScheme(scheme);
            }
        });
    }

    @Override
    @Nonnull
    protected AbstractSchemeRemovedFromProjectEvent createSchemeRemovedFromProjectEvent(
            final Scheme scheme, final Project project)
    {
        return new WorkflowSchemeRemovedFromProjectEvent(scheme, project);
    }

    @Override
    protected AbstractSchemeUpdatedEvent createSchemeUpdatedEvent(final Scheme scheme, final Scheme originalScheme)
    {
        return new WorkflowSchemeUpdatedEvent(scheme, originalScheme);
    }

    private void doUpdateScheme(Scheme scheme)
    {
        super.updateScheme(scheme);
    }

    WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
    {
        return ComponentAccessor.getComponent(WorkflowSchemeMigrationTaskAccessor.class);
    }

    @Override
    public <T> T waitForUpdatesToFinishAndExecute(AssignableWorkflowScheme scheme, Callable<T> task) throws Exception
    {
        if (scheme == null || scheme.getId() == null)
        {
            return task.call();
        }
        final Collection<Lock> locks = new ArrayList<Lock>();
        try
        {
            for (final WorkflowAction action : WorkflowAction.values())
            {
                final ClusterLock lock = getLock(scheme.getId(), action);
                lock.lock();
                locks.add(lock);
            }
            return task.call();
        }
        finally
        {
            unlockAll(locks);
        }
    }

    private void unlockAll(final Collection<Lock> locks)
    {
        final List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
        for (final Lock lock : locks)
        {
            try
            {
                lock.unlock();
            }
            catch (RuntimeException ex)
            {
                // Keep trying to release the others
                exceptions.add(ex);
                log.error("Error releasing lock " + lock, ex);
            }
        }
        if (!exceptions.isEmpty())
        {
            // Probably OK to ignore any others as they'll likely have the same cause
            throw exceptions.iterator().next();
        }
    }

    private void createSchemeEntities(AssignableWorkflowScheme workflowScheme)
    {
        // Create all new entities
        for (Map.Entry<String, String> mappingEntry : workflowScheme.getMappings().entrySet())
        {
            String issueTypeId = mappingEntry.getKey();
            if (issueTypeId == null)
            {
                issueTypeId = ALL_ISSUE_TYPES;
            }

            String workflowName = mappingEntry.getValue();

            SchemeEntity schemeEntity = new SchemeEntity(workflowName, issueTypeId);
            try
            {
                createSchemeEntity(workflowScheme.getId(), schemeEntity);
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    @Nonnull
    @Override
    public AssignableWorkflowScheme getWorkflowSchemeObj(@Nonnull Project project)
    {
        notNull("project", project);

        final GenericValue schemeForProject = getSchemeForProject(project);
        if (schemeForProject != null)
        {
            return toWorkflowScheme(schemeForProject);
        }
        else
        {
            return defaultScheme;
        }
    }

    @Nonnull
    @Override
    public List<Project> getProjectsUsing(@Nonnull AssignableWorkflowScheme workflowScheme)
    {
        Assertions.notNull("workflowScheme", workflowScheme);
        ImmutableList.Builder<Project> projects = new ImmutableList.Builder<Project>();

        for (Project project : projectManager.getProjectObjects())
        {
            final GenericValue schemeForProject = getSchemeForProject(project);
            if (schemeForProject == null)
            {
                if (workflowScheme.isDefault())
                {
                    projects.add(project);
                }
            }
            else
            {
                final Long id = schemeForProject.getLong("id");
                if (id != null && id.equals(workflowScheme.getId()))
                {
                    projects.add(project);
                }
            }
        }
        return projects.build();
    }

    @Override
    public AssignableWorkflowScheme cleanUpSchemeDraft(Project project, User user)
    {
        AssignableWorkflowScheme workflowScheme = getWorkflowSchemeObj(project);

        if (workflowScheme.isDefault())
        {
            return null;
        }

        List<Project> projectsUsing = getProjectsUsing(workflowScheme);
        if (projectsUsing.size() > 1)
        {
            return null;
        }

        DraftWorkflowScheme draft = getDraftForParent(workflowScheme);
        if (draft == null)
        {
            return null;
        }

        AssignableWorkflowScheme.Builder copyOfDraftBuilder = new AssignableWorkflowSchemeBuilder()
                .setName(getNameForCopy(draft))
                .setDescription(getDescriptionForCopy(user, workflowScheme))
                .setMappings(draft.getMappings());

        AssignableWorkflowScheme scheme = createScheme(copyOfDraftBuilder.build());
        deleteWorkflowScheme(draft);

        return scheme;
    }

    @Override
    public AssignableWorkflowScheme copyDraft(DraftWorkflowScheme draft, User user, String newDescription)
    {
        AssignableWorkflowScheme.Builder copyOfDraftBuilder = new AssignableWorkflowSchemeBuilder()
                .setName(getNameForCopy(draft))
                .setDescription(newDescription)
                .setMappings(draft.getMappings());

        return createScheme(copyOfDraftBuilder.build());
    }

    private String getNameForCopy(WorkflowScheme scheme)
    {
        return getNameForCopy(scheme.getName(), 255);
    }

    @Override
    public void replaceSchemeWithDraft(DraftWorkflowScheme draft)
    {
        AssignableWorkflowScheme parentScheme = getParentForDraft(draft.getId())
                .builder()
                .setMappings(draft.getMappings())
                .build();

        updateWorkflowScheme(parentScheme);

        deleteWorkflowScheme(draft);
    }

    @Override
    public AssignableWorkflowScheme.Builder assignableBuilder()
    {
        return new AssignableWorkflowSchemeBuilder();
    }

    @Override
    public DraftWorkflowScheme.Builder draftBuilder(AssignableWorkflowScheme parent)
    {
        Assertions.notNull("parent", parent);
        Assertions.notNull("parent.id", parent.getId());

        return new DraftWorkflowSchemeBuilder(parent);
    }

    String getDescriptionForCopy(User user, AssignableWorkflowScheme workflowScheme)
    {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(workflowScheme.getDescription()))
        {
            sb.append(workflowScheme.getDescription()).append(" ");
        }

        sb.append(i18nFactory.getInstance(user).getText("admin.workflowschemes.manager.draft.auto.generated", workflowScheme.getName()));

        return sb.toString();
    }

    private AssignableWorkflowScheme toWorkflowScheme(GenericValue genericValue)
    {
        if (genericValue == null)
        {
            return null;
        }
        else
        {
            String name = genericValue.getString("name");
            String description = genericValue.getString("description");
            Long id = genericValue.getLong("id");
            final Map<String, String> map = getWorkflowMap(genericValue);

            return new WorkflowSchemeImpl(id, name, description, map);
        }
    }

    private Function<AssignableWorkflowSchemeStore.AssignableState, AssignableWorkflowScheme> toAssignableFunction()
    {
        return new Function<AssignableWorkflowSchemeStore.AssignableState, AssignableWorkflowScheme>()
        {
            @Override
            public AssignableWorkflowScheme apply(AssignableWorkflowSchemeStore.AssignableState input)
            {
                return toWorkflowScheme(input);
            }
        };
    }

    private DraftWorkflowScheme toWorkflowScheme(DraftWorkflowSchemeStore.DraftState state)
    {
        if (state == null)
        {
            return null;
        }
        else
        {
            final ApplicationUser userByKey = userManager.getUserByKey(state.getLastModifiedUser());
            return new DraftWorkflowSchemeImpl(state, userByKey, getWorkflowSchemeObj(state.getParentSchemeId()));
        }
    }

    private static AssignableWorkflowScheme toWorkflowScheme(AssignableWorkflowSchemeStore.AssignableState state)
    {
        if (state == null)
        {
            return null;
        }
        else
        {
            return new WorkflowSchemeImpl(state);
        }
    }

    private class WorkflowSupplier implements Supplier<Set<String>>
    {
        @Override
        public Set<String> get()
        {
            try
            {
                final Set<String> set = new HashSet<String>();

                final Collection<GenericValue> schemes = getSchemes();
                for (final GenericValue scheme : schemes)
                {

                    // Only interested in the schemes that are associated with a project
                    if (!getProjects(scheme).isEmpty())
                    {
                        final Collection<GenericValue> entities = getEntities(scheme);
                        for (final GenericValue schemeEntity : entities)
                        {
                            set.add((String) schemeEntity.get(COLUMN_WORKFLOW));
                        }
                    }
                }

                // Check if default workflow is active i.e. a project with no associated scheme or scheme with an unassigned issue type
                // Stop searching as soon as use of default workflow is detected
                boolean checkComplete = false;

                for (final GenericValue project : projectManager.getProjects())
                {
                    final GenericValue workflowScheme = getWorkflowScheme(project);
                    if (workflowScheme == null)
                    {
                        // Default workflow in use
                        set.add(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
                        checkComplete = true;
                    }
                    else
                    {
                        // Check if an unassigned issue type exists within this scheme
                        final Collection<GenericValue> issueTypes = getAllIssueTypes();

                        for (final GenericValue issueType : issueTypes)
                        {
                            final String issueTypeId = issueType.getString("id");
                            final JiraWorkflow workflow = getWorkflowFromScheme(workflowScheme, issueTypeId);
                            if (workflow.getName().equals(JiraWorkflow.DEFAULT_WORKFLOW_NAME))
                            {
                                // Default workflow in use
                                set.add(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
                                checkComplete = true;
                            }
                            if (checkComplete)
                            {
                                break;
                            }
                        }
                    }
                    if (checkComplete)
                    {
                        break;
                    }
                }
                return Collections.unmodifiableSet(set);
            }
            catch (GenericEntityException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private class WorkflowSchemeEntitySupplier implements CacheLoader<Long, Map<String, GenericValue>> {

        @Override
        public Map<String, GenericValue> load(@Nonnull final Long workflowSchemeId)
        {
            final GenericValue scheme = getScheme(workflowSchemeId);
            if (scheme == null)
            {
                return Collections.emptyMap();
            }
            final List<GenericValue> valueList;
            try
            {
                valueList = scheme.getRelated("Child" + scheme.getEntityName() + "Entity");
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }

            final Map<String, GenericValue> schemeMap = new HashMap<String, GenericValue>();
            for (GenericValue value : valueList)
            {
                final String issueType = value.getString(COLUMN_ISSUETYPE);
                if (issueType != null)
                {
                    schemeMap.put(issueType, value);
                }
            }

            return schemeMap;
        }
    }
}
