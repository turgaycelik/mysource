/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectLead extends AbstractProjectsSecurityType
{
    public static final String DESC = "lead";
    private JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectLead(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.project.lead");
    }

    public String getType()
    {
        return DESC;
    }

    @Override
    public boolean hasPermission(GenericValue entity, String argument)
    {
        return false;
    }

    @Override
    public boolean hasPermission(Project project, String argument)
    {
        // clearly the anonymous user is not the Project Lead.
        return false;
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter)
    {
        // clearly the anonymous user is not the Project Lead.
        return false;
    }

    /**
     * Determines if the user is the project lead for the project. The current project is derived from the entity using JiraUtils.getProject.
     * If it is not then false is returned.
     *
     * @param entity        The Generic Value. Should be an Issue or a Project
     * @param argument      Not needed for this implementation
     * @param user          User to check the permission on. If it is null then the check is made on the current user
     * @param issueCreation
     * @return true if the user is the project lead otherwise false
     * @see com.atlassian.jira.security.type.CurrentAssignee#hasPermission
     * @see com.atlassian.jira.security.type.CurrentReporter#hasPermission
     * @see SingleUser#hasPermission
     * @see com.atlassian.jira.security.type.GroupDropdown#hasPermission
     */
    @Override
    public boolean hasPermission(GenericValue entity, String argument, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
    {
        if (entity == null)
            throw new IllegalArgumentException("Entity passed must NOT be null");
        if (!("Project".equals(entity.getEntityName()) || "Issue".equals(entity.getEntityName())))
            throw new IllegalArgumentException("Entity passed must be a Project or an Issue not a " + entity.getEntityName());
        if (user == null)
            throw new IllegalArgumentException("User passed must not be null");

        String projectLead = null;
        if ("Project".equals(entity.getEntityName()))
        {
            projectLead = entity.getString("lead");
        }
        else if ("Issue".equals(entity.getEntityName()))
        {
            GenericValue project = ComponentAccessor.getProjectManager().getProject(entity);
            projectLead = project.getString("lead");
        }

        //if there is a project lead user is the project lead then return true
        if (projectLead != null)
        {
            if (projectLead.equals(ComponentAccessor.getUserKeyService().getKeyForUser(user)))
                return true;
        }

        return false;
    }

    @Override
    public boolean hasPermission(Project project, String argument, User user, boolean issueCreation)
    {
        if (project == null)
            throw new IllegalArgumentException("Project passed must not be null");
        if (user == null)
            throw new IllegalArgumentException("User passed must not be null");

        String projectLead = project.getLeadUserKey();
        // User has permission if they are the lead
        return projectLead != null && projectLead.equals(ApplicationUsers.getKeyFor(user));
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter, User user, boolean issueCreation)
    {
        return hasPermission(issue.getProjectObject(), parameter, user, issueCreation);
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    public Set<User> getUsers(PermissionContext ctx, String ignored)
    {
        Project project = ctx.getProjectObject();
        ApplicationUser user = project.getProjectLead();
        Set<User> result = new HashSet<User>(1);
        if (user != null)
            result.add(user.getDirectoryUser());
        return result;
    }

    @Override
    public Query getQuery(User searcher, Project project, IssueSecurityLevel securityLevel, String parameter)
    {
        //JRA-21648 : Project Lead should not return query for issues that you have no permission for
        if (project.getLeadUserKey() == null || !project.getLeadUserKey().equals(ApplicationUsers.getKeyFor(searcher)))
            return null;

        final BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID,""+project.getId())), BooleanClause.Occur.MUST);
        query.add(super.getQuery(securityLevel), BooleanClause.Occur.MUST);
        return query;
    }
}
