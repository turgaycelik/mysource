package com.atlassian.jira.functest.framework.admin.workflows;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import com.atlassian.jira.functest.framework.admin.WorkflowSteps;
import com.atlassian.jira.functest.framework.admin.WorkflowStepsImpl;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents the workflow designer page shown for a given workflow.
 *
 * @since v5.1
 */
public class WorkflowDesignerPage extends AbstractFuncTestUtil
{
    private final ViewWorkflows viewWorkflows;
    private TextView textView;

    public WorkflowDesignerPage(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel, ViewWorkflows viewWorkflows)
    {
        super(tester,environmentData,logIndentLevel);
        this.viewWorkflows = viewWorkflows;
        this.textView = new TextView(this, tester, environmentData, logIndentLevel);
    }

    public RenameWorkflowPage rename()
    {
        tester.clickLink("edit-workflow-trigger");
        return new RenameWorkflowPage(tester, viewWorkflows);
    }

    public TextView textView()
    {
        return textView;
    }

    public static class TextView
    {
        private final WorkflowDesignerPage workflowDesignerPage;
        private WebTester tester;
        private WorkflowSteps steps;

        public TextView(final WorkflowDesignerPage workflowDesignerPage, final WebTester tester, final JIRAEnvironmentData jiraEnvironmentData, int logIndentLevel)
        {
            this.workflowDesignerPage = workflowDesignerPage;
            this.tester = tester;
            this.steps = new WorkflowStepsImpl(tester, jiraEnvironmentData, logIndentLevel);
        }

        public TextView goTo()
        {
            tester.clickLink("workflow-text");
            return this;
        }

        public WorkflowSteps steps()
        {
            return steps;
        }
    }
}
