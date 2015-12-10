package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Represents the scope of a given custom field.
 * The scope is defined as the projects/issue types for which a custom field is visible.
 */
public class CustomFieldScopeImpl implements CustomFieldScope
{
    private final CustomField customField;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private LazyReference<List<FieldConfigScheme>> customFieldSchemes = new LazyReference<List<FieldConfigScheme>>()
    {
        @Override
        protected List<FieldConfigScheme> create() throws Exception
        {
            return fieldConfigSchemeManager.getConfigSchemesForField(customField);
        }
    };

    CustomFieldScopeImpl(final CustomField customField, final FieldConfigSchemeManager fieldConfigSchemeManager)
    {
        this.customField = customField;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
    }

    @Override
    public boolean isIncludedIn(final IssueContext issueContext)
    {
        List<FieldConfigScheme> customFieldSchemes = this.customFieldSchemes.get();
        if (customFieldSchemes == null || customFieldSchemes.isEmpty())
        {
            return false;
        }

        if (searchingForAnyProjectAndAnyIssueType(issueContext))
        {
            return true;
        }

        if (searchingForAnyProject(issueContext))
        {
            return Iterables.any(customFieldSchemes, inScopeByIssueType(issueContext));
        }

        if (searchingForAnyIssueType(issueContext))
        {
            return Iterables.any(customFieldSchemes, inScopeByProject(issueContext));
        }

        return Iterables.any(customFieldSchemes, Predicates.and(inScopeByProject(issueContext), inScopeByIssueType(issueContext)));
    }

    private Predicate<FieldConfigScheme> inScopeByProject(final IssueContext issueContext)
    {
        return new Predicate<FieldConfigScheme>()
        {
            @Override
            public boolean apply(@Nullable final FieldConfigScheme fieldScheme)
            {
                if (fieldScheme == null)
                {
                    return true;
                }
                List<Long> associatedProjectIds = fieldScheme.getAssociatedProjectIds();
                return associatedProjectIds.isEmpty() || associatedProjectIds.contains(issueContext.getProjectId());
            }
        };
    }

    private Predicate<FieldConfigScheme> inScopeByIssueType(final IssueContext issueContext)
    {
        return new Predicate<FieldConfigScheme>()
        {
            @Override
            public boolean apply(@Nullable final FieldConfigScheme fieldScheme)
            {
                if (fieldScheme == null)
                {
                    return true;
                }
                Map<String, FieldConfig> configs = fieldScheme.getConfigs();
                return configs.get(issueContext.getIssueTypeId()) != null || configs.get(null) != null;
            }
        };
    }

    private boolean searchingForAnyProjectAndAnyIssueType(IssueContext issueContext)
    {
        return issueContext == null || (searchingForAnyProject(issueContext) && searchingForAnyIssueType(issueContext));
    }

    private boolean searchingForAnyProject(IssueContext issueContext)
    {
        return issueContext.getProjectId() == null;
    }

    private boolean searchingForAnyIssueType(IssueContext issueContext)
    {
        return issueContext.getIssueTypeId() == null;
    }
}
