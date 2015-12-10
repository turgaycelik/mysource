package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.admin.workflows.WorkflowDesignerPage;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.DEFAULT_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_DESC;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_NAME;

/**
 * Abstract base class for workflow component (condition, function, validator) reloadability tests.
 *
 * @since v4.4
 */
public abstract class AbstractReloadableWorkflowComponentTest extends AbstractReloadablePluginsTest
{

    protected final void setUpTestScheme()
    {
        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName(TEST_WORKFLOW_SCHEME_NAME).setDescription(TEST_WORKFLOW_SCHEME_DESC).setDefaultWorkflow(TEST_WORKFLOW_NAME));
    }

    protected final WorkflowDesignerPage setUpTestWorkflow()
    {
        return administration.workflows().goTo().copyWorkflow(DEFAULT_WORKFLOW_NAME, TEST_WORKFLOW_NAME);
    }
}
