package com.atlassian.jira.functest.framework.admin.workflows;

import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents the page that
 *
 * @since v5.1
 */
public class RenameWorkflowPage
{
    private final WebTester tester;

    private final ViewWorkflows viewWorkflows;

    public RenameWorkflowPage(final WebTester tester, ViewWorkflows viewWorkflows)
    {
        this.tester = tester;
        this.viewWorkflows = viewWorkflows;
    }

    public RenameWorkflowPage setNameTo(final String name)
    {
        tester.setFormElement("newWorkflowName", name);
        return this;
    }

    public RenameWorkflowPage setDescriptionTo(final String description)
    {
        tester.setFormElement("description", description);
        return this;
    }

    public ViewWorkflows submit()
    {
        tester.submit();
        return viewWorkflows;
    }

    public boolean isNameEditable()
    {
        return tester.getDialog().hasFormParameterNamed("newWorkflowName");
    }
}
