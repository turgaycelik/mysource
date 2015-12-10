package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.operation.IssueOperation;

import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class OperationContextImpl implements OperationContext
{
    private final IssueOperation issueOperation;
    private final Map<String, Object> fieldValuesHolder;

    public OperationContextImpl(IssueOperation issueOperation, Map<String, Object> fieldValuesHolder)
    {
        this.issueOperation = issueOperation;
        this.fieldValuesHolder = fieldValuesHolder;
    }

    public Map<String, Object> getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        return issueOperation;
    }

    @SuppressWarnings ("RedundantIfStatement")
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof OperationContextImpl)) return false;

        final OperationContextImpl operationContext = (OperationContextImpl) o;

        if (fieldValuesHolder != null ? !fieldValuesHolder.equals(operationContext.fieldValuesHolder) : operationContext.fieldValuesHolder != null) return false;
        if (issueOperation != null ? !issueOperation.equals(operationContext.issueOperation) : operationContext.issueOperation != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (issueOperation != null ? issueOperation.hashCode() : 0);
        result = 29 * result + (fieldValuesHolder != null ? fieldValuesHolder.hashCode() : 0);
        return result;
    }
}
