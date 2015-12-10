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
public class TestXsrfAdminGroups extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAdminGroups() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("Delete Group", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoGroupBrowser();
                    tester.clickLink("del_jira-users");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
            ,
            new XsrfCheck("Add Group", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoGroupBrowser();
                    tester.setFormElement("addName", "newgroup");
                }
            }, new XsrfCheck.FormSubmission("add_group"))
            ,
            new XsrfCheck("Bulk Edit User Groups (Leave)", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoGroupBrowser();
                    tester.clickLink("edit_members_of_jira-developers");
                    tester.selectOption("usersToUnassign", ADMIN_USERNAME);
                }
            }, new XsrfCheck.FormSubmission("unassign"))
            ,
            new XsrfCheck("Bulk Edit User Groups (Join)", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    gotoGroupBrowser();
                    tester.clickLink("edit_members_of_jira-developers");
//                    JRADEV-18305 This won't work because the usersToAssignStr is now a MultiSelect field.
//                    The XSRF token is still checked however
//                    tester.setFormElement("usersToAssignStr", ADMIN_USERNAME);
                }
            }, new XsrfCheck.FormSubmission("assign"))

        ).run(funcTestHelperFactory);
    }

    private void gotoGroupBrowser()
    {
        navigation.gotoAdmin();
        tester.clickLink("group_browser");
    }
}