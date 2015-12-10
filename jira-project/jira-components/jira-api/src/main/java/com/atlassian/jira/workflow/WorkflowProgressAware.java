/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Map;

public interface WorkflowProgressAware
{
    /**
     * @deprecated Use {@link #getRemoteApplicationUser()} instead. Since v6.0.
     */
    public User getRemoteUser();

    public ApplicationUser getRemoteApplicationUser();

    public int getAction();

    public void setAction(int action);

    public void addErrorMessage(String error);

    public void addError(String name, String error);

    public Map getAdditionalInputs();

    public MutableIssue getIssue();

    public Project getProject();

    /**
     * Legacy synonym for {@link #getProject()}
     * @return The project
     */
    public Project getProjectObject();
}
