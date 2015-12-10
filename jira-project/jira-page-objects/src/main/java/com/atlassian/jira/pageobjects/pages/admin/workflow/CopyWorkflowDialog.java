package com.atlassian.jira.pageobjects.pages.admin.workflow;

import static java.lang.String.format;

/**
 * @since v5.1
 */
public class CopyWorkflowDialog extends WorkflowNameDescriptionDialog<CopyWorkflowDialog>
{
    public CopyWorkflowDialog(String workflowName)
    {
        super(format("copy_%s-dialog", workflowName), "copy-workflow-submit");
    }

    @Override
    CopyWorkflowDialog getThis()
    {
        return this;
    }

    public <T extends WorkflowHeader> T submit(WorkflowHeader.WorkflowMode<T> mode)
    {
        final String name = getName();
        submit();
        return mode.bind(binder, name, false);
    }
}
