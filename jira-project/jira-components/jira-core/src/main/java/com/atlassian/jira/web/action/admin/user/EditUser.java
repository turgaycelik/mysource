/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.user.GenericEditProfile;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings ({ "UnusedDeclaration" })
@WebSudoRequired
public class EditUser extends GenericEditProfile
{
    private final UserService userService;
    private final UserManager userManager;
    private final FeatureManager featureManager;

    private UserService.UpdateUserValidationResult updateUserValidationResult;
    private String editName;
    private ApplicationUser oldUser;
    private UserManager.UserState userState;
    private Collection<Project> projects;
    private Collection<ProjectComponent> components;

    public EditUser(UserService userService, UserManager userManager, UserPropertyManager userPropertyManager,
                    FeatureManager featureManager)
    {
        super(userPropertyManager);
        this.userService = userService;
        this.featureManager = featureManager;
        this.userManager = userManager;
    }

    public void doValidation()
    {
        super.doValidation();
        final ApplicationUser newUser = buildNewUser();

        updateUserValidationResult = userService.validateUpdateUser(newUser);
        addErrorCollection(updateUserValidationResult.getErrorCollection());
    }


    public boolean showProjectsUserLeadsError()
    {
        return !isActive() && !getProjectsUserLeads().isEmpty();
    }

    public String projectsUserLeadsErrorMessage()
    {
        String projectList = getDisplayableProjectList(getProjectsUserLeads(), "/roles");
        return getText("admin.errors.users.cannot.deactivate.due.to.project.lead", projectList);
    }

    private Collection<Project> getProjectsUserLeads()
    {
        if (projects == null)
        {
            projects = ComponentAccessor.getProjectManager().getProjectsLeadBy(getEditedUser());
        }
        return projects;
    }

    private Collection<ProjectComponent> getComponentsUserLeads()
    {
        if (components == null)
        {
            components = ComponentAccessor.getProjectComponentManager().findComponentsByLead(getEditName());
        }
        return components;
    }

    public boolean showComponentsUserLeadsError()
    {
        return !isActive() && !getComponentsUserLeads().isEmpty();
    }

    public String componentsUserLeadsErrorMessage()
    {
        String projectList = getDisplayableProjectList(getProjectsFor(getComponentsUserLeads()), "/components");
        return getText("admin.errors.users.cannot.deactivate.due.to.component.lead", projectList);
    }

    private ApplicationUser buildNewUser()
    {
        ImmutableUser.Builder builder = ImmutableUser.newUser(getEditedUser().getDirectoryUser());
        if (showRenameUser())
        {
            builder.name(getUsername());
        }
        builder.displayName(getFullName());
        builder.emailAddress(StringUtils.trim(getEmail()));
        if (showActiveCheckbox())
        {
            builder.active(isActive());
        }
        return new DelegatingApplicationUser(getEditedUser().getKey(), builder.toUser());
    }

    private Collection<Project> getProjectsFor(Collection<ProjectComponent> components)
    {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        HashSet<Project> projects = new HashSet<Project>(components.size());
        for (ProjectComponent component : components)
        {
            projects.add(projectManager.getProjectObj(component.getProjectId()));
        }
        return projects;
    }

    private String getDisplayableProjectList(Collection<Project> projects, String projectConfigSection)
    {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (Project project : projects)
        {
            if (count >= 5)
            {
                sb.append(", ...");
                break;
            }
            if (count > 0)
                sb.append(", ");

            sb.append("<a href=\"");
            sb.append(insertContextPath("/plugins/servlet/project-config/"));
            sb.append(project.getKey());
            sb.append(projectConfigSection);
            sb.append("\">");
            sb.append(project.getKey());
            sb.append("</a>");

            count++;
        }
        return sb.toString();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Now update the user
        userService.updateUser(updateUserValidationResult);
        String result = getResult();

        if (SUCCESS.equals(result))
        {
            // Event is now sent in UserService
            final String returnUrl;
            if (getReturnUrl() == null)
            {
                returnUrl = "ViewUser.jspa?name=" + URLEncoder.encode(updateUserValidationResult.getApplicationUser().getUsername(),"UTF8");
            }
            else
            {
                returnUrl = getReturnUrl();
            }

            // The username might have changed, so we always want to redirect, even if the request is coming from an inline dialog.
            return returnCompleteWithInlineRedirect(returnUrl);
        }

        return result;
    }

    public String getEditName()
    {
        return editName;
    }

    public void setEditName(String editName)
    {
        this.editName = editName;
    }

    public boolean showActiveCheckbox()
    {
        // Hide this for JOD
        if (featureManager.isOnDemand())
        {
            return false;
        }
        else
        {
            // Hide for LDAP until we design JRA-24937
            final Directory directory = userManager.getDirectory(getEditedUser().getDirectoryId());
            return directory.getType() != DirectoryType.CONNECTOR;
        }
    }

    public boolean showRenameUser()
    {
        return userManager.canRenameUser(getEditedUser());
    }

    private UserManager.UserState getUserState()
    {
        if (userState == null)
        {
            userState = userManager.getUserState(getEditedUser());
        }
        return userState;
    }

    public boolean isInMultipleDirectories()
    {
        return getUserState().isInMultipleDirectories();
    }

    @Override
    public ApplicationUser getEditedUser()
    {
        if (oldUser == null)
        {
            oldUser = userManager.getUserByName(editName);
        }
        return oldUser;
    }
}
