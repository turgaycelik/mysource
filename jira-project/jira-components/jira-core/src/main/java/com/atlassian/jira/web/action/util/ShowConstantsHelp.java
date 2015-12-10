/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.SimpleStatusImpl;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Collection;
import java.util.HashSet;

/**
 * Used to Show the local instances Contants (Issue types, priorities etc)
 */
@SuppressWarnings ("UnusedDeclaration")
public class ShowConstantsHelp extends JiraWebActionSupport
{
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final PermissionManager permissionManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final StatusCategoryManager statusCategoryManager;

    public ShowConstantsHelp(ConstantsManager constantsManager, SubTaskManager subTaskManager, PermissionManager permissionManager, IssueSecurityLevelManager issueSecurityLevelManager, final StatusCategoryManager statusCategoryManager)
    {
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.permissionManager = permissionManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.statusCategoryManager = statusCategoryManager;
    }

    // Protected -----------------------------------------------------
    protected String doExecute() throws Exception
    {
        if (!permissionManager.hasProjects(Permissions.BROWSE, getLoggedInUser()))
        {
            return ERROR;
        }
        return super.doExecute();
    }

    public boolean isSubTasksEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public Collection getIssueTypes()
    {
        return constantsManager.getIssueTypes();
    }

    public IssueType getIssueType(String id)
    {
        return constantsManager.getIssueTypeObject(id);
    }

    public Collection getSubTaskIssueTypes()
    {
        if (!isSubTasksEnabled())
        {
            throw new IllegalStateException("Should not call this method as subtasks are disabled");
        }

        return constantsManager.getSubTaskIssueTypes();
    }

    public Collection getPriorities()
    {
        return constantsManager.getPriorityObjects();
    }

    public Collection getStatuses()
    {
        return constantsManager.getStatusObjects();
    }

    public boolean isStatusCategoriesEnabled()
    {
        return statusCategoryManager.isStatusAsLozengeEnabled();
    }

    public Collection<SimpleStatus> getStatusCategories()
    {
        if (!isStatusCategoriesEnabled())
        {
            throw new IllegalStateException("Should not call this method as subtasks are disabled");
        }

        Collection<SimpleStatus> categories = new HashSet<SimpleStatus>();
        for (StatusCategory category : statusCategoryManager.getStatusCategories())
        {
            categories.add(new SimpleStatusImpl(null, getNameTranslation(category), getDescTranslation(category), category, null));
        }
        return categories;
    }

    public Collection getResolutions()
    {
        return constantsManager.getResolutionObjects();
    }

    public Collection<IssueSecurityLevel> getSecurityLevels()
    {
        return issueSecurityLevelManager.getUsersSecurityLevels(getSelectedProjectObject(), getLoggedInUser());
    }

    private String getNameTranslation(StatusCategory category)
    {
        return getText("common.statuscategory." + category.getKey());
    }

    private String getDescTranslation(StatusCategory category)
    {
        return getText("common.statuscategory." + category.getKey() + ".description");
    }
}