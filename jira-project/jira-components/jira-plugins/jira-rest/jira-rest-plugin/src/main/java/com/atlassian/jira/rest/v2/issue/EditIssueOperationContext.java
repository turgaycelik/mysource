package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.operation.*;

import java.util.Map;

/**
 * @since v5.0
 */
public class EditIssueOperationContext implements OperationContext
{
    @Override
    public Map getFieldValuesHolder()
    {
        return null;
    }

    @Override
    public com.atlassian.jira.issue.operation.IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }

}
