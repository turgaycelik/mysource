package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import net.sourceforge.jwebunit.WebTester;

import java.text.DecimalFormat;

/**
 * @since v4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserSessions extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestUserSessions.xml");
    }

    public void testSessionsAppear()
    {
        startSessionsForUsers(1, 150);

        gotoUserSessions();

        assertLinkPresent("gotoNext", "gotoEnd");
        assertLinkNotPresent("gotoStart", "gotoPrev");

        tester.clickLink("gotoNext");
        assertLinkPresent("gotoStart", "gotoPrev", "gotoNext", "gotoEnd");
        
        tester.clickLink("gotoPrev");
        assertLinkPresent("gotoNext", "gotoEnd");
        assertLinkNotPresent("gotoStart", "gotoPrev");

        tester.clickLink("gotoEnd");
        assertLinkPresent("gotoStart", "gotoPrev");
        assertLinkNotPresent("gotoEnd", "gotoNext");

    }

    public void testNotAccessibleForAdmin()
    {
        navigation.login("justadmin");
        tester.gotoPage("/secure/admin/CurrentUsersList.jspa");
        assertions.getJiraFormAssertions().assertFormWarningMessage("'Just Admin' does not have permission to access this page.");
    }

    private void assertLinkPresent(final String... args)
    {
        for (final String arg : args)
        {
            tester.assertLinkPresent(arg);
        }
    }

    private void assertLinkNotPresent(final String... args) {
        for (final String arg : args)
        {
            tester.assertLinkNotPresent(arg);
        }
    }

    private void startSessionsForUsers(final int start, final int end)
    {
        for (int i = start; i <= end; i++)
        {
            loginAs(makeUserName(i));
        }
    }

    private void loginAs(final String userName)
    {
        final WebTester tester = WebTesterFactory.createNewWebTester(environmentData);
        tester.beginAt("/login.jsp");
        tester.setFormElement("os_username", userName);
        tester.setFormElement("os_password", userName);
        tester.setWorkingForm("login-form");
        tester.submit();
        log.log("Started session for " + userName);
    }

    private void gotoUserSessions()
    {
        navigation.gotoAdmin();
        tester.clickLink("usersessions");
    }

    private String makeUserName(final int j)
    {
        return "user-" + new DecimalFormat("000").format(j);
    }
}
