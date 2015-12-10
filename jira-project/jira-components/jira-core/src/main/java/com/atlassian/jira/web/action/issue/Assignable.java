/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import org.ofbiz.core.entity.GenericValue;

public interface Assignable
{
    public String getAssignee();

    public void setAssignee(String assignee);

    public GenericValue getAssignIn() throws Exception;
}
