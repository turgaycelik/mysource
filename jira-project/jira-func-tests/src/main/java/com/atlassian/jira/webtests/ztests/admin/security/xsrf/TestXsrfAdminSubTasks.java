package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfAdminSubTasks extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testSubTasks() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("SubTask Enablement", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoSubTasks();
                }
            }, new XsrfCheck.LinkWithTextSubmission("Enable"))
            ,
            new XsrfCheck("Edit Sub Tasks", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoSubTasks();
                    tester.clickLink("edit_Sub-task");
                }
            }, new XsrfCheck.FormSubmission("Update"))
            ,
            new XsrfCheck("Add Sub Task Type", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoSubTasks();
                    tester.clickLink("add-subtask-type");
                    tester.setFormElement("name", "name");
                    tester.setFormElement("description", "desc");
                }
            },
            new XsrfCheck.FormSubmission("Add")),
                
            new XsrfCheck("Delete Sub Task Type", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoSubTasks();
                    tester.clickLinkWithText("Delete");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
            ,
                
            new XsrfCheck("Disable Sub Tasks", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoSubTasks();
                }
            }, new XsrfCheck.LinkWithTextSubmission("Disable"))

        ).run(funcTestHelperFactory);
    }

    private void gotoSubTasks()
    {
        navigation.gotoAdmin();
        tester.clickLink("subtasks");
    }

}