/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import com.atlassian.jira.jelly.tag.JellyUtils;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Strings;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.ofbiz.core.entity.GenericValue;

public class CreateProject extends JiraDynaBeanTagSupport implements ProjectContextAccessor
{
    private final ProjectService projectService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    private String projectKey;
    private String projectName;
    private String projectLeadUsername;
    private Long avatarId;

    private static final String KEY_PROJECTKEY = "key";
    private static final String KEY_PROJECTNAME = "name";
    private static final String KEY_PROJECTLEAD = "lead";
    private static final String KEY_AVATAR_ID = "avatarId";
    private final ProjectContextAccessor projectContextAccessor;

    public CreateProject()
    {
        this.projectService = ComponentAccessor.getComponentOfType(ProjectService.class);
        this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        this.projectContextAccessor = new ProjectContextAccessorImpl(this);
    }

    @Override
    public void doTag(final XMLOutput output) throws JellyTagException
    {
        validateParams();

        Long defaultAssignee = ProjectAssigneeTypes.PROJECT_LEAD;
        if (ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED))
        {
            defaultAssignee = ProjectAssigneeTypes.UNASSIGNED;
        }
        final Long avatarId = getAvatarId();
        final ProjectService.CreateProjectValidationResult validationResult;
        if (avatarId==null)
        {
             validationResult= projectService.validateCreateProject(getLoggedInUser(), getProjectName(), getProjectKey(), "", getLeadUserName(), "", defaultAssignee);
        }
        else
        {
            validationResult= projectService.validateCreateProject(getLoggedInUser(), getProjectName(), getProjectKey(), "", getLeadUserName(), "", defaultAssignee,avatarId);
        }
        if (validationResult.isValid())
        {
            final Project project = projectService.createProject(validationResult);

            setProject(project.getKey());

            // set response attributes
            final Script body = getBody();
            if (body != null)
            {
                body.run(context, output);
            }
        }
        else
        {
            JellyUtils.processErrorCollection(validationResult.getErrorCollection());
        }
    }

    private void validateParams() throws MissingAttributeException
    {
        if (Strings.isNullOrEmpty(getProjectKey()))
        {
            throw new MissingAttributeException(KEY_PROJECTKEY);
        }

        if (Strings.isNullOrEmpty(getProjectName()))
        {
            throw new MissingAttributeException(KEY_PROJECTNAME);
        }

        if (Strings.isNullOrEmpty(getLeadUserName()))
        {
            throw new MissingAttributeException(KEY_PROJECTLEAD);
        }
    }

    @Override
    public void setProject(final Long projectId)
    {
        projectContextAccessor.setProject(projectId);
    }

    @Override
    public void setProject(final String projectKey)
    {
        projectContextAccessor.setProject(projectKey);
    }

    @Override
    public void setProject(final GenericValue project)
    {
        projectContextAccessor.setProject(project);
    }

    @Override
    public void loadPreviousProject()
    {
        projectContextAccessor.loadPreviousProject();
    }

    private String getProjectKey()
    {
        if (Strings.isNullOrEmpty(projectKey))
        {
            projectKey = (String) getProperties().get(KEY_PROJECTKEY);
        }

        return projectKey;
    }

    private String getProjectName()
    {
        if (Strings.isNullOrEmpty(projectName))
        {
            projectName = (String) getProperties().get(KEY_PROJECTNAME);
        }

        return projectName;
    }

    private Long getAvatarId()
    {
        if (avatarId == null)
        {
            final String strAvatarId = (String) getProperties().get(KEY_AVATAR_ID);
            if (strAvatarId != null)
            {
                try
                {
                    avatarId = Long.valueOf(strAvatarId);
                }
                catch (NumberFormatException e)
                {

                    avatarId = null;
                }

            }
        }
        return avatarId;
    }
    private String getLeadUserName()
    {
        if (Strings.isNullOrEmpty(projectLeadUsername))
        {
            projectLeadUsername = (String) getProperties().get(KEY_PROJECTLEAD);
        }

        return projectLeadUsername;
    }

    public User getLoggedInUser()
    {
        final ApplicationUser user = jiraAuthenticationContext.getUser();
        return user==null?null:user.getDirectoryUser();
    }
}