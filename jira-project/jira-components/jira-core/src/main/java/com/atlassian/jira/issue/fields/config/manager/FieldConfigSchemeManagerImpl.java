package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.DefaultBandanaManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigSchemePersister;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigSchemePersisterImpl;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.NameComparator;
import com.atlassian.jira.util.dbc.Null;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class FieldConfigSchemeManagerImpl implements FieldConfigSchemeManager
{
    private static final Logger log = Logger.getLogger(FieldConfigSchemeManagerImpl.class);

    private final FieldConfigManager configManager;
    private final FieldConfigSchemePersister schemePersister;
    private final FieldConfigContextPersister contextPersister;
    private final DefaultBandanaManager defaultBandanaManager;
    private final JiraContextTreeManager treeManager;

    public FieldConfigSchemeManagerImpl(final FieldConfigSchemePersister configSchemePersister,
            final FieldConfigContextPersister contextPersister, final JiraContextTreeManager treeManager,
            final FieldConfigManager configManager)
    {
        defaultBandanaManager = new DefaultBandanaManager(contextPersister);

        schemePersister = configSchemePersister;
        this.contextPersister = contextPersister;
        this.treeManager = treeManager;
        this.configManager = configManager;
    }

    public void init()
    {
        defaultBandanaManager.init();
        schemePersister.init();
    }

    public Object getValue(final BandanaContext context, final String key)
    {
        return getValue(context, key, true);
    }

    public Object getValue(final BandanaContext context, final String key, final boolean lookUp)
    {
        final Long configSchemeId = (Long) defaultBandanaManager.getValue(context, key, lookUp);

        if (configSchemeId != null)
        {
            return schemePersister.getFieldConfigScheme(configSchemeId);
        }
        return null;
    }

    public void setValue(final BandanaContext context, final String key, final Object value)
    {
        contextPersister.store(context, key, value);
    }

    public List<FieldConfigScheme> getConfigSchemesForField(final ConfigurableField field)
    {
        return schemePersister.getConfigSchemesForCustomField(field);
    }

    public FieldConfigScheme getConfigSchemeForFieldConfig(final FieldConfig fieldConfig)
    {
        notNull("fieldConfig", fieldConfig);
        try
        {
            return schemePersister.getConfigSchemeForFieldConfig(fieldConfig);
        }
        catch (final DataAccessException e)
        {
            return null;
        }
    }

    public FieldConfigScheme getFieldConfigScheme(final Long configSchemeId)
    {
        if (configSchemeId != null)
        {
            return schemePersister.getFieldConfigScheme(configSchemeId);
        }

        return null;
    }

    private FieldConfigScheme createFieldConfigScheme(final FieldConfigScheme newConfigScheme, final List<JiraContextNode> contexts, final ConfigurableField field)
    {
        final FieldConfigScheme configScheme = schemePersister.create(newConfigScheme, field);

        if ((contexts != null) && !contexts.isEmpty())
        {
            storeAssociateContexts(configScheme, contexts, field);
        }
        else
        {
            log.info("ConfigScheme " + configScheme.getName() + " (" + configScheme.getId() + "). Created with no associated contexts");
        }
        return configScheme;
    }

    /*
     * This version creates a default config on the fly
     */
    public FieldConfigScheme createFieldConfigScheme(final FieldConfigScheme newConfigScheme, final List<JiraContextNode> contexts, final List<GenericValue> issueTypes, final ConfigurableField field)
    {
        final FieldConfigScheme.Builder builder = new FieldConfigScheme.Builder(newConfigScheme);
        if ((field != null) && (issueTypes != null) && !issueTypes.isEmpty())
        {
            final FieldConfig config = configManager.createWithDefaultValues(field);

            // Create the issue type mapping
            final Map<String, FieldConfig> issueTypesMappings = new HashMap<String, FieldConfig>(issueTypes.size());
            for (final GenericValue issueTypeGv : issueTypes)
            {
                final String issueTypeId = issueTypeGv == null ? null : issueTypeGv.getString(FieldConfigSchemePersisterImpl.ENTITY_ID);
                issueTypesMappings.put(issueTypeId, config);
            }

            builder.setConfigs(issueTypesMappings);
        }

        return createFieldConfigScheme(builder.toFieldConfigScheme(), contexts, field);
    }

    public FieldConfigScheme createDefaultScheme(final ConfigurableField field, final List<JiraContextNode> contexts, final List<GenericValue> issueTypes)
    {
        if ((field != null) && (issueTypes != null) && !issueTypes.isEmpty())
        {
            final FieldConfig config = configManager.createWithDefaultValues(field);

            // Create the issue type mapping
            final Map<String, FieldConfig> issueTypesMappings = new HashMap<String, FieldConfig>(issueTypes.size());
            for (final GenericValue issueTypeGv : issueTypes)
            {
                final String issueTypeId = issueTypeGv == null ? null : issueTypeGv.getString(FieldConfigSchemePersisterImpl.ENTITY_ID);
                issueTypesMappings.put(issueTypeId, config);
            }

            FieldConfigScheme defaultScheme = schemePersister.createWithDefaultValues(field, issueTypesMappings);

            if ((contexts != null) && !contexts.isEmpty())
            {
                storeAssociateContexts(defaultScheme, contexts, field);
            }
            else
            {
                log.info("ConfigScheme " + defaultScheme.getName() + " (" + defaultScheme.getId() + "). Created with no associated contexts");
            }

            return defaultScheme;
        }
        else
        {
            log.info("Incomplete information supplied. Default scheme not created.");
            return null;
        }
    }

    public FieldConfigScheme createDefaultScheme(final ConfigurableField field, final List<JiraContextNode> contexts)
    {
        return createDefaultScheme(field, contexts, ALL_ISSUE_TYPES);
    }

    public void removeSchemeAssociation(final List<JiraContextNode> contexts, final ConfigurableField configurableField)
    {
        for (final JiraContextNode contextNode : contexts)
        {
            setValue(contextNode, configurableField.getId(), null);
        }
    }

    @Override
    public List<GenericValue> getAssociatedProjects(final ConfigurableField field)
    {
        final List<FieldConfigScheme> configurations = getConfigSchemesForField(field);
        final List<GenericValue> projects = new LinkedList<GenericValue>();
        if (configurations != null)
        {
            for (final FieldConfigScheme config : configurations)
            {
                final List<GenericValue> configProject = config.getAssociatedProjects();
                if (configProject != null)
                {
                    projects.addAll(configProject);
                }
            }

            Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        }
        return Collections.unmodifiableList(projects);
    }

    @Override
    public List<Project> getAssociatedProjectObjects(final ConfigurableField field)
    {
        final List<FieldConfigScheme> configurations = getConfigSchemesForField(field);
        final List<Project> projects = new LinkedList<Project>();
        if (configurations != null)
        {
            for (final FieldConfigScheme config : configurations)
            {
                final List<Project> configProject = config.getAssociatedProjectObjects();
                if (configProject != null)
                {
                    projects.addAll(configProject);
                }
            }

            Collections.sort(projects, NameComparator.COMPARATOR);
        }
        return Collections.unmodifiableList(projects);
    }

    public FieldConfigScheme updateFieldConfigScheme(final FieldConfigScheme newScheme, final List<JiraContextNode> contexts, final ConfigurableField field)
    {
        final FieldConfigScheme configScheme = updateFieldConfigScheme(newScheme);

        contextPersister.removeContextsForConfigScheme(newScheme);
        storeAssociateContexts(configScheme, contexts, field);
        return configScheme;
    }

    public FieldConfigScheme updateFieldConfigScheme(final FieldConfigScheme scheme)
    {
        return schemePersister.update(scheme);
    }

    public void removeFieldConfigScheme(final Long fieldConfigSchemeId)
    {
        /*
         * See http://extranet.atlassian.com/display/JIRADEV/CustomField+Configuration+-+DB+Entity+Model for more
         * background information on this.
         */
        FieldConfigScheme fieldConfigScheme = getFieldConfigScheme(fieldConfigSchemeId);
        contextPersister.removeContextsForConfigScheme(fieldConfigScheme);
        configManager.removeConfigsForConfigScheme(fieldConfigSchemeId);
        schemePersister.remove(fieldConfigSchemeId);
    }

    public void removeInvalidFieldConfigSchemesForIssueType(final IssueType issueType)
    {
        @SuppressWarnings("unchecked")
        final Collection<FieldConfigScheme> fieldConfigSchemes = getInvalidFieldConfigSchemesForIssueTypeRemoval(issueType);
        for (final FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
        {
            removeFieldConfigScheme(fieldConfigScheme.getId());
        }
        // now clean up the fieldconfigschemeissue type associations.
        schemePersister.removeByIssueType(issueType);
    }

    public void removeInvalidFieldConfigSchemesForCustomField(final String customFieldId)
    {
        final List<Long> schemeIds = schemePersister.getConfigSchemeIdsForCustomFieldId(customFieldId);
        for (final Long schemeId : schemeIds)
        {
            removeFieldConfigScheme(schemeId);
        }
    }

    public Collection getInvalidFieldConfigSchemesForIssueTypeRemoval(final IssueType issueType)
    {
        Null.not("issueType", issueType);
        return schemePersister.getInvalidFieldConfigSchemeAfterIssueTypeRemoval(issueType);
    }

    public FieldConfig getRelevantConfig(final IssueContext issueContext, final ConfigurableField field)
    {
        final FieldConfigScheme scheme = getRelevantConfigScheme(issueContext, field);

        if (scheme != null)
        {
            final Map<String, FieldConfig> configs = scheme.getConfigs();
            if (configs != null)
            {
                final FieldConfig configForAnyIssueType = configs.get(null);
                if (configForAnyIssueType != null)
                {
                    return configForAnyIssueType;
                }

                final IssueType issueType = issueContext.getIssueTypeObject();
                final String issueTypeId = issueType == null ? null : issueType.getId();
                final FieldConfig config = configs.get(issueTypeId);
                if (config != null)
                {
                    return config;
                }
            }

            return null;
        }
        return null;
    }

    public FieldConfigScheme getRelevantConfigScheme(final IssueContext issueContext, final ConfigurableField field)
    {
        final JiraContextNode context = new ProjectContext(issueContext, treeManager);
        return (FieldConfigScheme) getValue(context, field.getId());
    }

    FieldConfigSchemePersister getFieldConfigSchemePersister()
    {
        return schemePersister;
    }

    private void storeAssociateContexts(final FieldConfigScheme config, final List<JiraContextNode> contexts, final ConfigurableField field)
    {
        contextPersister.store(contexts, field.getId(), config);
    }
}
