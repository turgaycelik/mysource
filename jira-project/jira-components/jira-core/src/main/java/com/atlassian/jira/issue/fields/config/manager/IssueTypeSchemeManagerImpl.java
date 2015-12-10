package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.issue.field.config.manager.IssueTypeSchemeCreatedEvent;
import com.atlassian.jira.event.issue.field.config.manager.IssueTypeSchemeDeletedEvent;
import com.atlassian.jira.event.issue.field.config.manager.IssueTypeSchemeUpdatedEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A manager to manage {@link IssueType}'s unique set of circumstances. That is, it circumvents the scheme system by
 * collpasing the scheme and config
 */
public class IssueTypeSchemeManagerImpl implements IssueTypeSchemeManager
{
    private final FieldConfigSchemeManager configSchemeManager;
    private final OptionSetManager optionSetManager;
    private final GenericConfigManager genericConfigManager;
    private final ConstantsManager constantsManager;
    private EventPublisher eventPublisher;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public IssueTypeSchemeManagerImpl(FieldConfigSchemeManager configSchemeManager, OptionSetManager optionSetManager,
            GenericConfigManager genericConfigManager, ConstantsManager constantsManager, EventPublisher eventPublisher)
    {
        this.configSchemeManager = configSchemeManager;
        this.optionSetManager = optionSetManager;
        this.genericConfigManager = genericConfigManager;
        this.constantsManager = constantsManager;
        this.eventPublisher = eventPublisher;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public FieldConfigScheme create(String schemeName, String schemeDescription, List optionIds)
    {
        FieldConfigScheme configScheme = configSchemeManager.createFieldConfigScheme(new FieldConfigScheme.Builder().setName(schemeName).setDescription(schemeDescription).toFieldConfigScheme(),
                null,
                FieldConfigSchemeManager.ALL_ISSUE_TYPES,
                getIssueTypeField());
        FieldConfig config = configScheme.getOneAndOnlyConfig();
        optionSetManager.createOptionSet(config, optionIds);

        getFieldManager().refresh();
        eventPublisher.publish(new IssueTypeSchemeCreatedEvent(configScheme));

        return configScheme;
    }

    public FieldConfigScheme update(FieldConfigScheme configScheme, Collection optionIds)
    {
        configScheme = configSchemeManager.updateFieldConfigScheme(configScheme);

        FieldConfig config = configScheme.getOneAndOnlyConfig();
        optionSetManager.updateOptionSet(config, optionIds);
        getFieldManager().refresh();
        eventPublisher.publish(new IssueTypeSchemeUpdatedEvent(configScheme));

        return configScheme;
    }

    public FieldConfigScheme getDefaultIssueTypeScheme()
    {
        final Long schemeId = getDefaultIssueTypeSchemeId();
        return configSchemeManager.getFieldConfigScheme(schemeId);
    }

    public boolean isDefaultIssueTypeScheme(FieldConfigScheme configScheme)
    {
        return getDefaultIssueTypeSchemeId().equals(configScheme.getId());
    }


    // -------------------------------------------------------------------------------------------------- Helper Methods
    private ConfigurableField getIssueTypeField()
    {
        return getFieldManager().getConfigurableField(IssueFieldConstants.ISSUE_TYPE);
    }

    private Long getDefaultIssueTypeSchemeId()
    {
        final String s = ComponentAccessor.getApplicationProperties().getString(APKeys.DEFAULT_ISSUE_TYPE_SCHEME);
        return new Long(s);
    }

    public void addOptionToDefault(String id)
    {
        FieldConfigScheme defaultIssueTypeScheme = getDefaultIssueTypeScheme();
        List<String> optionIds = new ArrayList<String>(optionSetManager.getOptionsForConfig(defaultIssueTypeScheme.getOneAndOnlyConfig()).getOptionIds());
        optionIds.add(id);

        update(defaultIssueTypeScheme, optionIds);
    }

    public Collection<FieldConfigScheme> getAllRelatedSchemes(final String optionId)
    {
        List<FieldConfigScheme> configs = configSchemeManager.getConfigSchemesForField(getIssueTypeField());
        return Lists.newArrayList(Iterables.filter(configs, new Predicate<FieldConfigScheme>()
        {
            @Override
            public boolean apply(@Nullable FieldConfigScheme configScheme)
            {
                Collection optionIds = optionSetManager.getOptionsForConfig(configScheme.getOneAndOnlyConfig()).getOptionIds();
                return optionIds.contains(optionId);
            }
        }));
    }

    public void removeOptionFromAllSchemes(String optionId)
    {
        Collection<FieldConfigScheme> relatedSchemes = getAllRelatedSchemes(optionId);
        for (FieldConfigScheme configScheme : relatedSchemes)
        {
            Collection optionIds = optionSetManager.getOptionsForConfig(configScheme.getOneAndOnlyConfig()).getOptionIds();
            optionIds.remove(optionId);
            update(configScheme, optionIds);
        }
    }

    public void deleteScheme(FieldConfigScheme configScheme)
    {
        // Note: the following calls are now encapsulated by the FieldConfigSchemeManager#removeFieldConfigScheme() method
        // as that method has greater scope in the application.
        //   * option sets are now deleted when FieldConfigPersister#remove() is called
        //   * instead of calling FieldConfigManager#removeFieldConfig(), we use FieldConfigManager#removeConfigsForConfigScheme()
        // optionSetManager.removeOptionSet(configScheme.getOneAndOnlyConfig());
        // configManager.removeFieldConfig(configScheme.getOneAndOnlyConfig());

        configSchemeManager.removeFieldConfigScheme(configScheme.getId());
        getFieldManager().refresh();
        eventPublisher.publish(new IssueTypeSchemeDeletedEvent(configScheme));
    }

    /**
     * Retrieves all schemes and sorts them.
     *
     * Schemes with the default scheme ID are first
     * Schemes with null names are second
     * the rest are sorted on name
     *
     * @return all schemes sorted on name with default first
     */
    public List<FieldConfigScheme> getAllSchemes()
    {
        ArrayList<FieldConfigScheme> schemes = new ArrayList<FieldConfigScheme>(configSchemeManager.getConfigSchemesForField(getFieldManager().getIssueTypeField()));
        Collections.sort(schemes, new SchemeComparator());
        return schemes;
    }

    /**
     * Returns the FieldManager to use.
     * We cannot inject this because we would get a cyclic dependency.
     * Not private, so we can mock it out in Unit Tests.
     *
     * @return the FieldManager to use.
     */
    FieldManager getFieldManager()
    {
        return ComponentAccessor.getFieldManager();
    }

    public IssueType getDefaultValue(Issue issue)
    {
        FieldConfig config = configSchemeManager.getRelevantConfig(issue, getIssueTypeField());
        return getDefaultValue(config);
    }

    public IssueType getDefaultValue(FieldConfig config)
    {
        if (config != null)
        {
            String issueTypeId = (String) genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, config.getId().toString());
            return ComponentAccessor.getConstantsManager().getIssueTypeObject(issueTypeId);
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the default IssueType for a project.
     *
     * @param project
     * @return IssueType or null if there is no default
     */
    @Override
    public IssueType getDefaultValue(GenericValue project)
    {
        if (project != null)
        {
            IssueTypeField issueTypeField = getFieldManager().getIssueTypeField();
            FieldConfig relevantConfig = issueTypeField.getRelevantConfig(new IssueContextImpl(project.getLong("id"), null));
            return getDefaultValue(relevantConfig);
        }
        else
        {
            return null;
        }
    }

    @Override
    public IssueType getDefaultIssueType(Project project)
    {
        if (project != null)
        {
            IssueTypeField issueTypeField = getFieldManager().getIssueTypeField();
            FieldConfig relevantConfig = issueTypeField.getRelevantConfig(new IssueContextImpl(project.getId(), null));
            return getDefaultValue(relevantConfig);
        }
        else
        {
            return null;
        }
    }

    public void setDefaultValue(FieldConfig config, String optionId)
    {
        genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, config.getId().toString(), optionId);
        getFieldManager().refresh();
    }

    public FieldConfigScheme getConfigScheme(GenericValue project)
    {
        return configSchemeManager.getRelevantConfigScheme(new IssueContextImpl(project != null ? project.getLong("id") : null, null), getFieldManager().getIssueTypeField());
    }


    public FieldConfigScheme getConfigScheme(Project project)
    {
        return configSchemeManager.getRelevantConfigScheme(new IssueContextImpl(project.getId(), null), getFieldManager().getIssueTypeField());
    }

    public Collection<IssueType> getIssueTypesForProject(GenericValue project)
    {
        FieldConfigScheme fieldConfigScheme = getConfigScheme(project);
        return getIssueTypesForConfigScheme(fieldConfigScheme, true, true);
    }

    public Collection<IssueType> getIssueTypesForProject(Project project)
    {
        return getIssueTypesForProject(project.getGenericValue());
    }

    public Collection<IssueType> getIssueTypesForDefaultScheme()
    {
        FieldConfigScheme fieldConfigScheme = getDefaultIssueTypeScheme();
        return getIssueTypesForConfigScheme(fieldConfigScheme, true, true);
    }

    public Collection<IssueType> getNonSubTaskIssueTypesForProject(Project project)
    {
        FieldConfigScheme fieldConfigScheme = getConfigScheme(project.getGenericValue());
        return getIssueTypesForConfigScheme(fieldConfigScheme, false, true);
    }

    public Collection<IssueType> getSubTaskIssueTypesForProject(Project project)
    {
        FieldConfigScheme fieldConfigScheme = getConfigScheme(project.getGenericValue());
        return getIssueTypesForConfigScheme(fieldConfigScheme, true, false);
    }

    private Collection<IssueType> getIssueTypesForConfigScheme(FieldConfigScheme fieldConfigScheme, boolean includeSubTasks,
                                                                 boolean includeNonSubTaskIssueTypes)
    {
        FieldConfig config = fieldConfigScheme.getOneAndOnlyConfig();
        Collection<Option> options = optionSetManager.getOptionsForConfig(config).getOptions();
        Collection<IssueType> issueTypeObjects = new ArrayList<IssueType>();

        for (Option option : options)
        {
            IssueConstantOption issueTypeOption = (IssueConstantOption) option;
            final IssueType issueType = constantsManager.getIssueTypeObject(issueTypeOption.getId());
            final boolean isSubTask = issueType.isSubTask();
            if ((includeSubTasks && isSubTask) || (includeNonSubTaskIssueTypes && !isSubTask))
            {
                issueTypeObjects.add(issueType);
            }
        }

        return issueTypeObjects;
    }

    /**
     * Comparator class for the getAllSchemes function
     * Schemes with the default scheme ID are first
     * Schemes with null names are second
     * the rest are sorted on name
     */
    class SchemeComparator implements Comparator<FieldConfigScheme>
    {
        private final Long defaultId;

        SchemeComparator()
        {
            defaultId = getDefaultIssueTypeScheme().getId();
        }

        public int compare(FieldConfigScheme one, FieldConfigScheme two)
        {
            // if one is the default scheme, then make sure it floats to the top
            if (defaultId != null) {
                Long oneId = one.getId();
                Long twoId = two.getId();
                if (oneId != null && one.getId().compareTo(defaultId) == 0)
                {
                    return -1;
                }
                else if (twoId != null && two.getId().compareTo(defaultId) == 0)
                {
                    return 1;
                }
            }

            String oneName = one.getName();
            String twoName = two.getName();

            // null names float to top
            if (oneName == null)
            {
                return -1;
            }
            else if (twoName == null)
            {
                return 1;
            }
            else
            {
                return (oneName.compareToIgnoreCase(twoName));
            }
        }
    }
}
