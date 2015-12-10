package com.atlassian.jira.webtest.webdriver.util.admin;

import com.atlassian.jira.pageobjects.dialogs.admin.AbstractAddWorkflowToSchemeDialog;
import com.atlassian.jira.pageobjects.dialogs.admin.AbstractAddWorkflowToSchemeDialog.PreviewPanel;
import com.atlassian.jira.pageobjects.dialogs.admin.AbstractAddWorkflowToSchemeDialog.Workflow;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;

/**
 * @since v6.0
 */
public abstract class AbstractAddWorkflowDialogHelper<D extends AbstractAddWorkflowToSchemeDialog<D>, W>
{
    private D dialog;
    private Iterable<W> workflows;

    public AbstractAddWorkflowDialogHelper<D, W> dialog(D dialog)
    {
        this.dialog = dialog;
        return this;
    }

    public AbstractAddWorkflowDialogHelper<D, W> workflows(Iterable<W> workflows)
    {
        this.workflows = workflows;
        return this;
    }

    public void assertDialog()
    {
        int activePos = assertAddWorkflowDialog(0);
        dialog = nextAndBack(dialog);
        assertAddWorkflowDialog(activePos);
    }

    private int assertAddWorkflowDialog(int activePos)
    {
        final List<Workflow> actualWorkflows = dialog.getWorkflows();

        assertEquals(size(workflows), actualWorkflows.size());
        assertDialogPanel(get(workflows, activePos));

        final Iterator<Workflow> iterator = actualWorkflows.iterator();
        int pos = 0;
        for (W expectedWorkflow : workflows)
        {
            final Workflow actualWorkflow = iterator.next();
            assertEquals(displayName(expectedWorkflow), actualWorkflow.getDisplayName());

            if (pos != activePos)
            {
                actualWorkflow.click();
                activePos = pos;
            }

            assertDialogPanel(expectedWorkflow);
            pos++;
        }
        return activePos;
    }

    private void assertDialogPanel(W expectedWorkflow)
    {
        final PreviewPanel previewPanel = dialog.getPreviewPanel();
        assertEquals(lastModifiedUser(expectedWorkflow), previewPanel.getLastModifiedUser());
        assertEquals(description(expectedWorkflow), previewPanel.getDescription());
        assertEquals(displayName(expectedWorkflow), previewPanel.getWorkflowName());
    }


    protected abstract String lastModifiedUser(W workflow);

    protected abstract String description(W workflow);

    protected abstract String displayName(W workflow);

    protected abstract D nextAndBack(D dialog);
}
