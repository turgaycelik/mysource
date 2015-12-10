package com.atlassian.jira.webtests.ztests.user.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that the User Profile preferences are not susceptible to XSRF attacks.
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.USERS_AND_GROUPS })
public class TestXsrfUserProfile extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testUpdateProfile() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("Update Profile", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        gotoUserProfile();
                        tester.clickLink("edit_profile_lnk");

                    }
                }, new XsrfCheck.FormSubmission("Edit"))
                ,
                new XsrfCheck("Update Profile Preferences", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        gotoUserProfile();
                        tester.clickLink("edit_prefs_lnk");

                    }
                }, new XsrfCheck.FormSubmission("Update"))

        ).run(funcTestHelperFactory);
    }

    private void gotoUserProfile()
    {
        tester.gotoPage("secure/ViewProfile.jspa");
    }
}