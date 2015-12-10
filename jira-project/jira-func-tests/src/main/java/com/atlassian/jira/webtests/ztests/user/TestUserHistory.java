package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.UserProfile;
import com.atlassian.jira.functest.framework.parser.issue.ViewIssueDetails;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Testing for MSSQL and the UserHistoryItem table. We need MSSQL to be cause-insensitive by default so that column
 * names are case insensitive. However, this causes problems with the UserHistoryItem table where there is a unique
 * index on the ('UserName,Type,EntityId') columns. Basically, JIRA was doing a case sensitive compare on 'EntityId' while
 * the database was not which meant that JIRA tried to insert a duplicate (e.g. ('admin', 'assignee', 'Assignee') and
 * ('admin', 'assignee', 'assignee')) if the case of a username changed (NOTE: The case problem is in the EntityId
 * column and not the UserName).
 *
 * From 5.2 onwards we insert items into the UserHistoryItem table when an issue is created or assigned. This meant
 * that a duplicate key error could cause JIRA to stop creating issues because a DB runtime exception is thrown.
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserHistory extends FuncTestCase
{
    private static final String INTERNAL_ADMIN_FULL = "Administrator in the Shadows";
    private static final String INTERNAL_ADMIN_NAME = "admin";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestUserHistoryErrors.xml");
    }

    public void testCreateIssue() throws Exception
    {
        //This would have failed in the past on MSSQL because it would have thrown a 500 error with a duplicate
        //key on on the user history item table.
        String summary = "Second Issue";
        createIssue(summary, INTERNAL_ADMIN_NAME);
        assertIssue(summary, INTERNAL_ADMIN_FULL);
    }

    public void testProfilePage() throws Exception
    {
        //This would have failed in the past on MSSQL because it would have thrown a 500 error with a duplicate
        //key on on the user history item table.
        UserProfile userProfile = navigation.userProfile();
        userProfile.gotoCurrentUserProfile();
        assertEquals(INTERNAL_ADMIN_FULL, userProfile.userName());
    }

    private void assertIssue(String summary, String fullUserName)
    {
        final ViewIssueDetails viewIssueDetails = parse.issue().parseViewIssuePage();
        assertEquals(summary, viewIssueDetails.getSummary());
        assertEquals(fullUserName, viewIssueDetails.getAssignee());
    }

    private String createIssue(String summary, String userName)
    {
        tester.clickLink("create_link");
        tester.setWorkingForm("issue-create");
        tester.submit();

        tester.setFormElement("summary", summary);
        tester.setFormElement("assignee", userName);
        tester.submit();

        return parse.issue().parseViewIssuePage().getKey();
    }
}
