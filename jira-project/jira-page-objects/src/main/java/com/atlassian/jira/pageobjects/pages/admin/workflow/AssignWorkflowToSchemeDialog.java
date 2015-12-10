package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.dialogs.admin.AbstractAssignIssueTypesDialog;

/**
 * @since v5.2
 */
public class AssignWorkflowToSchemeDialog
        extends AbstractAssignIssueTypesDialog<AssignWorkflowToSchemeDialog>
{
    private final EditWorkflowScheme parentPage;

    public AssignWorkflowToSchemeDialog(EditWorkflowScheme parentPage)
    {
        this.parentPage = parentPage;
    }

    @Override
    protected AssignWorkflowToSchemeDialog getThis()
    {
        return this;
    }

    void submitNotWait()
    {
        super.submit();
    }

    @Override
    public void submit()
    {
        parentPage.waitForAction(new Runnable()
        {
            @Override
            public void run()
            {
                AssignWorkflowToSchemeDialog.super.submit();
            }
        });
    }

    public AddWorkflowToSchemeDialog back()
    {
        clickBack();
        return binder.bind(AddWorkflowToSchemeDialog.class, parentPage);
    }
}
