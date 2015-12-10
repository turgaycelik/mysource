/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.operation.IssueOperation;
import java.util.Map;

@PublicApi
public interface OperationContext
{
    public Map<String, Object> getFieldValuesHolder();

    public IssueOperation getIssueOperation();
}
