package com.atlassian.jira.functest.framework.admin.workflows;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.admin.WorkflowSteps;
import com.atlassian.jira.functest.framework.admin.WorkflowStepsImpl;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v5.1
 */
public class ViewWorkflowPage extends AbstractFuncTestUtil
{
    private DiagramView diagramView;

    private final TextView textView;

    public ViewWorkflowPage(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
        textView = new TextView(tester,environmentData,logIndentLevel);
    }

    public TextView textView()
    {
        return textView;
    }

    public String downloadAsXml()
    {
        tester.clickLinkWithText("XML");
        return tester.getDialog().getResponseText();
    }

    public static class DiagramView
    {
        public DiagramView goTo()
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    public static class TextView
    {
        private final WebTester tester;
        private final WorkflowStepsImpl workflowSteps;

        public TextView(WebTester tester, JIRAEnvironmentData jiraEnvironmentData, int logIndentLevel)
        {
            this.tester = tester;
            workflowSteps = new WorkflowStepsImpl(tester, jiraEnvironmentData, logIndentLevel);
        }

        public TextView goTo()
        {
            tester.clickLink("workflow-text");
            return this;
        }

        public WorkflowSteps steps()
        {
            return workflowSteps;
        }
    }
}
