package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.0
 */
public class SubtasksImpl extends AbstractFuncTestUtil implements Subtasks
{
    public SubtasksImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    @Override
    public void enable()
    {
        final Navigation navigation = getFuncTestHelperFactory().getNavigation();
        navigation.gotoAdminSection("subtasks");
        final HtmlPage page = new HtmlPage(tester);

        // if the link doesn't exist, we're already ON
        if (page.isLinkPresentWithExactText("Enable"))
        {
            log("Enabling sub-tasks");
            tester.clickLinkWithText("Enable");
        }
    }

    @Override
    public void disable()
    {
        final Navigation navigation = getFuncTestHelperFactory().getNavigation();
        navigation.gotoAdminSection("subtasks");
        final HtmlPage page = new HtmlPage(tester);

        // if the link doesn't exist, we're already OFF
        if (page.isLinkPresentWithExactText("Disable"))
        {
            log("Disabling sub-tasks");
            tester.clickLinkWithText("Disable");
        }
    }

    @Override
    public boolean isEnabled()
    {
        final Navigation navigation = getFuncTestHelperFactory().getNavigation();
        navigation.gotoAdminSection("subtasks");
        final HtmlPage page = new HtmlPage(tester);
        return page.isLinkPresentWithExactText("Disable");
    }

    @Override
    public void addSubTaskType(final String subTaskName, final String subTaskDescription)
    {
        enable();
        tester.clickLink("add-subtask-type");
        tester.setFormElement("name", subTaskName);
        tester.setFormElement("description", subTaskDescription);
        tester.submit("Add");
    }

    @Override
    public void deleteSubTaskType(final String subTaskName)
    {
        enable();
        tester.clickLink("del_" + subTaskName);
        tester.submit("Delete");
    }
}
