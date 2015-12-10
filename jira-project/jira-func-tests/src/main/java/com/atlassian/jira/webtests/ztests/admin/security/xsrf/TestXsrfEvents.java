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
public class TestXsrfEvents extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testEventAdministration() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("AddEvent", new EventSetup()
            {
                public void setup()
                {
                    super.setup();
                    tester.setWorkingForm("jiraform");
                    tester.setFormElement("name", "New Event");
                    tester.selectOption("templateId", "Issue Created");
                }
            }, new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck("EditEvent", new EventSetup()
            {
                public void setup()
                {
                    super.setup();
                    tester.clickLink("edit_New Event");
                    tester.setFormElement("description", "This is a New Event");
                }
            }, new XsrfCheck.FormSubmission("Update")),
            new XsrfCheck("DeleteEvent", new EventSetup()
            {
                public void setup()
                {
                    super.setup();
                    tester.clickLink("del_New Event");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
        ).run(funcTestHelperFactory);
    }

    private class EventSetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            navigation.gotoAdminSection("eventtypes");
        }
    }
}


