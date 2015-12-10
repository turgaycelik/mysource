/*
 * Editing of project lead and deafult assignee
 *
 * @since v4.4
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import java.net.URI;

public class EditProjectLeadAndDefaultAssignee extends ViewProject
{
    private final ProjectService projectService;
    private final AvatarService avatarService;
    private final UserManager userManager;
    private final UserPickerSearchService userPickerSearchService;
    private String leadError;
    private ApplicationUser leadUserObj;

    public EditProjectLeadAndDefaultAssignee(ProjectService projectService, AvatarService avatarService, UserManager userManager, UserPickerSearchService userPickerSearchService)
    {
        this.projectService = projectService;
        this.avatarService = avatarService;
        this.userManager = userManager;
        this.userPickerSearchService = userPickerSearchService;
    }

    public String doDefault() throws Exception
    {
        // check if the project exists:
        if (getProject() == null)
        {
            return handleProjectDoesNotExist();
        }
        if (!(hasProjectAdminPermission() || hasAdminPermission()))
        {
            return "securitybreach";
        }
        setName(getProject().getString("name"));
        setAvatarId(getProject().getLong("avatar"));
        setLead(getProjectObject().getLeadUserName());
        setUrl(getProject().getString("url"));
        setDescription(getProject().getString("description"));
        setAssigneeType(getProject().getLong("assigneetype"));

        return INPUT;
    }

    private String handleProjectDoesNotExist() throws Exception
    {
        if (hasAdminPermission())
        {
            // User is admin - admit that the Project Doesn't exist because they have permission to see any project.
            // We will show the Edit Project Page, but without any values in the fields (and with an error message).
            // This is consistent with what happens if we start to edit a project, but it gets deleted before we save it.
            setName("???");
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));

            return super.doDefault();
        }
        else
        {
            // User is not admin - show security breach because this isn't a Project they have permission to edit.
            return "securitybreach";
        }
    }

    protected void doValidation()
    {
        // First check that the Project still exists
        if (getProject() == null)
        {
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));
            // Don't try to do any more validation.
            return;
        }
        final Project projectObject = getProjectObject();
        final ProjectService.UpdateProjectValidationResult result =
                projectService.validateUpdateProject(getLoggedInUser(), projectObject.getName(), projectObject.getKey(),
                        projectObject.getDescription(), getLead(), projectObject.getUrl(), getAssigneeType(), getAvatarId());
        if (!result.isValid())
        {
            //map keyed errors to JSP field names
            mapErrorCollection(result.getErrorCollection());
        }

        if (getLeadUserObj() == null)
        {
            setLeadError(getLead());
        }

    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!(hasProjectAdminPermission() || hasAdminPermission()))
        {
            return "securitybreach";
        }


        final Project projectObject = getProjectObject();
        final ProjectService.UpdateProjectValidationResult result =
                projectService.validateUpdateProject(getLoggedInUser(), projectObject.getName(), projectObject.getKey(),
                        projectObject.getDescription(), getLead(), projectObject.getUrl(), getAssigneeType(), getAvatarId());
        projectService.updateProject(result);

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/roles");
    }

    public boolean userPickerDisabled()
    {
        return !userPickerSearchService.canPerformAjaxSearch(this.getJiraServiceContext());
    }

    public ApplicationUser getLeadUserObj()
    {
        if (getLead() != null && leadUserObj == null)
        {
            leadUserObj = userManager.getUserByName(getLead());
        }
        return leadUserObj;
    }

    public URI getLeadUserAvatarUrl()
    {
        return avatarService.getAvatarURL(getLoggedInUser(), getLead(), Avatar.Size.SMALL);
    }

    public String getLeadError()
    {
        return leadError;
    }

    public void setLeadError(String leadError)
    {
        this.leadError = leadError;
    }
}
