/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public interface ProjectAware
{
    public Long getProjectId();

    public void setProjectId(Long projectId);

    public GenericValue getProject() throws GenericEntityException;
}
