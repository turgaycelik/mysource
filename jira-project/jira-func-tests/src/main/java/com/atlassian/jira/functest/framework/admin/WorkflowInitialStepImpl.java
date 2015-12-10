package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import net.sourceforge.jwebunit.WebTester;

/**
 * Concrete implementation of {@link WorkflowInitialStep}
 */
public class WorkflowInitialStepImpl extends AbstractFuncTestUtil implements WorkflowInitialStep
{
    private final WorkflowTransition transition;
    private String workflowName;

    public WorkflowInitialStepImpl(final WebTester tester, final JIRAEnvironmentData environmentData, final int logIndentLevel, String workflowName)
    {
        super(tester, environmentData, logIndentLevel);
        this.transition = new WorkflowTransitionImpl(tester, environmentData, childLogIndentLevel());
        this.workflowName = workflowName;

        initEditWorkflowDesignerToAccessInitialStep();
    }

    private void initEditWorkflowDesignerToAccessInitialStep()
    {
        String xsrfToken = new HtmlPage(this.tester).getXsrfToken();
        this.tester.gotoPage("/secure/admin/workflows/EditWorkflowDispatcher.jspa?atl_token=" + xsrfToken + "&wfName="+workflowName);
    }

    @Override
    public WorkflowTransition createTransition()
    {
        tester.gotoPage("/secure/admin/workflows/ViewWorkflowTransition.jspa?workflowMode=draft&workflowName=" + workflowName + "&workflowTransition=1&descriptorTab=validators&");
        return transition;
    }

}
