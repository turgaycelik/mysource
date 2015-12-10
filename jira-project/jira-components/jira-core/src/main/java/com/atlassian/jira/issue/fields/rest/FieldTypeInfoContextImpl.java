package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;

/**
 * @since v5.0
 */
public class FieldTypeInfoContextImpl implements FieldTypeInfoContext
{
    private final IssueContext issueContext;
    private final OperationContext operationContext;
    private final OrderableField oderableField;
    private final Issue issue;

    public FieldTypeInfoContextImpl(OrderableField oderableField, Issue issue, IssueContext issueContext, OperationContext operationContext)
    {
        this.oderableField = oderableField;
        this.issue = issue;
        this.issueContext = issueContext;
        this.operationContext = operationContext;
    }

    @Override
    public IssueContext getIssueContext()
    {
        return issueContext;
    }

    @Override
    public OperationContext getOperationContext()
    {
        return operationContext;
    }

    @Override
    public OrderableField getOderableField()
    {
        return oderableField;
    }

    @Override
    public Issue getIssue()
    {
        return issue;
    }
}
