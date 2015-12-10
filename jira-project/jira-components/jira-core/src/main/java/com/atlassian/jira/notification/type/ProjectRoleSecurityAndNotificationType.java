/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.type.AbstractProjectsSecurityType;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.JiraEntityUtils;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

public class ProjectRoleSecurityAndNotificationType extends AbstractProjectsSecurityType implements NotificationType, SecurityType
{
    public static final String PROJECT_ROLE = "projectrole";

    private static final Logger log = Logger.getLogger(ProjectRoleSecurityAndNotificationType.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectRoleManager projectRoleManager;
    private final ProjectFactory projectFactory;

    public ProjectRoleSecurityAndNotificationType(JiraAuthenticationContext jiraAuthenticationContext, ProjectRoleManager projectRoleManager, ProjectFactory projectFactory)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectRoleManager = projectRoleManager;
        this.projectFactory = projectFactory;
    }

    @Override
    public Query getQuery(User searcher, Project project, IssueSecurityLevel securityLevel, String parameter)
    {
        Query issueLevelQuery = new TermQuery(new Term(DocumentConstants.ISSUE_SECURITY_LEVEL, securityLevel.getId().toString()));
        Query projectQuery = new TermQuery(new Term(DocumentConstants.PROJECT_ID, project.getId().toString()));
        BooleanQuery query = new BooleanQuery();
        query.add(issueLevelQuery, BooleanClause.Occur.MUST);
        query.add(projectQuery, BooleanClause.Occur.MUST);

        return query;
    }

    // This is used by the view to configure notification types
    public Collection<ProjectRole> getProjectRoles()
    {
        return projectRoleManager.getProjectRoles();
    }

    // Methods from *both* SecurityType and NotificationType
    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notifications.projectrole");
    }

    public String getType()
    {
        return PROJECT_ROLE;
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        if (!doValidation(key, parameters))
        {
            String localisedMessage = jiraServiceContext.getI18nBean().getText("admin.permissions.errors.please.select.project.role");
            jiraServiceContext.getErrorCollection().addErrorMessage(localisedMessage);
        }
    }

    public boolean doValidation(final String key, final Map parameters)
    {
        Object value = parameters.get(key);
        if (StringUtils.isNotBlank((String) value))
        {
            Long roleId = new Long((String) value);
            for (ProjectRole projectRole : getProjectRoles())
            {
                if (roleId.equals(projectRole.getId()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String getArgumentDisplay(String roleTypeId)
    {
        ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(roleTypeId));
        return projectRole.getName();
    }

    // Just return it as-is.  The mapping to the role ID has already been done by the
    // user interface using drop-down options.  Converting it *again* would be wrong.
    public String getArgumentValue(String roleTypeId)
    {
        return roleTypeId;
    }


    public Set<User> getUsers(PermissionContext permissionContext, String roleId)
    {
        GenericValue projectGv = permissionContext.getProject();
        if (projectGv != null)
        {
            Project project = projectFactory.getProject(projectGv);
            return getUsersFromRole(project, roleId);
        }
        else
        {
            log.warn("returning no users because project in the permission context was null");
            return Collections.emptySet();
        }
    }

    // Methods from Security Type only
    @Override
    public boolean hasPermission(GenericValue entity, String argument)
    {
        return false;// if there is no remote user available, then it is not possible for them to have a role
    }

    @Override
    public boolean hasPermission(Project project, String argument)
    {
        // if there is no remote user available, then it is not possible for them to have a role
        return false;
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter)
    {
        // if there is no remote user available, then it is not possible for them to have a role
        return false;
    }

    @Override
    public boolean hasPermission(GenericValue entity, String argument, User user, boolean issueCreation)
    {
        GenericValue projectGv = JiraEntityUtils.getProject(entity);
        if (projectGv != null)
        {
            Project project = projectFactory.getProject(projectGv);
            ProjectRoleActors projectRoleActors = getProjectRoleActors(argument, project);
            return projectRoleActors.contains(ApplicationUsers.from(user));
        }
        else
        {
            log.warn("falling back to no permission because project in the permission context was null");
            return false;
        }
    }

    @Override
    public boolean hasPermission(Project project, String argument, User user, boolean issueCreation)
    {
        if (project == null)
            throw new IllegalArgumentException("Project passed must not be null");

        ProjectRoleActors projectRoleActors = getProjectRoleActors(argument, project);
        return projectRoleActors.contains(ApplicationUsers.from(user));
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter, User user, boolean issueCreation)
    {
        return hasPermission(issue.getProjectObject(), parameter, user, issueCreation);
    }

    // Methods from Notification Type only
    public List<NotificationRecipient> getRecipients(IssueEvent event, String roleId)
    {
        Project project = event.getIssue().getProjectObject();
        if (project == null)
        {
            return Collections.emptyList();
        }

        final Collection<User> recipients = getUsersFromRole(project, roleId);
        final List<NotificationRecipient> notificationRecipients = new ArrayList<NotificationRecipient>(recipients.size());
        for (User user : recipients)
        {
            notificationRecipients.add(new NotificationRecipient(user));
        }
        return notificationRecipients;
    }

    /**
     * Get all the users that satisfy this particular role.
     * <p/>
     * Protected access so it can be directly called from unittests
     *
     * @param project the  project you to find want users for
     * @param roleId  the specific role you want to find users for
     *
     * @return A collection of {@link User} objects
     */
    @VisibleForTesting
    protected Set<User> getUsersFromRole(Project project, String roleId)
    {
        try
        {
            ProjectRoleActors projectRoleActors = getProjectRoleActors(roleId, project);
            return projectRoleActors.getUsers();
        }
        catch (IllegalArgumentException e)
        {
            log.error("Could not resolve project role actors for the provided roleId and project", e);
            // Logging the error, but will continue to pass up an empty set.
            return Collections.emptySet();
        }
    }

    private ProjectRoleActors getProjectRoleActors(String roleId, Project project)
    {
        ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(roleId));
        return projectRoleManager.getProjectRoleActors(projectRole, project);
    }
}
