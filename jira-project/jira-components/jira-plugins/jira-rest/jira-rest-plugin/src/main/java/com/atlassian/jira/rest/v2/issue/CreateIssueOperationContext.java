package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;

import java.util.Map;

/**
  * @since v5.0
 */
public class CreateIssueOperationContext implements OperationContext
{
    @Override
    public Map getFieldValuesHolder()
    {
        return null;
    }

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.CREATE_ISSUE_OPERATION;
    }
}
