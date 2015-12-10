package com.atlassian.jira.pageobjects.pages.admin.workflow;

/**
 * @since v5.1
 */
public class AddWorkflowDialog extends WorkflowNameDescriptionDialog<AddWorkflowDialog>
{
    public AddWorkflowDialog()
    {
        super("add-workflow-dialog", "add-workflow-submit");
    }

    public <T extends WorkflowHeader> T submit(WorkflowHeader.WorkflowMode<T> mode)
    {
        final String name = getName();
        submit();
        return mode.bind(binder, name, false);
    }

    @Override
    AddWorkflowDialog getThis()
    {
        return this;
    }
}
