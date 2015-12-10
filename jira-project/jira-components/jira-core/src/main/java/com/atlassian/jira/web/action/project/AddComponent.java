package com.atlassian.jira.web.action.project;

import com.atlassian.jira.bc.project.component.DefaultProjectComponentService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.util.TextUtils;

/**
 * Only used by Jelly
 */
public class AddComponent extends ViewProject
{
    String name;
    private String componentLead;
    private String description;
    private final ProjectComponentService projectComponentService;

    private static final String SECURITY_BREACH = "securitybreach";

    public AddComponent(ProjectComponentService projectComponentService)
    {
        this.projectComponentService = projectComponentService;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String doDefault() throws Exception
    {
        if (hasProjectAdminPermission() || hasAdminPermission())
        {
            return INPUT;
        }
        else
        {
            return "securitybreach";
        }
    }

    protected void doValidation()
    {
        try
        {
            getProject();
            if (project == null)
            {
                addErrorMessage(getText("admin.errors.project.no.project.with.id"));
            }
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));
        }
    }


    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // getProject has been validated by the super class doValidate() method
        Long projectId = getProject().getLong("id");
        final ProjectComponent projectComponent = projectComponentService.create(getLoggedInUser(), this, getName(), getDescription(), getComponentLeadUserKey(), projectId);

        if (hasAnyErrors())
        {
            if (getErrorMessages() != null && !getErrorMessages().isEmpty())
            {
                if (getErrorMessages().contains(DefaultProjectComponentService.KEY_USER_NO_PERMISSION))
                {
                    return SECURITY_BREACH;
                }
            }
            return ERROR;
        }
        else
        {
            // No errors - return to viewing the project in which component has been created
            return getRedirect("/plugins/servlet/project-config/" + getProjectObject().getKey() + "/summary");
        }

    }

    private String getComponentLeadUserKey()
    {
        ApplicationUser leadUserObj = getLeadUserObj();
        if (leadUserObj == null)
        {
            return null;
        }
        return leadUserObj.getKey();
    }

    private ApplicationUser getLeadUserObj()
    {
        return getUserManager().getUserByName(getComponentLead());
    }

    public String getComponentLead()
    {
        return componentLead;
    }

    public void setComponentLead(String componentLead)
    {
        if (TextUtils.stringSet(componentLead))
        {
            this.componentLead = componentLead;
        }
        else
        {
            this.componentLead = null;
        }
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * Set description to give value. If the value is an empty string, it will be set to null
     * @param description description to set it to
     */
    public void setDescription(String description)
    {
        // dont store empty strings as some databases treat them differently - JRA-12196
        this.description = TextUtils.stringSet(description) ? description : null;
    }
}
