/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.issue.field.CustomFieldCreatedEvent;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.CustomFieldComparators;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptors;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptors;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class DefaultCustomFieldManager implements CustomFieldManager
{
    private static final Logger log = Logger.getLogger(DefaultCustomFieldManager.class);

    private final PluginAccessor pluginAccessor;
    private final OfBizDelegator delegator;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;
    private final FieldConfigContextPersister contextPersister;
    private final FieldScreenManager fieldScreenManager;
    private final CustomFieldValuePersister customFieldValuePersister;
    private final NotificationSchemeManager notificationSchemeManager;
    private final FieldManager fieldManager;
    private final EventPublisher eventPublisher;
    private final CustomFieldFactory customFieldFactory;
    private final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors;
    private final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors;

    private final Cache<Long, CacheObject<CustomField>> customFieldsById;
    private final Cache<String, List<Long>> customFieldsByName;
    private final CachedReference<List<Long>> allCustomFieldIds;

    public DefaultCustomFieldManager(final PluginAccessor pluginAccessor,
            final OfBizDelegator delegator,
            final FieldConfigSchemeManager fieldConfigSchemeManager,
            final ConstantsManager constantsManager,
            final ProjectManager projectManager,
            final FieldConfigContextPersister contextPersister,
            final FieldScreenManager fieldScreenManager,
            final CustomFieldValuePersister customFieldValuePersister,
            final NotificationSchemeManager notificationSchemeManager,
            final FieldManager fieldManager,
            final EventPublisher eventPublisher,
            final CacheManager cacheManager,
            final CustomFieldFactory customFieldFactory,
            final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors,
            final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors)
    {
        this.pluginAccessor = pluginAccessor;
        this.delegator = delegator;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
        this.contextPersister = contextPersister;
        this.fieldScreenManager = fieldScreenManager;
        this.customFieldValuePersister = customFieldValuePersister;
        this.notificationSchemeManager = notificationSchemeManager;
        this.fieldManager = fieldManager;
        this.eventPublisher = eventPublisher;
        this.customFieldFactory = customFieldFactory;
        this.customFieldTypeModuleDescriptors = customFieldTypeModuleDescriptors;
        this.customFieldSearcherModuleDescriptors = customFieldSearcherModuleDescriptors;

        this.customFieldsById = cacheManager.getCache(DefaultCustomFieldManager.class.getName() + ".customFieldsById",
                new CustomFieldByIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
        this.customFieldsByName = cacheManager.getCache(DefaultCustomFieldManager.class.getName() + ".customFieldsByName",
                new CustomFieldByNameCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        this.allCustomFieldIds = cacheManager.getCachedReference(DefaultCustomFieldManager.class, "allCustomFieldIds",
                new AllCustomFieldIdsSupplier());

        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public CustomField createCustomField(String fieldName, String description, CustomFieldType fieldType,
                                         CustomFieldSearcher customFieldSearcher, List contexts, List issueTypes) throws GenericEntityException
    {
        final Map<String, Object> createFields = new HashMap<String, Object>();

        createFields.put(CustomField.ENTITY_NAME, StringUtils.abbreviate(fieldName, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH));
        createFields.put(CustomField.ENTITY_CF_TYPE_KEY, fieldType.getKey());

        if (StringUtils.isNotEmpty(description))
            createFields.put(CustomField.ENTITY_DESCRIPTION, description);

        if (customFieldSearcher != null)
            createFields.put(CustomField.ENTITY_CUSTOM_FIELD_SEARCHER, customFieldSearcher.getDescriptor().getCompleteKey());

        final GenericValue customFieldGV = delegator.createValue(CustomField.ENTITY_TABLE_NAME, createFields);
        final CustomField customField = customFieldFactory.create(customFieldGV);

        eventPublisher.publish(new CustomFieldCreatedEvent(customField));

        associateCustomFieldContext(customField, contexts, issueTypes);

        // add this custom field to the cache
        customFieldsById.remove(customField.getIdAsLong());
        customFieldsByName.remove(fieldName);
        allCustomFieldIds.reset();

        // refresh the IssueFieldManager
        refreshConfigurationSchemes(customField.getIdAsLong());
        fieldManager.refresh();

        return getCustomFieldObject(customFieldGV.getLong(CustomField.ENTITY_ID));
    }

    public void associateCustomFieldContext(CustomField customField, List<JiraContextNode> contexts, List<GenericValue> issueTypes)
    {
        if (contexts != null && !contexts.isEmpty())
        {
            fieldConfigSchemeManager.createDefaultScheme(customField, contexts, issueTypes);
        }
    }

    @Nonnull
    public List<CustomFieldType<?,?>> getCustomFieldTypes()
    {
        return customFieldTypeModuleDescriptors.getCustomFieldTypes();
    }

    public CustomFieldType getCustomFieldType(String key)
    {
        return customFieldTypeModuleDescriptors.getCustomFieldType(key).getOrNull();
    }

    @Nonnull
    public List<CustomFieldSearcher> getCustomFieldSearchers(CustomFieldType customFieldType)
    {
        final List<CustomFieldSearcher> allFieldSearchers = pluginAccessor.getEnabledModulesByClass(CustomFieldSearcher.class);
        List<CustomFieldSearcher> customFieldSearchers = new ArrayList<CustomFieldSearcher>();
        final CustomFieldTypeModuleDescriptor customFieldTypeDescriptor = customFieldType.getDescriptor();
        for (final CustomFieldSearcher searcher : allFieldSearchers)
        {
            // The searcher can name valid custom field types to use, or the custom field type can name valid searchers.
            if (searcher.getDescriptor().getValidCustomFieldKeys().contains(customFieldType.getKey())
                || customFieldTypeDescriptor.getValidSearcherKeys().contains(searcher.getDescriptor().getCompleteKey()))
            {
                customFieldSearchers.add(searcher);
            }
        }
        return customFieldSearchers;
    }

    public CustomFieldSearcher getCustomFieldSearcher(String key)
    {
        return customFieldSearcherModuleDescriptors.getCustomFieldSearcher(key).getOrNull();
    }

    @Nullable
    @Override
    public CustomFieldSearcher getDefaultSearcher(@Nonnull final CustomFieldType<?, ?> type)
    {
        Preconditions.checkArgument(type != null, "type == null");
        return Iterables.getFirst(getCustomFieldSearchers(type), null);
    }

    public Class<? extends CustomFieldSearcher> getCustomFieldSearcherClass(String key)
    {
        if (!ObjectUtils.isValueSelected(key))
        {
            return null;
        }

        final ModuleDescriptor<?> module = pluginAccessor.getEnabledPluginModule(key);

        if (module instanceof CustomFieldSearcherModuleDescriptor)
        {
            return ((CustomFieldSearcherModuleDescriptor)module).getModuleClass();
        }
        else
        {
            log.warn("Custom field searcher module: " + key + " is invalid. Null being returned.");
            return null;
        }
    }

    @Override
    public void refreshConfigurationSchemes(Long customFieldId)
    {
        fieldConfigSchemeManager.init();
        customFieldsById.remove(customFieldId);
    }

    /** Get all {@link CustomField}s in scope for this issue's project/type.
     */
    public List<CustomField> getCustomFieldObjects(Issue issue)
    {
        return getCustomFieldObjects(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
    }

    /** @deprecated Use {@link #getCustomFieldObjects(com.atlassian.jira.issue.Issue)} */
    public List<CustomField> getCustomFieldObjects(GenericValue issue)
    {
        return getCustomFieldObjects(issue.getLong("project"), issue.getString("type"));
    }


    public List<CustomField> getCustomFieldObjects(Long projectId, String issueTypeId)
    {
        List<String> issueTypes = issueTypeId == null ? null : Lists.newArrayList(issueTypeId);
        return getCustomFieldObjects(projectId, issueTypes);
    }

    public List<CustomField> getCustomFieldObjects(Long projectId, List<String> issueTypeIds)
    {
        List<CustomField> customFieldsInContext = new ArrayList<CustomField>();

        // Convert 2 Objects
        Project project = projectManager.getProjectObj(projectId);
        issueTypeIds = constantsManager.expandIssueTypeIds(issueTypeIds);

        // Add fields in context
        for (final CustomField customField : getCustomFieldObjects())
        {
            if (customField.isInScopeForSearch(project, issueTypeIds))
            {
                customFieldsInContext.add(customField);
            }
        }

        return customFieldsInContext;
    }

    public List<CustomField> getCustomFieldObjects(SearchContext searchContext)
    {
        List<CustomField> customFieldsInContext = new ArrayList<CustomField>();

        // Add fields in context
        for (final CustomField customField : getCustomFieldObjects())
        {
            if (customField.isInScope(searchContext))
            {
                customFieldsInContext.add(customField);
            }
        }

        return customFieldsInContext;
    }

    @Nullable
    public CustomField getCustomFieldObject(Long id)
    {
        CustomField customField = customFieldsById.get(id).getValue();
        return customField == null ? null : customFieldFactory.copyOf(customField);
    }

    @Nullable
    public CustomField getCustomFieldObject(String key)
    {
        final Long id = CustomFieldUtils.getCustomFieldId(key);
        if (id == null)
        {
            return null;
        }
        return getCustomFieldObject(id);
    }

    @Override
    public boolean exists(final String key)
    {
        final Long id = CustomFieldUtils.getCustomFieldId(key);
        return id != null && customFieldsById.get(id).hasValue();
    }

    @Nullable
    public CustomField getCustomFieldObjectByName(final String customFieldName)
    {
        Collection<CustomField> values = getCustomFieldObjectsByName(customFieldName);
        if (values == null || values.isEmpty()) { return null; }
        if (values.size() > 1)
        {
            // Should have called getCustomFieldObjectsByName instead?
            // Only dump the stack trace if debug logging is enabled - otherwise the log file can get full of rubbish.
            // Some 3rd party plugins are known to call this a lot.
            if (log.isDebugEnabled())
            {
                log.warn("Warning: returning 1 of "+values.size()+" custom fields named '" + customFieldName + '\'', new Throwable());
            }
            else
            {
                log.warn("Warning: returning 1 of " + values.size() + " custom fields named '" + customFieldName + '\'');
            }
        }
        return getCustomFieldObjectsByName(customFieldName).iterator().next();
    }

    public Collection<CustomField> getCustomFieldObjectsByName(final String customFieldName)
    {
        List<Long> ids = customFieldsByName.get(customFieldName);
        List<CustomField> customFields = getCustomFieldsFromIds(ids);
        return ImmutableList.copyOf(customFields);
    }

    public List<CustomField> getCustomFieldObjects()
    {
        List<Long> ids = allCustomFieldIds.get();
        List<CustomField> customFields = getCustomFieldsFromIds(ids);
        Collections.sort(customFields, CustomFieldComparators.byName());
        return ImmutableList.copyOf(customFields);
    }

    private List<CustomField> getCustomFieldsFromIds(final List<Long> ids)
    {
        List<CustomField> customFields = new ArrayList<CustomField>(ids.size());
        for (Long id : ids)
        {
            // Fields with invalid types don't get loaded and so will not be returned in the list.
            // This preserves current behaviour.
            CustomField customField = customFieldsById.get(id).getValue();
            if (customField != null)
            {
                customFields.add(customFieldFactory.copyOf(customField));
            }
        }
        return customFields;
    }

    public List<CustomField> getGlobalCustomFieldObjects()
    {
        return Lists.newArrayList(Iterables.filter(getCustomFieldObjects(), new Predicate<CustomField>()
        {
            public boolean apply(CustomField cf)
            {
                return cf.isGlobal();
            }
        }));
    }


    public void refresh()
    {
        fieldConfigSchemeManager.init();
        customFieldsById.removeAll();
        customFieldsByName.removeAll();
        allCustomFieldIds.reset();
        refreshSearchersAndIndexers();
   }

    private void refreshSearchersAndIndexers()
    {
        // Resets the issue search manager @todo This must be statically called since otherwise a cyclic dependency will occur. There really needs to be a CacheManager that handles all these dependent caches
        final IssueSearcherManager issueSearcherManager = ComponentAccessor.getComponent(IssueSearcherManager.class);
        issueSearcherManager.refresh();

        final FieldIndexerManager fieldIndexerManager = ComponentAccessor.getComponent(FieldIndexerManager.class);
        fieldIndexerManager.refresh();
    }

    public void clear()
    {
        customFieldsById.removeAll();
        customFieldsByName.removeAll();
        allCustomFieldIds.reset();
        ComponentAccessor.getFieldLayoutManager().refresh();
    }

    public void removeCustomFieldPossiblyLeavingOrphanedData(final Long customFieldId) throws RemoveException
    {
        Assertions.notNull("id", customFieldId);

        final CustomField originalCustomField = getCustomFieldObject(customFieldId);
        if (originalCustomField != null)
        {
            removeCustomField(originalCustomField);
        }
        else
        {
            log.debug("Couldn't load customfield object for id '" + customFieldId + "'.  Trying to lookup field directly via the db."
                    + "  Please note that deleting a custom field this way may leave some custom field data behind.");
            //couldn't find it via the manager.  Lets try to look it up via the db directly.  The customfield
            //type is no longer available via the pluginmanager.
            final GenericValue customFieldGv = delegator.findById(CustomField.ENTITY_TABLE_NAME, customFieldId);
            if (customFieldGv != null)
            {
                log.debug("Customfield with id '" + customFieldId + "' retrieved successfully via the db.");

                final String customFieldStringId = FieldManager.CUSTOM_FIELD_PREFIX + customFieldId;
                removeCustomFieldAssociations(customFieldStringId);

                customFieldValuePersister.removeAllValues(customFieldStringId);
                try
                {
                    customFieldGv.remove();
                }
                catch (GenericEntityException e)
                {
                    throw new DataAccessException("Error deleting custom field gv with id '" + customFieldId + '\'', e);
                }
                //it's not in the manager, no need to refresh the cache
                fieldManager.refresh();
            }
            else
            {
                throw new IllegalArgumentException("Tried to remove custom field with id '" + customFieldId + "' that doesn't exist!");
            }
        }
    }

    @Override
    public void removeCustomField(CustomField customField) throws RemoveException
    {
        removeCustomFieldAssociations(customField.getId());
        customField.remove();
        customFieldsById.remove(customField.getIdAsLong());
        customFieldsByName.remove(customField.getName());
        allCustomFieldIds.reset();
        refreshSearchersAndIndexers();
        fieldManager.refresh();
    }

    private void removeCustomFieldAssociations(String customFieldId) throws RemoveException
    {
        // Remove and field screen layout items of this custom field
        fieldScreenManager.removeFieldScreenItems(customFieldId);

        delegator.removeByAnd("ColumnLayoutItem", FieldMap.build("fieldidentifier", customFieldId));
        // JRA-4423 Remove any references to the customfield in the field layouts.
        delegator.removeByAnd("FieldLayoutItem", FieldMap.build("fieldidentifier", customFieldId));

        fieldConfigSchemeManager.removeInvalidFieldConfigSchemesForCustomField(customFieldId);

        // This should be triggered via an event system but until then is done explicitly
        notificationSchemeManager.removeSchemeEntitiesForField(customFieldId);
    }

    @Override
    public void removeCustomFieldValues(GenericValue issue) throws GenericEntityException
    {
        // Remove the rows in the customfieldValues
        delegator.removeByAnd("CustomFieldValue", FieldMap.build("issue", issue.getLong("id")));
    }

    @Override
    public void updateCustomField(CustomField customField)
    {
        CustomField oldField = getCustomFieldObject(customField.getIdAsLong());
        if (oldField == null)
        {
            throw new DataAccessException("Cannot update custom field that does not exist");
        }
        final GenericValue customFieldGV = customField.getGenericValue();

        try
        {
            customFieldGV.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Exception whilst trying to store genericValue " + customFieldGV + ".", e);
        }
        eventPublisher.publish(new CustomFieldUpdatedEvent(customField, oldField));
        customFieldsById.remove(customField.getIdAsLong());
        customFieldsByName.remove(customField.getName());

        String oldName = oldField.getName();
        if (!customField.getName().equals(oldName))
        {
            customFieldsByName.remove(oldName);
        }

        if (customField.getCustomFieldSearcher() != oldField.getCustomFieldSearcher())
        {
            refreshSearchersAndIndexers();
        }
        if (!areConfigSchemesEqual(customField.getConfigurationSchemes(), oldField.getConfigurationSchemes()))
        {
            fieldManager.refresh();
        }
    }

    @VisibleForTesting
    protected boolean areConfigSchemesEqual(List<FieldConfigScheme> schemes, List<FieldConfigScheme> otherSchemes)
    {
        if (schemes != null && otherSchemes != null)
        {
            return HashMultiset.create(schemes).equals(HashMultiset.create(otherSchemes));
        }
        return schemes == null && otherSchemes == null;
    }

    @Override
    public CustomField getCustomFieldInstance(GenericValue customFieldGv)
    {
        return customFieldFactory.create(customFieldGv);
    }

    @Override
    public void removeProjectAssociations(GenericValue project)
    {
        contextPersister.removeContextsForProject(project);
        refresh();
    }

    @Override
    public void removeProjectAssociations(Project project)
    {
        contextPersister.removeContextsForProject(project);
        refresh();
    }

    @Override
    public void removeProjectCategoryAssociations(ProjectCategory projectCategory)
    {
        contextPersister.removeContextsForProjectCategory(projectCategory);
        refresh();
    }

    private List<Long> getCustomFieldIds(final EntityCondition condition)
    {
        List<GenericValue> customFieldGvs = delegator.findByCondition(CustomField.ENTITY_TABLE_NAME, condition, Collections.singletonList(CustomField.ENTITY_ID));
        List<Long> ids = Lists.newArrayListWithCapacity(customFieldGvs.size());
        for (GenericValue customFieldGv : customFieldGvs)
        {
            ids.add(customFieldGv.getLong(CustomField.ENTITY_ID));
        }
        return ids;
    }

    private class CustomFieldByIdCacheLoader implements CacheLoader<Long, CacheObject<CustomField>>
    {
        @Override
        public CacheObject<CustomField> load(@Nonnull final Long id)
        {
            GenericValue customFieldGv = delegator.findById(CustomField.ENTITY_TABLE_NAME, id);
            if (customFieldGv == null)
            {
                return CacheObject.NULL();
            }
            CustomField customFieldImpl = customFieldFactory.create(customFieldGv);
            // Don't add if the customfield type is invalid
            if (customFieldImpl.getCustomFieldType() == null)
            {
                return CacheObject.NULL();
            }
            return CacheObject.wrap(customFieldImpl);
        }
    }

    private class CustomFieldByNameCacheLoader implements CacheLoader<String, List<Long>>
    {
        @Override
        public List<Long> load(@Nonnull final String name)
        {
            EntityCondition condition = new EntityExpr(CustomField.ENTITY_NAME, EntityOperator.EQUALS, name);
            return getCustomFieldIds(condition);
        }
    }

    private class AllCustomFieldIdsSupplier implements Supplier<List<Long>>
    {
        @Override
        public List<Long> get()
        {
            return getCustomFieldIds(null);
        }
    }

}
