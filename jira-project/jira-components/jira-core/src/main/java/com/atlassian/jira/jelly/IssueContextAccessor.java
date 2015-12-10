/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.ofbiz.core.entity.GenericValue;

public interface IssueContextAccessor
{
    public void setIssue(Long issueId);

    public void setIssue(String issueKey);

    public void setIssue(GenericValue issue);

    public void loadPreviousIssue();
}
