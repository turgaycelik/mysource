package com.atlassian.jira.pageobjects.pages.admin.workflow;

/**
* @since v5.1
*/
public class EditWorkflowNameAndDescriptionDialog extends WorkflowNameDescriptionDialog<EditWorkflowNameAndDescriptionDialog>
{
    public EditWorkflowNameAndDescriptionDialog()
    {
        super("edit-workflow-dialog", "edit-workflow-submit");
    }

    public <P> P cancel(Class<P> page)
    {
        close();
        return binder.bind(page);
    }

    public <P> P submit(Class<P> page, Object...args)
    {
        submit();
        return binder.bind(page, args);
    }

    @Override
    EditWorkflowNameAndDescriptionDialog getThis()
    {
        return this;
    }
}
