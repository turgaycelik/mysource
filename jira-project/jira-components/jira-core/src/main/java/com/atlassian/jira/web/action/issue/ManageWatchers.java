/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.web.component.multiuserpicker.UserPickerLayoutBean;
import com.atlassian.jira.web.component.multiuserpicker.UserPickerWebComponent;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ParameterAware;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ManageWatchers extends AbstractIssueSelectAction implements ParameterAware
{
    private final WatcherManager watcherManager;
    private final VelocityTemplatingEngine templatingEngine;
    private final UserPickerSearchService searchService;
    private final PermissionManager permissionManager;
    private final CrowdService crowdService;

    private static final String NOWATCHING = "watchingnotenabled";
    public static final String REMOVE_WATCHERS_PREFIX = "stopwatch_";

    private String userNames;
    private Map params;

    public ManageWatchers(final WatcherManager watcherManager, final VelocityTemplatingEngine templatingEngine,
            final UserPickerSearchService searchService, final PermissionManager permissionManager,
            final CrowdService crowdService)
    {
        this.watcherManager = watcherManager;
        this.templatingEngine = templatingEngine;
        this.searchService = searchService;
        this.permissionManager = permissionManager;
        this.crowdService = crowdService;
    }

    public String doDefault() throws Exception
    {
        if (!isWatchingEnabled())
        {
            return NOWATCHING;
        }

        try
        {
            if (!isCanViewWatcherList())
            {
                return "securitybreach";
            }
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }

        return super.doDefault();
    }

    public String getUserPickerHtml() throws GenericEntityException
    {
        final UserPickerLayoutBean layout = getManageWatchersLayout();
        final boolean canEdit = isCanEditWatcherList();
        final Long issueId = getIssueObject().getId();
        final List<String> usernames = watcherManager.getCurrentWatcherUsernames(getIssue());
        return new UserPickerWebComponent(templatingEngine, getApplicationProperties(), searchService)
                .getHtmlForUsernames(layout, usernames, canEdit, issueId);
    }

    public boolean isWatchingEnabled()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    public boolean isWatching()
    {
        return watcherManager.isWatching(getLoggedInUser(), getIssue());
    }

    // Start the current user watching this issue
    @RequiresXsrfCheck
    public String doStartWatching() throws GenericEntityException
    {
        try
        {
            watcherManager.startWatching(getLoggedInUser(), getIssue());
        }
        catch (IssueNotFoundException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        return getRedirect("ManageWatchers!default.jspa?id=" + getId());
    }

    // Stop the current user watching this issue
    @RequiresXsrfCheck
    public String doStopWatching() throws GenericEntityException
    {
        try
        {
            watcherManager.stopWatching(getLoggedInUser(), getIssue());
        }
        catch (IssueNotFoundException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        return getRedirect("ManageWatchers!default.jspa?id=" + getId());
    }

    // Stop the user with the specified username watching this issue
    private String stopUserWatching(String username)
    {
        if (isBlank(username))
        {
            addErrorMessage(getText("watcher.error.selectuser"));
            return ERROR;
        }
        watcherManager.stopWatching(username, getIssue());
        userNames = null;
        return INPUT;

    }

    // Stop the specified users watching this issue
    @RequiresXsrfCheck
    public String doStopWatchers() throws GenericEntityException
    {
        try
        {
            // Require the MANAGE_WATCHER_LIST permission to remove other users from the watch list
            if (!isCanEditWatcherList())
            {
                return "securitybreach";
            }
        }
        catch (IssueNotFoundException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }

        final Collection <String> userNames = UserPickerWebComponent.getUserNamesToRemove(params, REMOVE_WATCHERS_PREFIX);
        for (final String userName : userNames)
        {
            stopUserWatching(userName);
        }
        return getRedirect("ManageWatchers!default.jspa?id=" + getId());
    }

    // Start the specified users watching this issue
    @RequiresXsrfCheck
    public String doStartWatchers() throws GenericEntityException
    {
        try
        {
            // Require the MANAGE_WATCHER_LIST permission to add other users to the watch list
            if (!isCanEditWatcherList())
            {
                return "securitybreach";
            }
        }
        catch (IssueNotFoundException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }

        final Collection<String> userNames = UserPickerWebComponent.getUserNamesToAdd(getUserNames());
        if (userNames.isEmpty())
        {
            addErrorMessage(getText("watcher.error.selectuser"));
            return ERROR;
        }

        boolean badUsersFound = false;
        for (final String userName : userNames)
        {
            final User user = getUser(userName);
            if (user != null)
            {
                if (isUserPermittedToSeeIssue(user))
                {
                    watcherManager.startWatching(user, getIssue());
                }
                else
                {
                    badUsersFound = true;
                    addErrorMessage(getText("watcher.error.user.cant.see.issue", userName));
                }
            }
            else
            {
                badUsersFound = true;
                addErrorMessage(getText("watcher.error.usernotfound", userName));
            }
        }

        if (badUsersFound)
        {
            setUserNames(null);
            return ERROR;
        }
        else
        {
            return getRedirect("ManageWatchers!default.jspa?id=" + getId());
        }
    }

    private boolean isUserPermittedToSeeIssue(final User user)
    {
        return permissionManager.hasPermission(Permissions.BROWSE, getIssueObject(), user);
    }

    private User getUser(String userName)
    {
        User user = crowdService.getUser(userName);
        if (user == null)
        {
            log.info("Unable to retrieve the user '" + userName + "' to add to watch list.");
        }
        return user;
    }

    public UserPickerLayoutBean getManageWatchersLayout()
    {
        return new UserPickerLayoutBean("watcher.manage", REMOVE_WATCHERS_PREFIX, "ManageWatchers!stopWatchers.jspa", "ManageWatchers!startWatchers.jspa");
    }

    public String getUserNames()
    {
        return userNames;
    }

    public void setUserNames(String userNames)
    {
        this.userNames = userNames;
    }

    // Used to name the checkboxes to determine the users to remove from the watch list
    public String getCheckboxName(User user)
    {
        return REMOVE_WATCHERS_PREFIX + user.getName();
    }

    public void setParameters(Map params)
    {
        this.params = params;
    }

    public Map getParams()
    {
        return params;
    }

    // Check permission to edit watcher list
    public boolean isCanEditWatcherList() throws GenericEntityException
    {
        return (permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, getIssueObject(), getLoggedInUser()));
    }

    // Check permission to view watcher list
    public boolean isCanViewWatcherList() throws GenericEntityException
    {
        return (permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, getIssueObject(), getLoggedInUser())
                || isCanEditWatcherList());
    }

    public boolean isCanStartWatching()
    {
        return isWatchingEnabled() && getLoggedInUser() != null && !isWatching();
    }

    public boolean isCanStopWatching()
    {
        return isWatchingEnabled() && getLoggedInUser() != null && isWatching();
    }
}
