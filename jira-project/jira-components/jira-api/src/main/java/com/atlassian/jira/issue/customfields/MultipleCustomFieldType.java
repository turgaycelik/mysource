package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import javax.annotation.Nullable;

/**
 * A type of custom field which provides the user with a specific set of options to choose from.
 * @param <T> Transport Object See {@link CustomFieldType} for more information.
 * @param <S> Single Form of Transport Object. See {@link CustomFieldType} for more information.
 */
@PublicSpi
public interface MultipleCustomFieldType<T, S> extends CustomFieldType<T, S>
{
    /**
     * Returns all possible Options for this field.
     * @param fieldConfig configuration for this field
     * @param jiraContextNode context
     * @return all possible Options for this field.
     */
    public Options getOptions(FieldConfig fieldConfig, @Nullable JiraContextNode jiraContextNode);

}
