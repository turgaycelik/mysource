package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.WorkflowManager;

/**
 * This upgrade task will create a backup copy of any inactive workflow drafts stored in JIRA.
 *
 * Moved from UT761 -> UT813. Need to run it again but no point in running it twice.
 *
 * @since v5.1
 */
public class UpgradeTask_Build813 extends AbstractUpgradeTask
{
    private final WorkflowManager workflowManager;

    public UpgradeTask_Build813(final WorkflowManager workflowManager)
    {
        super(false);
        this.workflowManager = workflowManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "813";
    }

    @Override
    public String getShortDescription()
    {
        return "Backing up all inactive workflow drafts";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        workflowManager.copyAndDeleteDraftsForInactiveWorkflowsIn(null, workflowManager.getWorkflows());
    }
}
