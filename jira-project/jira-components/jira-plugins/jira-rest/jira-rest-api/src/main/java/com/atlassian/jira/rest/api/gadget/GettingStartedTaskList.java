package com.atlassian.jira.rest.api.gadget;

import com.atlassian.jira.rest.api.issue.FieldsSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Admin Getting Started Task List bean.
 */
@JsonSerialize (using = FieldsSerializer.class)
public class GettingStartedTaskList
{
    public AdminTask createProject;
    public AdminTask createIssue;
    public AdminTask createUser;
    public AdminTask lookAndFeel;
    public boolean isCompleted;
    public boolean isDismissed;

    public AdminTask getCreateProject()
    {
        return createProject;
    }

    public GettingStartedTaskList setCreateProject(AdminTask createProject)
    {
        this.createProject = createProject;
        return this;
    }

    public AdminTask getCreateUser()
    {
        return createUser;
    }

    public GettingStartedTaskList setCreateUser(AdminTask createUser)
    {
        this.createUser = createUser;
        return this;
    }

    public boolean isCompleted()
    {
        return isCompleted;
    }

    public GettingStartedTaskList setCompleted(boolean completed)
    {
        isCompleted = completed;
        return this;
    }

    public boolean isDismissed()
    {
        return isDismissed;
    }

    public GettingStartedTaskList setDismissed(boolean dismissed)
    {
        isDismissed = dismissed;
        return this;
    }

    public AdminTask getLookAndFeel()
    {
        return lookAndFeel;
    }

    public GettingStartedTaskList setLookAndFeel(AdminTask lookAndFeel)
    {
        this.lookAndFeel = lookAndFeel;
        return this;
    }

    public AdminTask getCreateIssue()
    {
        return createIssue;
    }

    public GettingStartedTaskList setCreateIssue(AdminTask createIssue)
    {
        this.createIssue = createIssue;
        return this;
    }
}
