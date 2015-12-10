/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.enterprise;

import org.ofbiz.core.entity.GenericValue;

public interface IssueSchemeLevelAware extends IssueSchemeAware
{
    public boolean hasIssueSchemeLevel();

    public Long getIssueSchemeLevelId();

    public GenericValue getIssueSchemeLevel();
}
