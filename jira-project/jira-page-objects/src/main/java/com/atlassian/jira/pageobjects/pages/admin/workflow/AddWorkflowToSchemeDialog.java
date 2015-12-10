package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.dialogs.admin.AbstractAddWorkflowToSchemeDialog;

/**
 * @since v6.0
 */
public class AddWorkflowToSchemeDialog
        extends AbstractAddWorkflowToSchemeDialog<AddWorkflowToSchemeDialog>
{
    private final EditWorkflowScheme parentPage;

    public AddWorkflowToSchemeDialog(EditWorkflowScheme parentPage)
    {
        this.parentPage = parentPage;
    }

    @Override
    protected AddWorkflowToSchemeDialog getThis()
    {
        return this;
    }

    public AssignWorkflowToSchemeDialog next()
    {
        clickNext();
        return binder.bind(AssignWorkflowToSchemeDialog.class, parentPage);
    }
}
