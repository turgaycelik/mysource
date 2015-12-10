package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

@WebSudoRequired
public class ManageSubTasks extends JiraWebActionSupport
{
    private final SubTaskManager subTaskManager;
    private final TranslationManager translationManager;

    private String id;
    private String name;
    private String description;
    private String iconurl = "/images/icons/issuetypes/subtask_alternate.png";

    public ManageSubTasks(SubTaskManager subTaskManager, TranslationManager translationManager)
    {
        this.subTaskManager = subTaskManager;
        this.translationManager = translationManager;
    }

    @RequiresXsrfCheck
    public String doEnableSubTasks() throws Exception
    {
        // Ensure sub tasks are turned on
        if (isSubTasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.already.enabled"));
            return getResult();
        }

        // Enable sub-tasks
        subTaskManager.enableSubTasks();

        return getRedirect(getActionName() + ".jspa");
    }

    public boolean isSubTasksEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public Collection getSubTasksIssueTypes()
    {
        return subTaskManager.getSubTaskIssueTypeObjects();
    }

    public String doAddNewSubTaskIssueType()
    {
        if (!isSubTasksEnabled())
        {
            return returnCompleteWithInlineRedirect(getActionName() + ".jspa");
        }
        else
        {
            return INPUT;
        }
    }
    
    @RequiresXsrfCheck
    public String doAddSubTaskIssueType() throws Exception
    {
        // Ensure that the passed in parameters are OK
        validateAddInput();

        if (invalidInput())
        {
            return ERROR;
        }

        try
        {
            final String avatarId = getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID);

            // Add the sub-task issue type
            subTaskManager.insertSubTaskIssueType(getName(), (long) getSubTasksIssueTypes().size(), getDescription(), Long.valueOf(avatarId));
            return returnCompleteWithInlineRedirect(getActionName() + ".jspa");
        }
        catch (CreateException e)
        {
            log.error("Error occurred while adding sub-task issue type.", e);
            addErrorMessage("Error occurred while adding sub-task issue type. Please see log for more details.");
            return ERROR;
        }
    }

    private void validateAddInput()
    {
        if (!isSubTasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.disabled"));
            return;
        }

        // Ensure that the name is set
        if (!TextUtils.stringSet(getName()))
        {
            addError("name", getText("admin.errors.specify.a.name.for.this.new.sub.task.issue.type"));
        }
        else
        {
            // Ensure that an issue type with that name does not already exist
            if (subTaskManager.issueTypeExistsByName(getName()))
            {
                addError("name", getText("admin.errors.issue.type.with.this.name.already.exists"));
            }
        }

        // Check that icon URL is set
        if (!TextUtils.stringSet(getIconurl()))
        {
            addError("iconurl", getText("admin.errors.must.specify.a.url.for.the.icon"));
        }
    }

    @RequiresXsrfCheck
    public String doMoveSubTaskIssueTypeUp() throws Exception
    {
        validateMoveInput();

        if (invalidInput())
        {
            return ERROR;
        }

        try
        {
            // Move the sub-task issue type up
            subTaskManager.moveSubTaskIssueTypeUp(getId());
            return getRedirect(getActionName() + ".jspa");
        }
        catch(DataAccessException e)
        {
            log.error("Error occurred while storing sub-task issue types.", e);
            addErrorMessage(getText("admin.errors.error.occured.while.storing.sub.task.issue"));
            return ERROR;
        }
    }

    @RequiresXsrfCheck
    public String doMoveSubTaskIssueTypeDown() throws Exception
    {
        validateMoveInput();

        if (invalidInput())
        {
            return ERROR;
        }

        try
        {
            // Move the sub-task issue type up
            subTaskManager.moveSubTaskIssueTypeDown(getId());
            return getRedirect(getActionName() + ".jspa");
        }
        catch(DataAccessException e)
        {
            log.error("Error occurred while storing sub-task issue types.", e);
            addErrorMessage(getText("admin.errors.error.occured.while.storing.sub.task.issue"));
            return ERROR;
        }
    }

    private void validateMoveInput()
    {
        if (!isSubTasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.are.disabled"));
            return;
        }

        // Esure that the id is set
        if (!TextUtils.stringSet(getId()))
        {
            addErrorMessage(getText("admin.errors.no.subtask.issue.type.id.specified"));
        }
        // Ensure that the subtask with that id exists
        else if (!subTaskManager.issueTypeExistsById(getId()))
        {
            addErrorMessage(getText("admin.errors.no.subtask.issue.type.with.id.exists", "'" + getId() + "'"));
        }
    }


    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(String iconurl)
    {
        this.iconurl = iconurl;
    }

    public boolean isTranslatable()
    {
        //JRA-16912: If there's no installed languages don't show the translate link!
        return !translationManager.getInstalledLocales().isEmpty();
    }
}
