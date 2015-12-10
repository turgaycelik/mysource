/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.commons.jelly.JellyContext;
import org.ofbiz.core.entity.GenericValue;

public interface IssueAware
{
    public JellyContext getContext();

    public boolean hasIssue();

    public Long getIssueId();

    public GenericValue getIssue();
}
