/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.security;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * Interface used as a template for the different Permission Types.
 */
public interface IssueSecurityType
{
    public String getDisplayName();

    public String getType();

    public boolean doValidation(String key, Map parameters);

    /**
     * Interface for determining if a permission type has the permission
     * @param entity
     * @param argument
     * @param user
     * @see com.atlassian.jira.security.type.CurrentAssignee#hasPermission
     * @see com.atlassian.jira.security.type.CurrentReporter#hasPermission
     * @see com.atlassian.jira.security.type.ProjectLead#hasPermission
     * @see com.atlassian.jira.security.type.SingleUser#hasPermission
     * @see com.atlassian.jira.security.type.GroupDropdown#hasPermission
     */
    public boolean hasPermission(GenericValue entity, String argument, User user);
}
