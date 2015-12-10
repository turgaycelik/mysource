package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueActionErrors extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestIssueActionErrors.xml");
    }

    public void testOperations()
    {
        assertOperationErrors("Attach Files",
                "secure/AttachFile!default.jspa?id=10000", "secure/AttachFile!default.jspa?id=999",
                "You do not have permission to create attachments for this issue.", true);

        assertOperationErrors("Assign",
                "secure/AssignIssue!default.jspa?id=10000", "secure/AssignIssue!default.jspa?id=999",
                "It seems that you have tried to perform an operation which you are not permitted to perform.", true);

        assertOperationErrors("Log Work",
                "secure/CreateWorklog!default.jspa?id=10000", "secure/CreateWorklog!default.jspa?id=999",
                "It seems that you have tried to perform an operation which you are not permitted to perform.", true);

        assertOperationErrors("Delete",
                "secure/DeleteIssue!default.jspa?id=10000", "secure/DeleteIssue!default.jspa?id=999",
                "It seems that you have tried to perform an operation which you are not permitted to perform.", true);

        assertOperationErrors("Link",
                "secure/LinkJiraIssue!default.jspa?id=10000", "secure/LinkJiraIssue!default.jspa?id=999",
                "It seems that you have tried to perform an operation which you are not permitted to perform.", true);

        assertOperationErrors("Clone",
                "secure/CloneIssueDetails!default.jspa?id=10000", "secure/CloneIssueDetails!default.jspa?id=999",
                "It seems that you have tried to perform an operation which you are not permitted to perform.", false);
    }

    private void assertOperationErrors(String operationTitle, String goodUrl, String invalidUrl, String operationError, final boolean hasTargetIssue)
    {
        log("Testing '" + operationTitle + "'");

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //success case
        navigation.gotoPage(goodUrl);
        Locator locator = new CssLocator(tester, "#page div.form-body h2.dialog-title");
        text.assertTextPresent(locator, operationTitle);

        //issue that doesn't exist
        navigation.gotoPage(invalidUrl);
        assertions.getJiraMessageAssertions().assertHasTitle("Error");
        assertions.getJiraMessageAssertions().assertHasMessage("The issue no longer exists.");

        //logged out with issue that exists
        navigation.logout();
        navigation.gotoPage(goodUrl);
        assertions.getJiraMessageAssertions().assertHasTitle("Error");
        assertions.getJiraMessageAssertions().assertHasMessage("You do not have the permission to see the specified issue");

        tester.assertLinkPresentWithText("log in");
        //make sure the login link goes back to the issue and not to the attach file webpage!
        if (hasTargetIssue)
        {
            tester.assertTextPresent("login.jsp?os_destination=%2Fbrowse%2FHSP-1");
        }
        else
        {
            tester.assertTextPresent("login.jsp?os_destination=%2Fbrowse");
        }
        tester.assertLinkPresentWithText("sign up");

        //logged in but not enough permissions
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        navigation.gotoPage(goodUrl);
        assertions.getJiraMessageAssertions().assertHasTitle("Error");
        assertions.getJiraMessageAssertions().assertHasMessage(operationError);
        tester.assertLinkNotPresentWithText("log in");
        tester.assertLinkNotPresentWithText("sign up");
    }
}
