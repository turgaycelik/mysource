package com.atlassian.jira.issue.fields.layout.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.association.NodeAssocationType;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeAddedToProjectEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeCopiedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeCreatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeDeletedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityCreatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityRemovedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityUpdatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.enterprise.ImmutableFieldConfigurationScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.ozymandias.SafePluginPointAccess;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@EventComponent
public class DefaultFieldLayoutManager extends AbstractFieldLayoutManager implements FieldLayoutManager
{
    private static final String PROJECT_ENTITY_NAME = "Project";
    private static final String FIELD_LAYOUT_SCHEME_ASSOCIATION = "FieldLayoutScheme";
    private static final NodeAssocationType NODE_ASSOCATION_TYPE = new NodeAssocationType(SchemeManager.PROJECT_ASSOCIATION, "Project", SCHEME);

    private static final Logger log = Logger.getLogger(DefaultFieldLayoutManager.class);

    /**
     * Cache of Project ID to Field Configuration (FieldLayout) Scheme ID.
     */
    private final Cache<Long, CacheObject<Long>> fieldSchemeCache;

    /**
     * Cache of Immutables objects that give us the FieldConfigurationScheme mapping of IssueType to FieldLayoutId.
     *
     * See JRA-16870. This replaced the old schemeCache of FieldLayoutScheme objects.
     * FieldLayoutScheme is mutable, and therefore had to lock on certain operations, causing major lock contention issues.
     */
    private final Cache<Long, ImmutableFieldConfigurationScheme> fieldConfigurationSchemeCache;

    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final ProjectManager projectManager;
    private final EventPublisher eventPublisher;
    private final NodeAssociationStore nodeAssociationStore;

    public DefaultFieldLayoutManager(final FieldManager fieldManager, final OfBizDelegator ofBizDelegator,
            final ConstantsManager constantsManager, final SubTaskManager subTaskManager,
            final ProjectManager projectManager, final I18nHelper.BeanFactory i18nFactory,
            final NodeAssociationStore nodeAssociationStore, final CacheManager cacheManager,
            final EventPublisher eventPublisher)
    {
        super(fieldManager, ofBizDelegator, i18nFactory, cacheManager);
        this.projectManager = projectManager;
        this.eventPublisher = eventPublisher;
        this.constantsManager = notNull("constantsManager", constantsManager);
        this.subTaskManager = notNull("subTaskManager", subTaskManager);
        this.nodeAssociationStore = nodeAssociationStore;

        fieldSchemeCache = cacheManager.getCache(DefaultFieldLayoutManager.class.getName() + ".fieldSchemeCache",
                new FieldSchemeCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        fieldConfigurationSchemeCache = cacheManager.getCache(DefaultFieldLayoutManager.class.getName() + ".fieldConfigurationSchemeCache",
                new ConfigurationSchemeCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public FieldLayout getFieldLayout(GenericValue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("GenericValue must be an issue. It is a(n) " + issue.getEntityName() + '.');
        }

        return getFieldLayout(projectManager.getProjectObj(issue.getLong("project")), issue.getString("type"));
    }

    public FieldLayout getFieldLayout(Project project, String issueTypeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project cannot be null.");
        }

        // Retrieve the scheme from the project
        FieldConfigurationScheme fieldConfigurationScheme = getFieldConfigurationScheme(project);

        if (fieldConfigurationScheme != null)
        {
            // Lookup the Field Layout id for the issue type
            Long fieldLayoutId = fieldConfigurationScheme.getFieldLayoutId(issueTypeId);
            // Retrieve the field layout for the given id
            return getRelevantFieldLayout(fieldLayoutId);
        }
        else
        {
            // If the project is not associated with any field layout schemes use the system default
            return getFieldLayout();
        }
    }

    public FieldLayout getFieldLayout(GenericValue project, String issueTypeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Generic Value cannot be null.");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("Generic Value must be a Project - '" + project.getEntityName() + "' is not.");
        }

        // Retrieve the scheme from the project
        FieldConfigurationScheme fieldConfigurationScheme = getFieldConfigurationScheme(project);

        if (fieldConfigurationScheme != null)
        {
            // Lookup the Field Layout id for the issue type
            Long fieldLayoutId = fieldConfigurationScheme.getFieldLayoutId(issueTypeId);
            // Retrieve the field layout for the given id
            return getRelevantFieldLayout(fieldLayoutId);
        }
        else
        {
            // If the project is not associated with any field layout schemes use the system default
            return getFieldLayout();
        }
    }

    private ImmutableFieldConfigurationScheme buildFieldConfigurationScheme(final GenericValue fieldLayoutSchemeGV)
    {
        Assertions.notNull("fieldLayoutSchemeGV", fieldLayoutSchemeGV);
        // Get the Scheme entities (these are the mappings from IssueTypeId -> FieldLayoutId)
        final Collection<GenericValue> schemeEntities = getFieldLayoutSchemeEntityGVs(fieldLayoutSchemeGV.getLong("id"));
        return new ImmutableFieldConfigurationScheme(fieldLayoutSchemeGV, schemeEntities);
    }

    private Collection<GenericValue> getFieldLayoutSchemeEntityGVs(Long fieldLayoutSchemeId)
    {
        try
        {
            return ofBizDelegator.findByField("FieldLayoutSchemeEntity", "scheme", fieldLayoutSchemeId);
        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while retrieving field layout scheme entities from the database.", e);
        }
    }

    public List<FieldLayoutScheme> getFieldLayoutSchemes()
    {
        List<FieldLayoutScheme> fieldLayoutSchemes = new LinkedList<FieldLayoutScheme>();
        List<GenericValue> fieldLayoutSchemeGVs = ofBizDelegator.findAll(SCHEME, Collections.singletonList("name ASC"));
        for (final GenericValue fieldLayoutSchemeGV : fieldLayoutSchemeGVs)
        {
            fieldLayoutSchemes.add(buildFieldLayoutScheme(fieldLayoutSchemeGV));
        }

        return fieldLayoutSchemes;
    }

    public Collection<GenericValue> getRelatedProjects(FieldLayout fieldLayout)
    {
        Collection<GenericValue> relatedProjects = new ArrayList<GenericValue>();
        // Find all the custom schemes that use this fieldLayout
        for (final FieldConfigurationScheme fieldConfigurationScheme : getFieldConfigurationSchemes(fieldLayout))
        {
            // For each scheme, we add all projects that use that scheme
            relatedProjects.addAll(getProjects(fieldConfigurationScheme));
        }
        // If the fieldLayout is the Default one, then we need to consider Projects that use the Default FieldConfigurationScheme
        if (fieldLayout.isDefault())
        {
            relatedProjects.addAll(getProjects((FieldConfigurationScheme) null));
        }
        
        return relatedProjects;
    }

    public List<EditableFieldLayout> getEditableFieldLayouts()
    {
        List<EditableFieldLayout> fieldLayouts = new LinkedList<EditableFieldLayout>();
        // Retrieve the default field layout
        fieldLayouts.add(getEditableDefaultFieldLayout());

        // Get all non-default field layouts
        List<GenericValue> fieldLayoutGVs = ofBizDelegator.findByField("FieldLayout", "type", null, "name");
        for (final GenericValue editableFieldLayoutGV : fieldLayoutGVs)
        {
            FieldLayout fieldLayout = getRelevantFieldLayout(editableFieldLayoutGV.getLong("id"));
            fieldLayouts.add(new EditableFieldLayoutImpl(fieldLayout.getGenericValue(), fieldLayout.getFieldLayoutItems()));
        }

        return fieldLayouts;
    }

    public EditableFieldLayout getEditableFieldLayout(Long id)
    {
        FieldLayout fieldLayout = getRelevantFieldLayout(id);
        return new EditableFieldLayoutImpl(fieldLayout.getGenericValue(), fieldLayout.getFieldLayoutItems());
    }

    public void updateFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        if (!TextUtils.stringSet(fieldLayoutScheme.getName()))
        {
            throw new IllegalArgumentException("Name passed must not be null.");
        }

        try
        {
            FieldLayoutScheme originalScheme = getMutableFieldLayoutScheme(fieldLayoutScheme.getId());

            // The field layout scheme might have been cached a few times. So take the conservative approach
            // and clear the whole cache.
            clearCaches();

            // Now update the scheme
            fieldLayoutScheme.getGenericValue().store();

            if (fieldLayoutScheme.getEntities() != null)
            {
                for (final FieldLayoutSchemeEntity fieldLayoutSchemeEntity : fieldLayoutScheme.getEntities())
                {
                    fieldLayoutSchemeEntity.store();
                }
            }

            eventPublisher.publish(new FieldLayoutSchemeUpdatedEvent(fieldLayoutScheme, originalScheme));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void deleteFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            // Remove any project assoications to the this scheme
            nodeAssociationStore.removeAssociationsFromSink(fieldLayoutScheme.getGenericValue());

            // Remove the scheme
            fieldLayoutScheme.getGenericValue().remove();

            // Reset the caches
            refresh();

            eventPublisher.publish(new FieldLayoutSchemeDeletedEvent(fieldLayoutScheme.getId(), fieldLayoutScheme.getName()));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void deleteFieldLayout(FieldLayout fieldLayout)
    {
        try
        {
            GenericValue genericValue = fieldLayout.getGenericValue();
            if (genericValue != null)
            {
                genericValue.removeRelated("ChildFieldLayoutItem");
                genericValue.remove();
            }

            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeEntities(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            List<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities = new LinkedList<FieldLayoutSchemeEntity>();

            List<GenericValue> fieldLayoutSchemeEntityGVs = ofBizDelegator.findByField("FieldLayoutSchemeEntity", "scheme", fieldLayoutScheme.getId());
            for (final GenericValue fieldLayoutSchemeEntityGV : fieldLayoutSchemeEntityGVs)
            {
                FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(this, fieldLayoutSchemeEntityGV, ComponentAccessor.getConstantsManager());
                fieldLayoutSchemeEntity.setFieldLayoutScheme(fieldLayoutScheme);
                fieldLayoutSchemeEntities.add(fieldLayoutSchemeEntity);
            }

            return fieldLayoutSchemeEntities;

        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while retrieving field layout scheme entities from the database.", e);
        }
    }

    public void createFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        createFieldLayoutSchemeEntity(fieldLayoutSchemeEntity.getFieldLayoutScheme(), fieldLayoutSchemeEntity.getIssueTypeId(),
                fieldLayoutSchemeEntity.getFieldLayoutId());
    }

    @Override
    public void updateFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        final Option<FieldLayoutSchemeEntity> originalEntity = Iterables.first(Collections2.filter(getFieldLayoutSchemeEntities(fieldLayoutSchemeEntity.getFieldLayoutScheme()),
                new Predicate<FieldLayoutSchemeEntity>()
                {
                    @Override
                    public boolean apply(@Nullable final FieldLayoutSchemeEntity input)
                    {
                        return StringUtils.equals(input.getIssueTypeId(), fieldLayoutSchemeEntity.getIssueTypeId());
                    }
                }));

        try
        {
            fieldLayoutSchemeEntity.getGenericValue().store();

            eventPublisher.publish(new FieldLayoutSchemeEntityUpdatedEvent(fieldLayoutSchemeEntity.getFieldLayoutScheme(),
                    originalEntity.getOrElse(fieldLayoutSchemeEntity), fieldLayoutSchemeEntity));

            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while persisting field layout scheme entity.", e);
        }
    }

    protected void removeFieldLayoutSchemeEntityNoEvent(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        try
        {
            final FieldLayoutScheme fieldLayoutScheme = fieldLayoutSchemeEntity.getFieldLayoutScheme();
            ((FieldLayoutSchemeImpl) fieldLayoutScheme).flushEntity(fieldLayoutSchemeEntity);
            fieldLayoutSchemeEntity.setFieldLayoutScheme(null);
            fieldLayoutSchemeEntity.getGenericValue().remove();

            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while removing field layout scheme entity.", e);
        }
    }

    public void removeFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        final FieldLayoutScheme scheme = fieldLayoutSchemeEntity.getFieldLayoutScheme();

        removeFieldLayoutSchemeEntityNoEvent(fieldLayoutSchemeEntity);

        eventPublisher.publish(new FieldLayoutSchemeEntityRemovedEvent(scheme, fieldLayoutSchemeEntity));
    }

    public void removeFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            for (final FieldLayoutSchemeEntity fieldLayoutSchemeEntity : fieldLayoutScheme.getEntities())
            {
                removeFieldLayoutSchemeEntityNoEvent(fieldLayoutSchemeEntity);
            }

            fieldLayoutScheme.getGenericValue().remove();
            clearCaches();

            eventPublisher.publish(new FieldLayoutSchemeDeletedEvent(fieldLayoutScheme.getId(), fieldLayoutScheme.getName()));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public Collection<FieldConfigurationScheme> getFieldConfigurationSchemes(FieldLayout fieldLayout)
    {
        // The Default FieldLayout will have a real ID if it has been customized, however the FieldLayoutSchemeEntity
        // will continue to use null to represent this FieldLayout
        Long id = fieldLayout.isDefault() ? null : fieldLayout.getId();

        // Build up a set of unique scheme ID's.
        Set<Long> fieldLayoutSchemeIds = new HashSet<Long>();
        // Find the FieldLayoutSchemeEntity's that include this fieldlayout
        List<GenericValue> fieldLayoutSchemeEntitiyGVs = ofBizDelegator.findByField("FieldLayoutSchemeEntity", "fieldlayout", id);
        for (final GenericValue fieldLayoutSchemeEntitiyGV : fieldLayoutSchemeEntitiyGVs)
        {
            fieldLayoutSchemeIds.add(fieldLayoutSchemeEntitiyGV.getLong("scheme"));
        }

        // Now turn our set of ID's into a Collection of scheme objects.
        Set<FieldConfigurationScheme> fieldConfigurationSchemes = new HashSet<FieldConfigurationScheme>(fieldLayoutSchemeIds.size());
        for (final Long schemeId : fieldLayoutSchemeIds)
        {
            fieldConfigurationSchemes.add(getFieldConfigurationScheme(schemeId));
        }

        return fieldConfigurationSchemes;
    }

    public void restoreSchemeFieldLayout(GenericValue scheme)
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Scheme passed must not be null.");
        }
        restoreFieldLayout(scheme.getLong("id"));
    }

    public Collection<GenericValue> getProjects(FieldConfigurationScheme fieldConfigurationScheme)
    {
        if (fieldConfigurationScheme == null)
        {
            return getProjectsWithDefaultFieldConfigurationScheme();
        }
        GenericValue fieldConfigurationSchemeGV = makeFieldLayoutSchemeGenericValue(fieldConfigurationScheme.getId());
        return nodeAssociationStore.getSourcesFromSink(fieldConfigurationSchemeGV, "Project", SchemeManager.PROJECT_ASSOCIATION);
    }

    private Collection<GenericValue> getProjectsWithDefaultFieldConfigurationScheme()
    {
        final Collection<GenericValue> projects = new ArrayList<GenericValue>();
        for (final GenericValue project : projectManager.getProjects())
        {
            if (getFieldConfigurationScheme(project) == null)
            {
                projects.add(project);
            }
        }
        return projects;
    }

    public Collection<GenericValue> getProjects(FieldLayoutScheme fieldLayoutScheme)
    {
        return nodeAssociationStore.getSourcesFromSink(fieldLayoutScheme.getGenericValue(), "Project", SchemeManager.PROJECT_ASSOCIATION);
    }

    @Override
    public FieldLayoutScheme createFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        return createFieldLayoutScheme(fieldLayoutScheme.getName(), fieldLayoutScheme.getDescription());
    }

    @Override
    public FieldLayoutScheme createFieldLayoutScheme(@Nonnull final String name, @Nullable final String description)
    {
        final FieldLayoutScheme fieldLayoutScheme = createFieldLayoutSchemeNoEvent(name, description);

        createFieldLayoutSchemeEntityNoEvent(fieldLayoutScheme, null, null);

        eventPublisher.publish(new FieldLayoutSchemeCreatedEvent(fieldLayoutScheme));

        return fieldLayoutScheme;
    }

    public FieldLayoutSchemeEntity createFieldLayoutSchemeEntity(@Nonnull final FieldLayoutScheme fieldLayoutScheme,
            @Nullable final String issueTypeId, @Nullable final Long fieldLayoutId)
    {
        final FieldLayoutSchemeEntity entity = createFieldLayoutSchemeEntityNoEvent(fieldLayoutScheme, issueTypeId, fieldLayoutId);

        eventPublisher.publish(new FieldLayoutSchemeEntityCreatedEvent(fieldLayoutScheme, entity));

        return entity;
    }

    protected FieldLayoutSchemeEntity createFieldLayoutSchemeEntityNoEvent(@Nonnull final FieldLayoutScheme fieldLayoutScheme,
            @Nullable final String issueTypeId, @Nullable final Long fieldLayoutId)
    {
        final FieldLayoutSchemeEntity entity = new FieldLayoutSchemeEntityImpl(this, null, ComponentAccessor.getConstantsManager());
        entity.setIssueTypeId(issueTypeId);
        entity.setFieldLayoutId(fieldLayoutId);
        entity.setFieldLayoutScheme(fieldLayoutScheme);

        final GenericValue fieldLayoutSchemeEntityGV = EntityUtils.createValue("FieldLayoutSchemeEntity",
                MapBuilder.<String, Object>build("scheme", fieldLayoutScheme.getId(),
                        "issuetype", issueTypeId,
                        "fieldlayout", fieldLayoutId));

        entity.setGenericValue(fieldLayoutSchemeEntityGV);

        clearCaches();

        ((FieldLayoutSchemeImpl) fieldLayoutScheme).cacheEntity(entity);
        return entity;
    }

    private FieldLayoutScheme createFieldLayoutSchemeNoEvent(final String name, final String description)
    {
        final FieldLayoutScheme fieldLayoutScheme = new FieldLayoutSchemeImpl(this, null);
        fieldLayoutScheme.setName(name);
        fieldLayoutScheme.setDescription(description);

        GenericValue genericValue = EntityUtils.createValue(SCHEME, FieldMap.build("name", fieldLayoutScheme.getName(), "description", fieldLayoutScheme.getDescription()));
        fieldLayoutScheme.setGenericValue(genericValue);

        return fieldLayoutScheme;
    }

    @Override
    public FieldLayoutScheme copyFieldLayoutScheme(@Nonnull final FieldLayoutScheme scheme, @Nonnull final String name, @Nullable final String description)
    {
        FieldLayoutScheme fieldLayoutScheme = createFieldLayoutSchemeNoEvent(name, description);

        for (FieldLayoutSchemeEntity fieldLayoutSchemeEntity : scheme.getEntities())
        {
            createFieldLayoutSchemeEntityNoEvent(fieldLayoutScheme, fieldLayoutSchemeEntity.getIssueTypeId(), fieldLayoutSchemeEntity.getFieldLayoutId());
        }

        eventPublisher.publish(new FieldLayoutSchemeCopiedEvent(scheme, fieldLayoutScheme));

        return fieldLayoutScheme;
    }

    private FieldLayoutScheme buildFieldLayoutScheme(GenericValue genericValue)
    {
        if (genericValue != null)
        {
            return new FieldLayoutSchemeImpl(this, genericValue);
        }
        else
        {
            return null;
        }
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
    {
        return fieldConfigurationSchemeCache.get(schemeId);
    }

    public FieldLayoutScheme getMutableFieldLayoutScheme(Long schemeId)
    {
        return buildFieldLayoutScheme(ofBizDelegator.findById(SCHEME, schemeId));
    }

    public boolean fieldConfigurationSchemeExists(String schemeName)
    {
        return !ofBizDelegator.findByField(SCHEME, "name", schemeName).isEmpty();
    }

    public Set<FieldLayout> getUniqueFieldLayouts(Project project)
    {
        final Set<FieldLayout> uniqueLayouts = new HashSet<FieldLayout>();
        final FieldConfigurationScheme scheme = getFieldConfigurationScheme(project.getGenericValue());
        if (scheme != null)
        {
            // Run through all the layouts id's for the scheme and resolve them
            for (Long layoutId : scheme.getAllFieldLayoutIds(constantsManager.getAllIssueTypeIds()))
            {
                final FieldLayout fieldLayout = getFieldLayout(layoutId);
                if (fieldLayout != null)
                {
                    uniqueLayouts.add(fieldLayout);
                }
            }
        }
        else
        {
            // If the project is not associated with any field layout schemes use the system default
            CollectionUtils.addIgnoreNull(uniqueLayouts, SafePluginPointAccess.call(new Callable<FieldLayout>()
            {
                @Override
                public FieldLayout call() throws Exception
                {
                    return getFieldLayout();
                }
            }).getOrNull());
        }

        return uniqueLayouts;
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }
        return getFieldConfigurationScheme(project.getGenericValue());
    }

    @Nullable
    public FieldConfigurationScheme getFieldConfigurationScheme(GenericValue project)
    {
        if (project == null)
        {
            log.error("Project passed must not be null.");
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        final CacheObject<Long> cacheObject = fieldSchemeCache.get(project.getLong("id"));
        if (cacheObject.getValue() == null)
        {
            return null;
        }
        return getFieldConfigurationScheme(cacheObject.getValue());
    }

    @Override
    public void addSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        addSchemeAssociation(projectManager.getProjectObj(project.getLong("id")), fieldLayoutSchemeId);
    }

    @Override
    public void addSchemeAssociation(Project project, Long fieldLayoutSchemeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        // Get old association
        final FieldConfigurationScheme oldScheme = getFieldConfigurationScheme(project);
        if (oldScheme != null)
        {
            // Remove old association
            removeSchemeAssociation(project, oldScheme.getId());
        }

        if (fieldLayoutSchemeId != null)
        {
            nodeAssociationStore.createAssociation(NODE_ASSOCATION_TYPE, project.getId(), fieldLayoutSchemeId);

            clearCaches();

            eventPublisher.publish(new FieldLayoutSchemeAddedToProjectEvent(getMutableFieldLayoutScheme(fieldLayoutSchemeId), project));
        }
    }

    /**
     * Makes a simple FieldLayoutScheme GenericValue with just the "id" field populated.
     * <p>Note that this GenericValue exists in memory only - it is not persisted.
     * <p>This is used to pass to the AssociationManager, which requires GenericValue in its arguments.
     *
     * @param fieldLayoutSchemeId FieldLayoutScheme ID
     * @return a simple FieldLayoutScheme GenericValue with just the "id" field populated.
     */
    private GenericValue makeFieldLayoutSchemeGenericValue(final Long fieldLayoutSchemeId)
    {
        GenericValue gvFieldLayoutScheme = ofBizDelegator.makeValue(SCHEME);
        gvFieldLayoutScheme.set("id", fieldLayoutSchemeId);
        return gvFieldLayoutScheme;
    }

    @Override
    public void removeSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project cannot be null.");
        }

        removeSchemeAssociation(projectManager.getProjectObj(project.getLong("id")), fieldLayoutSchemeId);
    }

    @Override
    public void removeSchemeAssociation(Project project, Long fieldLayoutSchemeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        nodeAssociationStore.removeAssociation(NODE_ASSOCATION_TYPE, project.getId(), fieldLayoutSchemeId);

        // Clear the caches
        clearCaches();

        final FieldLayoutScheme mutableFieldLayoutScheme = getMutableFieldLayoutScheme(fieldLayoutSchemeId);
        if (mutableFieldLayoutScheme != null)
        {
            eventPublisher.publish(new FieldLayoutSchemeRemovedFromProjectEvent(mutableFieldLayoutScheme, project));
        }
    }

    public FieldLayout getFieldLayout(final Long id)
    {
        return getRelevantFieldLayout(id);
    }

    public void refresh()
    {
        clearCaches();
        super.refresh();
    }

    protected void clearCaches()
    {
        fieldSchemeCache.removeAll();
        fieldConfigurationSchemeCache.removeAll();
    }

    public boolean isFieldLayoutSchemesVisiblyEquivalent(Long fieldConfigurationSchemeId1, Long fieldConfigurationSchemeId2)
    {
        // short circuit for comparing the system default with itself.
        if (fieldConfigurationSchemeId1 == null && fieldConfigurationSchemeId2 == null)
        {
            return true;
        }
        FieldConfigurationScheme scheme1 = getNotNullFieldConfigurationScheme(fieldConfigurationSchemeId1);
        FieldConfigurationScheme scheme2 = getNotNullFieldConfigurationScheme(fieldConfigurationSchemeId2);

        // Check the mapped FieldConfiguration for each Issue Type
        for (String issueType : getAllRelevantIssueTypeIds())
        {
            if (!isFieldLayoutsVisiblyEquivalent(scheme1.getFieldLayoutId(issueType), scheme2.getFieldLayoutId(issueType)))
            {
                return false;
            }
        }
        // All checks OK
        return true;
    }

    private FieldConfigurationScheme getNotNullFieldConfigurationScheme(final Long fieldConfigurationSchemeId)
    {
        if (fieldConfigurationSchemeId == null)
        {
            return new DefaultFieldConfigurationScheme();
        }
        else
        {
            return getFieldConfigurationScheme(fieldConfigurationSchemeId);
        }
    }

    public boolean isFieldLayoutsVisiblyEquivalent(final Long fieldLayoutId1, final Long fieldLayoutId2)
    {
        final Map<String, Boolean> map1 = createFieldIdToVisibilityMap(fieldLayoutId1);
        final Map<String, Boolean> map2 = createFieldIdToVisibilityMap(fieldLayoutId2);
        return map1.equals(map2);
    }

    private Map<String, Boolean> createFieldIdToVisibilityMap(final Long fieldLayoutId)
    {
        FieldLayout fieldLayout = getFieldLayout(fieldLayoutId);
        final List<FieldLayoutItem> list = fieldLayout.getFieldLayoutItems();
        final Map<String, Boolean> map = Maps.newHashMapWithExpectedSize(list.size());
        for (FieldLayoutItem item : list)
        {
            map.put(item.getOrderableField().getId(), item.isHidden());
        }
        return map;
    }

    ///CLOVER:OFF
    protected List<String> getAllRelevantIssueTypeIds()
    {
        if (subTaskManager.isSubTasksEnabled())
        {
            return constantsManager.getAllIssueTypeIds();
        }
        else
        {
            return CollectionUtil.transform(constantsManager.getRegularIssueTypeObjects().iterator(), new Function<IssueType, String>()
            {
                public String get(final IssueType input)
                {
                    return input.getId();
                }
            });
        }
    }
    ///CLOVER:ON

    /**
     * A FieldConfigurationScheme representing the default system scheme which usually is null.
     */
    static class DefaultFieldConfigurationScheme implements FieldConfigurationScheme
    {
        public Long getId()
        {
            return null;
        }

        public String getName()
        {
            return "Default Field Configuration Scheme";
        }

        public String getDescription()
        {
            return "";
        }

        public Long getFieldLayoutId(final String issueTypeId)
        {
            return null;
        }

        public Set<Long> getAllFieldLayoutIds(final Collection<String> allIssueTypeIds)
        {
            return Collections.singleton(null);
        }
    }

    private class FieldSchemeCacheLoader implements CacheLoader<Long, CacheObject<Long>>
    {
        @Override
        public CacheObject<Long> load(@Nonnull final Long projectId)
        {
            GenericValue fieldLayoutSchemeGV = EntityUtil.getOnly(DefaultFieldLayoutManager.this.nodeAssociationStore.getSinksFromSource(PROJECT_ENTITY_NAME, projectId, FIELD_LAYOUT_SCHEME_ASSOCIATION, SchemeManager.PROJECT_ASSOCIATION));
            if (fieldLayoutSchemeGV != null)
            {
                // Cache the ProjectId -> Scheme ID
                return new CacheObject<Long>(fieldLayoutSchemeGV.getLong("id"));
            }
            else
            {
                // Cache null value to indicate a field layout scheme is not assigned to this project
                return new CacheObject<Long>(null);
            }
        }
    }

    private class ConfigurationSchemeCacheLoader implements CacheLoader<Long, ImmutableFieldConfigurationScheme>
    {
        @Override
        public ImmutableFieldConfigurationScheme load(@Nonnull final Long schemeId)
        {
            final GenericValue fieldLayoutSchemeGV = DefaultFieldLayoutManager.this.ofBizDelegator.findById(SCHEME, schemeId);
            if (fieldLayoutSchemeGV == null)
            {
                throw new DataAccessException("No " + SCHEME + " found for id " + schemeId);
            }
            return buildFieldConfigurationScheme(fieldLayoutSchemeGV);
        }
    }
}
