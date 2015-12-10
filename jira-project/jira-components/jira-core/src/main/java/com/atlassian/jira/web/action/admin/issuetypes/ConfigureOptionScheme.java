package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.util.JiraArrayUtils;
import com.atlassian.jira.web.action.admin.issuetypes.events.IssueTypeSchemeCreatedThroughActionEvent;
import com.atlassian.jira.web.action.admin.issuetypes.events.IssueTypeSchemeDefaultValueUpdatedThroughActionEvent;

import com.google.common.base.Function;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableList.copyOf;

public class ConfigureOptionScheme extends AbstractManageIssueTypeOptionsAction implements ExecutableAction
{
    private String name;
    private String description;
    private String defaultOption;
    private String[] selectedOptions;

    // For associating on the fly
    private Long projectId;

    protected final ConstantsManager constantsManager;
    protected final EventPublisher eventPublisher;
    private static final String[] NO_OPTIONS = new String[0];

    public ConfigureOptionScheme(final FieldConfigSchemeManager configSchemeManager,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final FieldManager fieldManager, final OptionSetManager optionSetManager,
            final IssueTypeManageableOption manageableOptionType, final BulkMoveOperation bulkMoveOperation,
            final SearchProvider searchProvider, final ConstantsManager constantsManager,
            final IssueManager issueManager,
            final EventPublisher eventPublisher)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType, bulkMoveOperation, searchProvider,
            issueManager);
        this.constantsManager = constantsManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String doDefault() throws Exception
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        if (configScheme != null)
        {
            setName(configScheme.getName());
            setDescription(configScheme.getDescription());

            // Set the default
            final IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(configScheme.getOneAndOnlyConfig());
            if (defaultValue != null)
            {
                setDefaultOption(defaultValue.getId());
            }
        }

        return INPUT;
    }

    public String doInput()
    {
        return INPUT;
    }

    public String doCopy() throws Exception
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        setName(getText("common.words.copyof", configScheme.getName()));
        setDescription(configScheme.getDescription());

        // Set the default
        final IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(configScheme.getOneAndOnlyConfig());
        if (defaultValue != null)
        {
            setDefaultOption(defaultValue.getId());
        }

        final Collection<Option> originalOptions = getOriginalOptions();
        final String[] optionIds = new String[originalOptions.size()];
        int i = 0;
        for (final Option option : originalOptions)
        {
            optionIds[i] = option.getId();
            i++;
        }
        setSelectedOptions(optionIds);

        // Clear the schemes
        setSchemeId(null);
        setConfigScheme(null);

        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        if (StringUtils.isBlank(name))
        {
            addError("name", getText("admin.common.errors.validname"));
        }

        if ((selectedOptions == null) || (selectedOptions.length == 0))
        {
            addErrorMessage(getText("admin.errors.issuetypes.must.select.option"));
        }
        else
        {
            final String fieldId = getManageableOption().getFieldId();
            for (final String selectedOption : selectedOptions)
            {
                final IssueConstant constant = constantsManager.getConstantObject(fieldId, selectedOption);
                if (constant == null)
                {
                    addErrorMessage(getText("admin.errors.issuetypes.invalid.option.id", selectedOption));
                }
            }
        }

        if (StringUtils.isNotBlank(getDefaultOption()) && !ArrayUtils.contains(getSelectedOptions(), getDefaultOption()))
        {
            addError("defaultOption", getText("admin.errors.issuetypes.default.option.must.be.in.selected"));
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        final FieldConfigScheme configScheme = executeUpdate();

        if (getProjectId() == null)
        {
            return getRedirect(configScheme);
        }
        else
        {
            // Associate, the returnUrl will be added in forceRedirect.
            final String redirectUrl = "SelectIssueTypeSchemeForProject.jspa?" + "&schemeId=" + configScheme.getId() + "&projectId=" + getProjectId() + "&atl_token=" + urlEncode(getXsrfToken());
            return forceRedirect(redirectUrl);
        }
    }

    public void run()
    {
        executeUpdate();
    }

    private FieldConfigScheme executeUpdate()
    {
        final Set<String> optionIds = new LinkedHashSet<String>(Arrays.asList(selectedOptions));

        FieldConfigScheme configScheme = getConfigScheme();
        if (configScheme.getId() == null)
        {
            // Create
            configScheme = issueTypeSchemeManager.create(name, description, new ArrayList<String>(optionIds));
            issueTypeSchemeManager.setDefaultValue(configScheme.getOneAndOnlyConfig(), getDefaultOption());
            eventPublisher.publish(new IssueTypeSchemeCreatedThroughActionEvent());

            log.info("Config scheme '" + configScheme.getName() + "' created successfully. ");
        }
        else
        {
            IssueType previousDefaultIssueType = issueTypeSchemeManager.getDefaultValue(configScheme.getOneAndOnlyConfig());
            if ((previousDefaultIssueType == null && !getDefaultOption().isEmpty())
                || (previousDefaultIssueType != null && !previousDefaultIssueType.getId().equals(getDefaultOption())))
            {
                eventPublisher.publish(new IssueTypeSchemeDefaultValueUpdatedThroughActionEvent());
            }
            // Update
            configScheme = issueTypeSchemeManager.update(
                new FieldConfigScheme.Builder(configScheme).setName(name).setDescription(description).toFieldConfigScheme(), optionIds);
            issueTypeSchemeManager.setDefaultValue(configScheme.getOneAndOnlyConfig(), getDefaultOption());
        }

        return configScheme;
    }

    public Collection getOptionsForScheme()
    {
        final String[] selectedOptions = getSelectedOptions();
        if (selectedOptions != null)
        {
            return getNewOptions();
        }
        else
        {
            return getOriginalOptions();
        }
    }

    public boolean isAllowEditOptions()
    {
        return true;
    }

    public Collection getAvailableOptions()
    {
        return CollectionUtils.subtract(getAllOptions(), getOptionsForScheme());
    }

    public Collection<?> getAllOptions()
    {
        final Collection<?> constants = constantsManager.getConstantObjects(getManageableOption().getFieldId());
        return copyOf(transform(constants, new Function<Object, IssueConstantOption>()
        {
            @Override
            public IssueConstantOption apply(@Nullable Object input)
            {
                return new IssueConstantOption((IssueConstant) input);
            }
        }));
    }

    public long getMaxHeight()
    {
        final Collection constantObjects = constantsManager.getConstantObjects(getManageableOption().getFieldId());
        if ((constantObjects != null) && !constantObjects.isEmpty())
        {
            return 23L * constantObjects.size();
        }
        else
        {
            return 23;
        }
    }

    @Override
    public FieldConfigScheme getConfigScheme()
    {
        if (configScheme == null)
        {
            if (schemeId != null)
            {
                configScheme = configSchemeManager.getFieldConfigScheme(schemeId);
            }
            else
            {
                configScheme = new FieldConfigScheme.Builder().setName(name).setDescription(description).setFieldId(fieldId).toFieldConfigScheme();
            }
        }

        return configScheme;
    }

    public GenericValue getProject()
    {
        return getProjectManager().getProject(getProjectId());
    }

    public Collection getTargetOptions()
    {
        final List<String> optionIds = new ArrayList<String>(Arrays.asList(getSelectedOptions()));
        return CollectionUtils.collect(optionIds, new Transformer()
        {
            String fieldId = getManageableOption().getFieldId();

            public Object transform(final Object input)
            {
                final String id = (String) input;

                return new IssueConstantOption(constantsManager.getConstantObject(fieldId, id));
            }
        });
    }

    protected Collection<Option> getNewOptions()
    {
        final String[] selectedOptions = getSelectedOptions();
        final List<Option> selectedOptionsList = new ArrayList<Option>();
        for (final String selectedOption : selectedOptions)
        {
            final IssueConstant constantObject = constantsManager.getConstantObject(getManageableOption().getFieldId(), selectedOption);
            selectedOptionsList.add(new IssueConstantOption(constantObject));
        }
        return selectedOptionsList;
    }

    protected Collection<Option> getOriginalOptions()
    {
        final FieldConfigScheme configScheme = getConfigScheme();
        final FieldConfig config = configScheme.getOneAndOnlyConfig();
        if (config != null)
        {
            return optionSetManager.getOptionsForConfig(config).getOptions();
        }

        return Collections.emptyList();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public String getDefaultOption()
    {
        return defaultOption;
    }

    public void setDefaultOption(final String defaultOption)
    {
        this.defaultOption = defaultOption;
    }

    public String[] getSelectedOptions()
    {
        return selectedOptions;
    }

    public void setSelectedOptions(final String[] selectedOptions)
    {
        // Iff the one and only select is null
        if (JiraArrayUtils.isContainsOneBlank(selectedOptions))
        {
            this.selectedOptions = NO_OPTIONS;
        }
        else
        {
            this.selectedOptions = selectedOptions;
        }
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(final Long projectId)
    {
        this.projectId = projectId;
    }
}
