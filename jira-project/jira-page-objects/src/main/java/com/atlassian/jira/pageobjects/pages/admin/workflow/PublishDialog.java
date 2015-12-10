package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;

import org.openqa.selenium.By;

/**
* @since v5.1
*/
public class PublishDialog extends FormDialog
{
    public static final String FIELD_BACKUP = "enableBackup";
    public static final String FIELD_NEW_WORKFLOW_NAME = "newWorkflowName";

    private PageElement backupYes;
    private PageElement backupNo;
    private PageElement backupName;
    private PageElement submit;

    private final String workflowName;

    public PublishDialog(String workflowName)
    {
        super("publish_draft_workflow-dialog");
        this.workflowName = workflowName;
    }

    @Init
    public void init()
    {
        backupYes = find(By.id("publish-workflow-true"));
        backupNo = find(By.id("publish-workflow-false"));
        backupName = find(By.id("publish-workflow-newWorkflowName"));
        submit = find(By.id("publish-workflow-submit"));
    }

    public PublishDialog setName(String name)
    {
        assertDialogOpen();
        setElement(backupName, name);
        return this;
    }

    public PublishDialog disableBackup()
    {
        backupNo.click();
        return this;
    }

    public PublishDialog enableBackup(String name)
    {
        backupYes.click();
        setElement(backupName, name);
        return this;
    }

    public PublishDialog submitFail()
    {
        submit.click();
        waitWhileSubmitting();
        assertDialogOpen();
        return this;
    }

    public <P> P cancel(Class<P> page)
    {
        close();
        return binder.bind(page);
    }

    public ViewWorkflowSteps submitAndGotoViewWorkflow()
    {
        return submitAndBind(ViewWorkflowSteps.class, workflowName, false);
    }

    public <T> T submitAndBind(Class<T> page, Object... args)
    {
        submit(submit);
        return binder.bind(page, args);
    }
}
