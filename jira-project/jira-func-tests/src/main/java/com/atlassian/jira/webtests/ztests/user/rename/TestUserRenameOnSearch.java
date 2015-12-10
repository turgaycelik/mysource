package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.JQL, Category.CHANGE_HISTORY })
public class TestUserRenameOnSearch extends AbstractJqlFuncTest
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("user_rename_search.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "betty");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "cc");
        assertSearchWithResultsForUser("admin", "", "COW-4", "COW-3", "COW-2", "COW-1");
    }

    //    KEY       USERNAME    NAME
    //    bb        betty       Betty Boop
    //    ID10001   bb          Bob Belcher
    //    cc        cat         Crazy Cat
    //    ID10101   cc          Candy Chaos

    public void testCurrentAssigneeAndReporter()
    {
        assertSearchWithResults("assignee = currentUser()", "COW-4");
        assertSearchWithResults("assignee = admin", "COW-4");
        assertSearchWithResults("assignee = bb", "COW-3");
        assertSearchWithResults("assignee = betty", "COW-1");
        assertBadValueWarning("assignee = candy", "assignee", "candy");
        assertSearchWithResults("assignee = cat", "COW-2");
        assertSearchWithResults("assignee = cc");
        assertSearchWithResults("assignee in (betty,bb)", "COW-3", "COW-1");
        assertSearchWithResults("assignee in (admin,bb)", "COW-4", "COW-3");
        assertSearchWithResults("assignee in (currentUser(),bb)", "COW-4", "COW-3");

        assertSearchWithResults("reporter = currentUser()", "COW-4");
        assertSearchWithResults("reporter = admin", "COW-4");
        assertSearchWithResults("reporter = bb");
        assertSearchWithResults("reporter = betty");
        assertBadValueWarning("reporter = candy", "reporter", "candy");
        assertSearchWithResults("reporter = cat", "COW-2", "COW-1");
        assertSearchWithResults("reporter = cc", "COW-3");
        assertSearchWithResults("reporter in (cc,cat)", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("reporter in (admin,cat)", "COW-4", "COW-2", "COW-1");
        assertSearchWithResults("reporter in (currentUser(),cat)", "COW-4", "COW-2", "COW-1");
    }

    public void testCurrentCustomFieldValue()
    {
        assertSearchWithResults("tester = currentUser()");
        assertSearchWithResults("tester = admin");
        assertSearchWithResults("tester = bb");
        assertSearchWithResults("tester = betty");
        assertBadValueWarning("tester = candy", "tester", "candy");
        assertSearchWithResults("tester = cat", "COW-3", "COW-2");
        assertSearchWithResults("tester = cc", "COW-1");
        assertSearchWithResults("tester in (cc,cat)", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("tester in (admin,cat)", "COW-3", "COW-2");
        assertSearchWithResults("tester in (currentUser(),cat)", "COW-3", "COW-2");

        assertSearchWithResults("cc = currentUser()", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("cc = admin", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("cc = bb", "COW-2", "COW-1");
        assertSearchWithResults("cc = betty", "COW-3");
        assertBadValueWarning("cc = candy", "cc", "candy");
        assertSearchWithResults("cc = cat");
        assertSearchWithResults("cc = cc");
        assertSearchWithResults("cc in (cc,cat)");
        assertSearchWithResults("cc in (admin,cat)", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("cc in (currentUser(),cat)", "COW-3", "COW-2", "COW-1");
    }

    public void testVotersAndWatchers()
    {
        initVotersAndWatchers("betty", "bb");

        assertSearchWithResults("voter = currentUser()");
        assertSearchWithResults("voter = admin");
        assertSearchWithResults("voter = bb", "COW-3");
        assertSearchWithResults("voter = betty", "COW-1");
        assertBadValueWarning("voter = candy", "voter", "candy");
        assertSearchWithResults("voter = cat");
        assertSearchWithResults("voter = cc");
        assertSearchWithResults("voter in (bb,betty)", "COW-3", "COW-1");
        assertSearchWithResults("voter in (admin,bb)", "COW-3");
        assertSearchWithResults("voter in (currentUser(),bb)", "COW-3");

        assertSearchWithResults("watcher = currentUser()", "COW-4", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("watcher = admin", "COW-4", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("watcher = bb", "COW-4");
        assertSearchWithResults("watcher = betty", "COW-2");
        assertBadValueWarning("watcher = candy", "watcher", "candy");
        assertSearchWithResults("watcher = cat");
        assertSearchWithResults("watcher = cc");
        assertSearchWithResults("watcher in (bb,betty)", "COW-4", "COW-2");
        assertSearchWithResults("watcher in (admin,bb)", "COW-4", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("watcher in (currentUser(),bb)", "COW-4", "COW-3", "COW-2", "COW-1");
    }

    /*
     * History items...
     *
     * SELECT i.pkey,cg.author,ci.*
     * FROM jiraissue i
     * INNER JOIN changegroup cg ON cg.issueid = i.id
     * INNER JOIN changeitem ci ON ci.groupid = cg.id
     * WHERE ci.field in ('assignee','reporter')
     * ORDER BY ci.field, i.pkey DESC;
     *
     *  pkey  | author  |  id   | groupid | fieldtype |  field   | oldvalue |  oldstring  | newvalue |  newstring
     * -------+---------+-------+---------+-----------+----------+----------+-------------+----------+-------------
     *  COW-3 | admin   | 10500 |   10500 | jira      | assignee | admin    | Adam Ant    | ID10001  | The new BB
     *  COW-2 | ID10001 | 10600 |   10600 | jira      | assignee | admin    | Adam Ant    | cc       | Crazy Cat
     *  COW-1 | admin   | 10101 |   10101 | jira      | assignee | admin    | Adam Ant    | kiran    | Kiran
     *  COW-1 | bb      | 10400 |   10400 | jira      | assignee | kiran    | Kiran       | bb       | Betty Boop
     *  COW-3 | ID10001 | 10706 |   10704 | jira      | reporter | bb       | Betty Boop  | ID10101  | Candy Chaos
     *  COW-3 | admin   | 10502 |   10500 | jira      | reporter | admin    | Adam Ant    | ID10001  | The new BB
     *  COW-3 | cc      | 10704 |   10703 | jira      | reporter | ID10001  | Bob Belcher | bb       | Betty Boop
     *  COW-2 | ID10001 | 10602 |   10600 | jira      | reporter | admin    | Adam Ant    | cc       | Crazy Cat
     *  COW-1 | bb      | 10402 |   10400 | jira      | reporter | admin    | Adam Ant    | bb       | Betty Boop
     *  COW-1 | admin   | 10700 |   10700 | jira      | reporter | bb       | Betty Boop  | cc       | Crazy Cat
     *
     *  New changes that get added to verify that change history is created with keys vs. usernames:
     */


    public void testChangedBy()
    {
        assertSearchWithResults("reporter changed by admin", "COW-3", "COW-1");
        assertSearchWithResults("reporter changed by bb", "COW-3", "COW-2");
        assertSearchWithResults("reporter changed by cat", "COW-3");
        assertSearchWithResults("reporter changed by cc");

        assertSearchWithResults("assignee changed by admin", "COW-3", "COW-1");
        assertSearchWithResults("assignee changed by bb", "COW-2");
        assertSearchWithResults("assignee changed by cat");
        assertSearchWithResults("assignee changed by cc");

        assertByClauseError("reporter changed by asdfasdf", "asdfasdf");
        assertByClauseError("assignee changed by candy", "candy");
    }

    public void testWasClauseForAssigneeAndReporter()
    {
        assertSearchWithResults("assignee was currentUser()", "COW-4", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("assignee was admin", "COW-4", "COW-3", "COW-2", "COW-1");
        assertBadValueError("assignee was asdfasdf", "assignee", "asdfasdf");
        assertSearchWithResults("assignee was bb", "COW-3");
        assertBadValueError("assignee was candy", "assignee", "candy");
        assertSearchWithResults("assignee was cat", "COW-2");
        assertBadValueError("assignee was in (candy, cat)", "assignee", "candy");
        assertSearchWithResults("assignee was cc");

        assertSearchWithResults("reporter was currentUser()", "COW-4", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("reporter was admin", "COW-4", "COW-3", "COW-2", "COW-1");
        assertBadValueError("reporter was asdfasdf", "reporter", "asdfasdf");
        assertSearchWithResults("reporter was bb", "COW-3");
        assertBadValueError("reporter was candy", "reporter", "candy");
        assertSearchWithResults("reporter was cat", "COW-2", "COW-1");
        assertBadValueError("reporter was in (candy, cat)", "reporter", "candy");
        assertSearchWithResults("reporter was cc", "COW-3");
    }

    private void checkBettyAndCandy(final String goodBetty, final String badBetty, final String goodCandy, final String badCandy)
    {
        assertSearchWithResults("assignee = " + goodCandy);
        assertSearchWithResults("assignee changed by " + goodBetty + "", "COW-1");
        assertSearchWithResults("assignee was " + goodBetty + "", "COW-1");
        assertSearchWithResults("assignee was in (" + goodBetty + ", cat)", "COW-2", "COW-1");
        assertSearchWithResults("assignee was in (" + goodCandy + ", " + goodBetty + ")", "COW-1");
        assertSearchWithResults("assignee changed from " + goodBetty + "");
        assertSearchWithResults("assignee changed to " + goodBetty + "", "COW-1");

        assertSearchWithResults("reporter = " + goodCandy, "COW-3");
        assertSearchWithResults("reporter in (" + goodCandy + ", cat)", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("reporter was " + goodBetty + "", "COW-3", "COW-1");
        assertSearchWithResults("reporter was in (" + goodBetty + ", cat)", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("reporter was in (" + goodCandy + ", " + goodBetty + ")", "COW-3", "COW-1");
        assertSearchWithResults("reporter changed by " + goodBetty + "", "COW-1");
        assertSearchWithResults("reporter changed from " + goodBetty + "", "COW-3", "COW-1");
        assertSearchWithResults("reporter changed to " + goodBetty + "", "COW-3", "COW-1");
        assertSearchWithResults("reporter changed from bb to " + goodBetty + "", "COW-3");
        assertSearchWithResults("reporter changed from " + goodBetty + " to bb");
        assertBadValueError("reporter changed from " + goodBetty + " to " + badCandy, "reporter", badCandy);
        assertSearchWithResults("reporter changed from " + goodBetty + " to cat", "COW-1");
        assertSearchWithResults("reporter changed from " + goodBetty + " to " + goodCandy, "COW-3");

        assertSearchWithResults("tester = " + goodCandy, "COW-1");
        assertSearchWithResults("cc = " + goodCandy);
        assertSearchWithResults("voter = " + goodCandy, "COW-1");
        assertSearchWithResults("watcher = " + goodCandy, "COW-2");
        assertSearchWithResults("voter = " + goodBetty, "COW-3");
        assertSearchWithResults("watcher = " + goodBetty, "COW-4");

        assertBadValueWarning("assignee = " + badCandy, "assignee", badCandy);
        assertByClauseError("assignee changed by " + badBetty, badBetty);
        assertBadValueError("assignee changed to " + badBetty, "assignee", badBetty);
        assertBadValueWarning("reporter = " + badCandy, "reporter", badCandy);
        assertByClauseError("reporter changed by " + badBetty, badBetty);
        assertBadValueError("reporter changed from " + badBetty, "reporter", badBetty);
        assertBadValueWarning("tester = " + badCandy, "tester", badCandy);
        assertBadValueWarning("cc = " + badCandy, "cc", badCandy);
        assertBadValueWarning("voter = " + badCandy, "voter", badCandy);
        assertBadValueWarning("watcher = " + badCandy, "watcher", badCandy);

        try
        {
            backdoor.issueTableClient().loginAs(goodBetty, "betty");
            assertCurrentUserQueryResultsForBetty();
            backdoor.issueTableClient().loginAs(goodCandy, "cc");
            assertCurrentUserQueryResultsForCandy();
        }
        finally
        {
            backdoor.issueTableClient().loginAs(ADMIN_USERNAME);
        }
    }

    private void assertCurrentUserQueryResultsForBetty()
    {
        assertSearchWithResults("assignee = currentUser()", "COW-1");
        assertSearchWithResults("reporter = currentUser()");
        assertSearchWithResults("reporter in (currentUser(), cat)", "COW-2", "COW-1");
        assertSearchWithResults("tester = currentUser()");
        assertSearchWithResults("cc = currentUser()", "COW-3");
        assertSearchWithResults("voter = currentUser()", "COW-3");
        assertSearchWithResults("watcher = currentUser()", "COW-4");
    }

    private void assertCurrentUserQueryResultsForCandy()
    {
        assertSearchWithResults("assignee = currentUser()");
        assertSearchWithResults("reporter = currentUser()", "COW-3");
        assertSearchWithResults("reporter in (currentUser(), cat)", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("tester = currentUser()", "COW-1");
        assertSearchWithResults("cc = currentUser()");
        assertSearchWithResults("voter = currentUser()", "COW-1");
        assertSearchWithResults("watcher = currentUser()", "COW-2");
    }

    public void testChangedFromAndToForAssigneeAndReporter()
    {
        assertSearchWithResults("assignee changed from admin", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("assignee changed from bb");
        assertBadValueError("assignee changed from candy", "assignee", "candy");
        assertSearchWithResults("assignee changed from cat");
        assertSearchWithResults("assignee changed from cc");

        assertSearchWithResults("assignee changed to admin");
        assertSearchWithResults("assignee changed to bb", "COW-3");
        assertBadValueError("assignee changed to candy", "assignee", "candy");
        assertSearchWithResults("assignee changed to cat", "COW-2");
        assertSearchWithResults("assignee changed to cc");

        assertSearchWithResults("assignee changed from admin to cat", "COW-2");
        assertSearchWithResults("assignee changed from admin to cc");
        assertBadValueError("assignee changed from admin to candy", "assignee", "candy");
        assertSearchWithResults("assignee changed from cat to admin");
        assertSearchWithResults("assignee changed from cc to admin");

        assertSearchWithResults("reporter changed from admin", "COW-3", "COW-2", "COW-1");
        assertSearchWithResults("reporter changed from bb", "COW-3");
        assertBadValueError("reporter changed from candy", "reporter", "candy");
        assertSearchWithResults("reporter changed from cat");
        assertSearchWithResults("reporter changed from cc");

        assertSearchWithResults("reporter changed to admin");
        assertSearchWithResults("reporter changed to bb", "COW-3");
        assertBadValueError("reporter changed to candy", "reporter", "candy");
        assertSearchWithResults("reporter changed to cat", "COW-2", "COW-1");
        assertSearchWithResults("reporter changed to cc", "COW-3");

        assertSearchWithResults("reporter changed from admin to cat", "COW-2");
        assertBadValueError("reporter changed from admin to candy", "reporter", "candy");
    }

    private void initVotersAndWatchers(final String user1, final String user2)
    {
        try
        {
            navigation.logout();
            navigation.login(user1);
            navigation.issue().viewIssue("COW-1").toggleVote();
            navigation.issue().viewIssue("COW-2").toggleWatch();

            navigation.logout();
            navigation.login(user2);
            navigation.issue().viewIssue("COW-3").toggleVote();
            navigation.issue().viewIssue("COW-4").toggleWatch();
        }
        finally
        {
            navigation.logout();
            navigation.login(ADMIN_USERNAME);
        }
    }

    public void testRenameUser()
    {
        initVotersAndWatchers("cc", "betty");
        checkBettyAndCandy("betty", "boop", "cc", "candy");

        renameUser("cc", "candy");
        renameUser("betty", "boop");

        checkBettyAndCandy("boop", "betty", "candy", "cc");
    }

    private void renameUser(final String oldUsername, final String newUsername)
    {
        administration.usersAndGroups().gotoEditUser(oldUsername).setUsername(newUsername).submitUpdate();
        // Now we are on View User
        assertEquals("/secure/admin/user/ViewUser.jspa?name=" + newUsername, navigation.getCurrentPage());
        assertEquals(newUsername, locator.id("username").getText());

    }

    private void assertByClauseError(String jqlQuery, String badUser)
    {
        final String expectedMessage = "The user '" + badUser + "' does not exist and cannot be used in the 'by' predicate.";
        assertSearchWithError(jqlQuery, expectedMessage);
    }

    private void assertBadValueError(String jqlQuery, String badField, String badValue)
    {
        final String expectedMessage = "The value '" + badValue + "' does not exist for the field '" + badField + "'.";
        assertSearchWithError(jqlQuery, expectedMessage);
    }

    private void assertBadValueWarning(String jqlQuery, String badField, String badValue)
    {
        final String expectedMessage = "The value '" + badValue + "' does not exist for the field '" + badField + "'.";
        assertSearchWithWarning(jqlQuery, expectedMessage);
    }
}

