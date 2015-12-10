/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search.parameters.filter;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Predicate;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class NoBrowsePermissionPredicate implements Predicate<Issue>
{
    private final ApplicationUser searcher;
    private final PermissionManager permissionManager;

    public NoBrowsePermissionPredicate(ApplicationUser searcher)
    {
        this(searcher, ComponentAccessor.getPermissionManager());
    }

    NoBrowsePermissionPredicate(final ApplicationUser searcher, final PermissionManager permissionManager)
    {
        this.searcher = searcher;
        this.permissionManager = notNull("permissionManager", permissionManager);
    }

    /**
     * @return true if the user does not have permission to browse this issue; false otherwise.
     */
    @Override
    public boolean evaluate(Issue issue)
    {
        return !permissionManager.hasPermission(Permissions.BROWSE, issue, searcher);
    }
}
