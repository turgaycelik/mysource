/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.ofbiz.core.entity.GenericValue;

public interface IssueSchemeAware extends UserAware
{
    public boolean hasIssueScheme();

    public Long getIssueSchemeId();

    public GenericValue getIssueScheme();
}
