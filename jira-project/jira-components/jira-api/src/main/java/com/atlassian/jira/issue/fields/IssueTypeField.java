package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.option.Option;
import webwork.action.Action;

import java.util.Collection;
import java.util.List;

/**
 * Represents the IssueType System Field.
 *
 * @since v4.3
 */
@PublicApi
public interface IssueTypeField extends ConfigurableField, DependentField, MandatoryField, IssueConstantsField
{
    Collection<Option> getOptionsForIssue(Issue issue, boolean isSubTask);

    String getEditHtml(OperationContext operationContext, Action action, List options);

}
