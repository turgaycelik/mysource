package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;

import org.openqa.selenium.By;

/**
 * @since v5.1
 */
public class DiscardDraftDialog extends FormDialog
{
    private PageElement submit;
    private final String workflowName;

    public DiscardDraftDialog(String workflowName)
    {
        super("discard_draft_workflow-dialog");
        this.workflowName = workflowName;
    }

    @Init
    public void init()
    {
        submit = find(By.id("delete-workflow-submit"));
    }

    public ViewWorkflowSteps submitAndGotoViewWorkflow()
    {
        submit(submit);
        return binder.bind(ViewWorkflowSteps.class, workflowName, false);
    }

    public <T> T submitAndBind(Class<T> page, Object... args)
    {
        submit(submit);
        return binder.bind(page, args);
    }
}
