package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestMultiUserPicker extends FuncTestCase
{

    /** This is a func test for JRA-14215. It make sure that a username like ${uname} doesn't cause any issues */
    public void testMultiUserPickerWithStrangeUsername()
    {
        administration.restoreBlankInstance();
        administration.usersAndGroups().addUser("${uname}", "password", "${fullname}", "user@example.com");
        //now lets got to the Multi user picker and make sure there's no error!
        tester.gotoPage("secure/popups/UserPickerBrowser.jspa?formName=jiraform&multiSelect=true");
        final WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextNotPresent(locator, "System Error");
        text.assertTextPresent(locator, "${uname}");
        text.assertTextPresent(locator, ADMIN_USERNAME);
    }
}
