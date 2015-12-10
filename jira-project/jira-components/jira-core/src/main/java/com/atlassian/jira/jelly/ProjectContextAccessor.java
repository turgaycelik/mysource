/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.ofbiz.core.entity.GenericValue;

public interface ProjectContextAccessor
{
    public void setProject(Long projectId);

    public void setProject(String projectKey);

    public void setProject(GenericValue project);

    public void loadPreviousProject();
}
