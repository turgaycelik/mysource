package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectConverterImpl implements SelectConverter
{
    private final OptionsManager optionsManager;

    public SelectConverterImpl(OptionsManager optionsManager)
    {
        this.optionsManager = optionsManager;
    }

    public String getString(Option value)
    {
        if (value == null)
        {
            return ALL_STRING;
        }
        else
        {
            return value.getValue();
        }
    }

    public Option getObject(String stringValue)
    {
        if (stringValue == null || "".equals(stringValue) || ALL_STRING.equals(stringValue) || "-2".equals(stringValue))
        {
            return null;
        }
        try
        {
            Long id = Long.valueOf(stringValue);
            return optionsManager.findByOptionId(id);
        }
        catch (NumberFormatException e)
        {
            throw new FieldValidationException("Option Id '" + stringValue + "' is not a number.");
        }
    }

    public SearchContext getPossibleSearchContextFromValue(Option value, CustomField customField)
    {
        String stringValue = getString(value);
        Set<Long> projectIds = new HashSet<Long>();
        Set<String> issueTypeIds = new HashSet<String>();

        for (final FieldConfigScheme configScheme : customField.getConfigurationSchemes())
        {
            Set entries = configScheme.getConfigsByConfig().entrySet();
            for (final Object entry1 : entries)
            {
                Map.Entry entry = (Map.Entry) entry1;
                FieldConfig config = (FieldConfig) entry.getKey();
                Options options = optionsManager.getOptions(config);
                if (options.getOptionForValue(stringValue, null) != null)
                {
                    if (configScheme.isGlobal())
                    {
                        return new SearchContextImpl();
                    }

                    projectIds.addAll(configScheme.getAssociatedProjectIds());
                    issueTypeIds.addAll(configScheme.getAssociatedIssueTypeIds());
                }
            }
        }

        // Remove all values
        projectIds.remove(ALL_LONG);
        issueTypeIds.remove(ALL_STRING);

        return new SearchContextImpl(null, new ArrayList(projectIds), new ArrayList(issueTypeIds));
    }
}
