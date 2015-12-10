package com.atlassian.jira.issue.fields.config.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.entity.GenericValueFunctions;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.NotNullHashMap;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class FieldConfigSchemePersisterImpl implements FieldConfigSchemePersister
{
    // --------------------------------------------------------------------------------------------------- Entity Fields
    private static final Logger log = Logger.getLogger(FieldConfigSchemePersisterImpl.class);

    public static final String ENTITY_TABLE_NAME = "FieldConfigScheme";

    public static final String ENTITY_ID = "id";
    public static final String ENTITY_NAME = "name";
    public static final String ENTITY_DESCRIPTION = "description";
    public static final String ENTITY_FIELD = "fieldid";

    public static final String ENTITY_RELATED_TABLE_NAME = "FieldConfigSchemeIssueType";
    public static final String ENTITY_ISSUE_TYPE = "issuetype";
    public static final String ENTITY_SCHEME_ID = "fieldconfigscheme";
    public static final String ENTITY_CONFIG_ID = "fieldconfiguration";

    public static final String FK_RELATED_CONFIGS = "Related" + ENTITY_RELATED_TABLE_NAME;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final OfBizDelegator ofBizDelegator;
    private final ConstantsManager constantsManager;
    private final FieldConfigPersister fieldConfigPersister;
    private final FieldConfigContextPersister contextPersister;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public FieldConfigSchemePersisterImpl(final OfBizDelegator ofBizDelegator, final ConstantsManager constantsManager, final FieldConfigPersister fieldConfigPersister, final FieldConfigContextPersister contextPersister)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.constantsManager = constantsManager;
        this.fieldConfigPersister = fieldConfigPersister;
        this.contextPersister = contextPersister;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    public FieldConfigScheme create(final FieldConfigScheme configScheme, final ConfigurableField field)
    {
        final Map<String, Object> fields = transformToFieldsMap(configScheme);
        fields.put(ENTITY_FIELD, field.getId());

        try
        {
            final GenericValue createdGV = ofBizDelegator.createValue(ENTITY_TABLE_NAME, fields);
            final Long createdId = createdGV.getLong(ENTITY_ID);

            // Create links to the configs
            storeConfigAssociations(configScheme.getConfigs(), createdId);

            // Return the constructed object
            return transformToDomainObject(createdGV, field);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unable to create " + ENTITY_TABLE_NAME + " with values " + fields, e);
        }
    }

    public FieldConfigScheme createWithDefaultValues(final ConfigurableField field, final Map<String, FieldConfig> configs)
    {
        // Create a dummy config
        final FieldConfigScheme.Builder scheme = createBuilder();
        // scheme.setName("Default Configuration Scheme for " + field.getName());
        scheme.setName(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText(
            "admin.customfields.default.config.scheme", field.getName()));
        scheme.setDescription(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText(
            "admin.customfields.default.config.description"));
        scheme.setConfigs(configs);
        return create(scheme.toFieldConfigScheme(), field);
    }

    public Collection<FieldConfigScheme> getInvalidFieldConfigSchemeAfterIssueTypeRemoval(final IssueType issueType)
    {
        notNull("issueType", issueType);
        final Collection<FieldConfigScheme> ret = new HashSet<FieldConfigScheme>();
        // first lets lookup all the fieldConfigSchemeIssueType entries matching the issueType id provided.
        final List<GenericValue> gvs = ofBizDelegator.findByAnd(ENTITY_RELATED_TABLE_NAME, MapBuilder.build(ENTITY_ISSUE_TYPE, issueType.getId()));
        // now we need to find all the fieldConfigSchemes, for which the issue type is the
        for (final GenericValue fieldConfigSchemeIssueTypeGV : gvs)
        {
            final Long fieldConfigSchemeId = fieldConfigSchemeIssueTypeGV.getLong(ENTITY_SCHEME_ID);
            final List<GenericValue> specificSchemes = ofBizDelegator.findByAnd(ENTITY_RELATED_TABLE_NAME, MapBuilder.build(ENTITY_SCHEME_ID, fieldConfigSchemeId));
            if (specificSchemes.size() == 1)
            {
                ret.add(getFieldConfigScheme(fieldConfigSchemeId));
            }
        }
        return ret;
    }

    public void removeByIssueType(final IssueType issueType)
    {
        notNull("issueType", issueType);
        ofBizDelegator.removeByAnd(ENTITY_RELATED_TABLE_NAME, MapBuilder.build(ENTITY_ISSUE_TYPE, issueType.getId()));
    }

    public void init()
    {}

    public FieldConfigScheme update(final FieldConfigScheme configScheme)
    {
        try
        {
            final GenericValue gv = findById(configScheme.getId());
            final Map<String,Object> fields = transformToFieldsMap(configScheme);
            gv.setNonPKFields(fields);
            gv.store();

            // Recreated config relations
            removeRelatedConfigsForUpdate(configScheme, gv);
            gv.removeRelated(FK_RELATED_CONFIGS);
            storeConfigAssociations(configScheme.getConfigs(), configScheme.getId());

            return getFieldConfigScheme(configScheme.getId());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unable to store FieldConfigScheme: " + configScheme, e);
        }
    }

    // Hook point for the cached extension of this class.  This whole mess is gross and needs to be refactored,
    // but this is all I have time to do with it for now. :(
    protected void removeRelatedConfigsForUpdate(@Nonnull final FieldConfigScheme configScheme, @Nonnull final GenericValue gv) throws GenericEntityException
    {
        gv.removeRelated(FK_RELATED_CONFIGS);
    }

    public void remove(final Long fieldConfigSchemeId)
    {
        removeIfExist(fieldConfigSchemeId);
    }

    /**
     * Remove a field config from the database, returning the removed object if it exists.
     *
     * @param fieldConfigSchemeId Id of the config to be removed
     * @return the removed configuration scheme.
     */
    @Nullable
    protected FieldConfigScheme removeIfExist(final Long fieldConfigSchemeId)
    {
        try
        {
            final GenericValue gv = findById(fieldConfigSchemeId);
            if (gv == null)
            {
                return null;
            }
            gv.removeRelated(FK_RELATED_CONFIGS);
            gv.remove();
            return transformToDomainObject(gv, null);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unable to remove FieldConfigScheme with id '" + fieldConfigSchemeId + '\'', e);
        }
    }

    public FieldConfigScheme getFieldConfigScheme(final Long configSchemeId)
    {
        final GenericValue gv = findById(configSchemeId);
        return transformToDomainObject(gv, null);
    }

    public List<FieldConfigScheme> getConfigSchemesForCustomField(final ConfigurableField field)
    {
        final List<GenericValue> configs = ofBizDelegator.findByAnd(ENTITY_TABLE_NAME, MapBuilder.build(ENTITY_FIELD, field.getId()), ImmutableList.of("id ASC"));

        return CollectionUtil.transform(configs, new Function<GenericValue, FieldConfigScheme>()
        {
            public FieldConfigScheme get(final GenericValue input)
            {
                return transformToDomainObject(input, field);
            }
        });
    }

    public FieldConfigScheme getConfigSchemeForFieldConfig(final FieldConfig fieldConfig)
    {
        notNull("fieldConfig", fieldConfig);
        final Long fieldConfigId = fieldConfig.getId();
        final List<GenericValue> configs = ofBizDelegator.findByAnd(ENTITY_RELATED_TABLE_NAME, MapBuilder.build(ENTITY_CONFIG_ID, fieldConfigId));

        if (configs.isEmpty())
        {
            throw new DataAccessException(String.format("Could not find any field config schemes for field config '%d'", fieldConfigId));
        }
        else if (configs.size() > 1)
        {
            log.warn(String.format("Found more than one field config scheme for field config '%d'; returning first one", fieldConfigId));
        }

        final Long fieldConfigSchemeId = configs.get(0).getLong(ENTITY_SCHEME_ID);
        return getFieldConfigScheme(fieldConfigSchemeId);
    }

    public List<Long> getConfigSchemeIdsForCustomFieldId(final String customFieldId)
    {
        Assertions.notNull("customFieldId", customFieldId);

        final List<GenericValue> configs = ofBizDelegator.findByAnd(ENTITY_TABLE_NAME, MapBuilder.build(ENTITY_FIELD, customFieldId));

        return CollectionUtil.transform(configs, GenericValueFunctions.getLong(ENTITY_ID));
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    private Map<String, Object> transformToFieldsMap(final FieldConfigScheme configScheme)
    {
        final Map<String, Object> fields = new NotNullHashMap<String, Object>();
        fields.put(ENTITY_ID, configScheme.getId());
        fields.put(ENTITY_NAME, configScheme.getName());
        fields.put(ENTITY_DESCRIPTION, configScheme.getDescription());
        return fields;
    }

    /**
     * Transforms the generic value scheme to a {@link FieldConfigScheme} object
     *
     * @param createdGV
     *            the {@link GenericValue} of the scheme
     * @param field
     *            associated field, this may be null if not called from within the refresh code.
     * @return constructed {@link FieldConfigScheme}
     */
    @Nullable
    FieldConfigScheme transformToDomainObject(final GenericValue createdGV, final ConfigurableField field)
    {
        if (createdGV == null)
        {
            return null;
        }
        try
        {
            final FieldConfigScheme.Builder configScheme = createBuilder();

            configScheme.setId(createdGV.getLong(ENTITY_ID));
            configScheme.setName(createdGV.getString(ENTITY_NAME));
            configScheme.setDescription(createdGV.getString(ENTITY_DESCRIPTION));
            configScheme.setFieldId(createdGV.getString(ENTITY_FIELD));

            // Get the related objects
            final List<GenericValue> related = createdGV.getRelated(FK_RELATED_CONFIGS);
            if ((related != null) && !related.isEmpty())
            {
                final Map<String, FieldConfig> configs = new HashMap<String, FieldConfig>(related.size());
                for (final GenericValue gv : related)
                {
                    final IssueType issueType = constantsManager.getIssueTypeObject(gv.getString(ENTITY_ISSUE_TYPE));
                    final Long configId = gv.getLong(ENTITY_CONFIG_ID);
                    final FieldConfig config = (field != null) ? fieldConfigPersister.getFieldConfig(configId, field) : fieldConfigPersister.getFieldConfig(configId);
                    if (config != null)
                    {
                        final String key = issueType == null ? null : issueType.getId();
                        configs.put(key, config);
                    }
                }
                configScheme.setConfigs(configs);
            }

            return configScheme.toFieldConfigScheme();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unable to retrieve custom field config " + createdGV, e);
        }
    }

    private GenericValue findById(final Long configId)
    {
        return ofBizDelegator.findById(ENTITY_TABLE_NAME, configId);
    }

    private void storeConfigAssociations(final Map<String, FieldConfig> configs, final Long createdId) throws GenericEntityException
    {
        if (configs != null)
        {
            final Set<Map.Entry<String, FieldConfig>> entries = configs.entrySet();
            for (final Map.Entry<String, FieldConfig> entry : entries)
            {
                final String issueType = entry.getKey();
                final FieldConfig config = entry.getValue();
                final Map<String, Object> relatedFields = FieldMap.build(
                        ENTITY_ISSUE_TYPE, issueType,
                        ENTITY_SCHEME_ID, createdId,
                        ENTITY_CONFIG_ID, config.getId() );
                ofBizDelegator.createValue(ENTITY_RELATED_TABLE_NAME, relatedFields);
            }
        }
    }

    private FieldConfigScheme.Builder createBuilder()
    {
        final FieldConfigScheme.Builder configScheme = new FieldConfigScheme.Builder();
        configScheme.setFieldConfigContextPersister(contextPersister);

        return configScheme;
    }
}
