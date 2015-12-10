/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.commons.jelly.JellyContext;
import org.ofbiz.core.entity.GenericValue;

public interface ProjectAware
{
    public JellyContext getContext();

    public boolean hasProject();

    public Long getProjectId();

    public GenericValue getProject();
}
