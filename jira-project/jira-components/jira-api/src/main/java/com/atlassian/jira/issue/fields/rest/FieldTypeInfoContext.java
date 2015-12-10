package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

/**
 *
 * Context objects which holds references to {@link IssueContext}, {@link OperationContext}, {@link FieldLayoutItem}
 * and the {@link Issue}. This context is used when generating a {@link FieldTypeInfo}.
 *
 * @since v5.0
 */
@PublicApi
public interface FieldTypeInfoContext
{
    /**
     * @return an {@link IssueContext}
     */
    IssueContext getIssueContext();

    /**
     * @return a {@link OperationContext}
     */
    OperationContext getOperationContext();

    /**
     * @return a {@link OrderableField}
     */
    OrderableField getOderableField();

    /**
     * @return a {@link Issue}. NB: can be null, when requesting create meta data.
     */
    Issue getIssue();

}
