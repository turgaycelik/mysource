package com.atlassian.jira.functest.framework.admin.workflows;

import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import net.sourceforge.jwebunit.WebTester;

public class PublishDraftPage
{
    private final ViewWorkflows viewWorkflowsPage;
    private final WebTester tester;

    public PublishDraftPage(final WebTester tester, final ViewWorkflows viewWorkflowsPage)
    {
        this.viewWorkflowsPage = viewWorkflowsPage;
        this.tester = tester;
    }

    public PublishDraftPage backupOriginalWorkflowAs(final String name)
    {
        tester.checkCheckbox("enableBackup", "true");
        tester.setFormElement("newWorkflowName", name);
        return this;
    }

    public ViewWorkflows publish()
    {
        tester.submit("Publish");
        return viewWorkflowsPage;
    }

    public ViewWorkflows cancel()
    {
        tester.clickLink("publish-workflow-cancel");
        return viewWorkflowsPage;
    }
}
