package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.user.EditUserPage;
import com.atlassian.jira.functest.framework.page.IssueSearchPage;
import com.atlassian.jira.functest.framework.page.ManageWatchersPage;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.page.ViewVotersPage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;

import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static java.util.Arrays.asList;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.ISSUES, Category.ISSUE_NAVIGATOR})
public class TestUserRenameOnIssues extends FuncTestCase
{
    private static final int ISSUE_COW_2 = 10100;
    private static final int ISSUE_COW_3 = 10101;
    private static final int ISSUE_COW_4 = 10200;

    private static final int CF_CC = 10200;
    private static final int CF_TESTER = 10300;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        //    KEY       USERNAME    NAME
        //    bb	    betty	    Betty Boop
        //    ID10001	bb	        Bob Belcher
        //    cc	    cat	        Crazy Cat
        //    ID10101	cc	        Candy Chaos
        administration.restoreData("user_rename.xml");
    }

    public void testUserRenameOnIssues() throws Exception
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // COW-1
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-1");
        assertEquals("Betty Boop", viewIssuePage.getAssignee());
        assertEquals("Crazy Cat", viewIssuePage.getReporter());
        assertEquals("Adam Ant, Bob Belcher", viewIssuePage.getCustomFieldValue(CF_CC));
        assertEquals("Candy Chaos", viewIssuePage.getCustomFieldValue(CF_TESTER));

        // COW-3
        viewIssuePage = navigation.issue().viewIssue("COW-3");
        assertEquals("Bob Belcher", viewIssuePage.getAssignee());
        assertEquals("Candy Chaos", viewIssuePage.getReporter());
        assertEquals("Adam Ant, Betty Boop", viewIssuePage.getCustomFieldValue(CF_CC));
        assertEquals("Crazy Cat", viewIssuePage.getCustomFieldValue(CF_TESTER));

        // Now test Search
        IssueSearchPage issueSearchPage = navigation.issueNavigator().runSearch("");

        System.out.println("getCurrentPage() = " + navigation.getCurrentPage());
        HtmlTable searchResultsTable = issueSearchPage.getResultsTable();
        // COW-3
        assertEquals("COW-3", searchResultsTable.getRow(2).getCellForHeading("Key"));
        assertEquals("Bob Belcher", searchResultsTable.getRow(2).getCellForHeading("Assignee"));
        assertEquals("Candy Chaos", searchResultsTable.getRow(2).getCellForHeading("Reporter"));
        assertEquals("Crazy Cat", searchResultsTable.getRow(2).getCellForHeading("Tester"));
        assertEquals("Adam Ant, Betty Boop", searchResultsTable.getRow(2).getCellForHeading("CC"));
        // COW-1
        assertEquals("COW-1", searchResultsTable.getRow(4).getCellForHeading("Key"));
        assertEquals("Betty Boop", searchResultsTable.getRow(4).getCellForHeading("Assignee"));
        assertEquals("Crazy Cat", searchResultsTable.getRow(4).getCellForHeading("Reporter"));
        assertEquals("Candy Chaos", searchResultsTable.getRow(4).getCellForHeading("Tester"));
        assertEquals("Adam Ant, Bob Belcher", searchResultsTable.getRow(4).getCellForHeading("CC"));
    }

    private void doWatchAndVoteAsBetty() throws Exception
    {
        navigation.logout();
        navigation.login("betty");

        // Vote on COW-3
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-3");
        assertEquals(0, viewIssuePage.getVoteCount());
        viewIssuePage = viewIssuePage.toggleVote();
        assertEquals(1, viewIssuePage.getVoteCount());
        assertEquals(true, viewIssuePage.hasVoted());
        // Remove Vote
        viewIssuePage = viewIssuePage.toggleVote();
        assertEquals(0, viewIssuePage.getVoteCount());
        assertEquals(false, viewIssuePage.hasVoted());
        viewIssuePage = viewIssuePage.toggleVote();  // Put it back for admin to check later
        // Watch COW-3
        assertEquals(1, viewIssuePage.getWatcherCount());
        viewIssuePage = viewIssuePage.toggleWatch();
        assertEquals(2, viewIssuePage.getWatcherCount());
        assertEquals(true, viewIssuePage.isWatching());
        // UnWatch COW-2
        viewIssuePage = navigation.issue().viewIssue("COW-2");
        assertEquals(true, viewIssuePage.isWatching());
        assertEquals(3, viewIssuePage.getWatcherCount());
        viewIssuePage = viewIssuePage.toggleWatch();
        assertEquals(2, viewIssuePage.getWatcherCount());
        assertEquals(false, viewIssuePage.isWatching());
        viewIssuePage.toggleVote();  // Vote for admin to check later
    }

    private void doWatchAndVoteAsBob() throws Exception
    {
        // Login as Bob Belcher (recycled user)
        navigation.logout();
        navigation.login("bb");

        // Vote on COW-4
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-4");
        assertEquals(0, viewIssuePage.getVoteCount());
        viewIssuePage = viewIssuePage.toggleVote();
        assertEquals(1, viewIssuePage.getVoteCount());
        assertEquals(true, viewIssuePage.hasVoted());
        // Remove Vote
        viewIssuePage = viewIssuePage.toggleVote();
        assertEquals(0, viewIssuePage.getVoteCount());
        assertEquals(false, viewIssuePage.hasVoted());
        viewIssuePage = viewIssuePage.toggleVote();  // Put it back for admin to check later
        // Watch COW-4
        assertEquals(1, viewIssuePage.getWatcherCount());
        viewIssuePage = viewIssuePage.toggleWatch();
        assertEquals(2, viewIssuePage.getWatcherCount());
        assertEquals(true, viewIssuePage.isWatching());
        // UnWatch COW-2
        viewIssuePage = navigation.issue().viewIssue("COW-2");
        assertEquals(true, viewIssuePage.isWatching());
        assertEquals(2, viewIssuePage.getWatcherCount());
        viewIssuePage = viewIssuePage.toggleWatch();
        assertEquals(1, viewIssuePage.getWatcherCount());
        assertEquals(false, viewIssuePage.isWatching());
        viewIssuePage.toggleVote();  // Vote for admin to check later
    }

    public void testWatchersAndVoters() throws Exception
    {
        ManageWatchersPage manageWatchersPage = navigation.gotoPageWithParams(ManageWatchersPage.class, "id=" + ISSUE_COW_2);
        List<String> watchers = manageWatchersPage.getCurrentWatchers();

        assertEquals(asList("Adam Ant (admin)"), watchers);

        // Add a renamed user
        manageWatchersPage = manageWatchersPage.addWatchers("betty");
        watchers = manageWatchersPage.getCurrentWatchers();
        assertEquals(asList("Adam Ant (admin)", "Betty Boop (betty)"), watchers);

        // Add a recycled username
        manageWatchersPage = manageWatchersPage.addWatchers("bb");
        watchers = manageWatchersPage.getCurrentWatchers();
        assertEquals(asList("Adam Ant (admin)", "Betty Boop (betty)", "Bob Belcher (bb)"), watchers);

        // Login as Betty Boop (renamed user)
        try
        {
            doWatchAndVoteAsBetty();
            doWatchAndVoteAsBob();
        }
        finally
        {
            // Do this in finally block so we don't screw up other tests on fail.
            navigation.logout();
            navigation.login("admin");
        }

        assertEquals(asList("Betty Boop (betty)", "Bob Belcher (bb)"), votersFor(ISSUE_COW_2));
        assertEquals(asList("Betty Boop (betty)"), votersFor(ISSUE_COW_3));
        assertEquals(asList("Bob Belcher (bb)"), votersFor(ISSUE_COW_4));
    }

    public void testVotingOnOwnIssue() throws Exception
    {
        // Renamed user
        navigation.login("cat");
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-2");
        assertFalse("Renamed user shouldn't be able to vote on its own issue", viewIssuePage.canVote());

        viewIssuePage = navigation.issue().viewIssue("COW-3");
        assertTrue("Renamed user should be able to vote on someone else's issue", viewIssuePage.canVote());

        // recycled user
        navigation.logout();
        navigation.login("cc");
        viewIssuePage = navigation.issue().viewIssue("COW-3");
        assertFalse("Recycled user shouldn't be able to vote on its own issue", viewIssuePage.canVote());

        viewIssuePage = navigation.issue().viewIssue("COW-2");
        assertTrue("Recycled user should be able to vote on someone else's issue", viewIssuePage.canVote());
    }

    public void testRenameUser() throws Exception
    {
        try
        {
            navigation.logout();
            navigation.login("cc");

            // Vote for COW-4 and watch it
            ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-4");
            viewIssuePage = viewIssuePage.toggleVote();
            viewIssuePage.toggleWatch();
        }
        finally
        {
            // Do this in finally block so we don't screw up other tests on fail.
            navigation.logout();
            navigation.login("admin");
        }

        // COW-1
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-1");
        assertEquals("Crazy Cat", viewIssuePage.getReporter());
        assertEquals("Candy Chaos", viewIssuePage.getCustomFieldValue(CF_TESTER));

        // COW-4
        assertEquals(asList("Candy Chaos (cc)"), votersFor(ISSUE_COW_4));
        assertEquals(asList("Adam Ant (admin)", "Candy Chaos (cc)"), watchersFor(ISSUE_COW_4));

        // Rename cc to candy
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cc");
        editUserPage.setUsername("candy");
        editUserPage.submitUpdate();
        // Now we are on View User
        assertEquals("/secure/admin/user/ViewUser.jspa?name=candy", navigation.getCurrentPage());
        assertEquals("candy", locator.id("username").getText());

        // COW-1 - Issue should still have Candy in the CF
        viewIssuePage = navigation.issue().viewIssue("COW-1");
        assertEquals("Crazy Cat", viewIssuePage.getReporter());
        assertEquals("Candy Chaos", viewIssuePage.getCustomFieldValue(CF_TESTER));
        assertEquals(asList("Candy Chaos (candy)"), votersFor(ISSUE_COW_4));
        assertEquals(asList("Adam Ant (admin)", "Candy Chaos (candy)"), watchersFor(ISSUE_COW_4));
    }

    public void testRenameUserValidation() throws Exception
    {
        // try to rename Candy to blank
        navigation.gotoPage("secure/admin/user/EditUser!default.jspa?editName=cc");
        tester.setFormElement("username", "");
        tester.submit("Update");
        assertEquals("You must specify a username.", locator.id("user-edit-username-error").getText());

        // try to rename Candy to a username that already exists
        tester.setFormElement("username", "betty");
        tester.submit("Update");
        assertEquals("A user with that username already exists.", locator.id("user-edit-username-error").getText());

        // try to rename Candy to a username that is invalid
        tester.setFormElement("username", "i<3u");
        tester.submit("Update");
        assertEquals("The username must not contain '<', '>' or '&'.", locator.id("user-edit-username-error").getText());

        // try to rename Candy to a username that is invalid
        tester.setFormElement("username", "bet>");
        tester.submit("Update");
        assertEquals("The username must not contain '<', '>' or '&'.", locator.id("user-edit-username-error").getText());

        // try to rename Candy to a username that is invalid
        tester.setFormElement("username", "yes&no");
        tester.submit("Update");
        assertEquals("The username must not contain '<', '>' or '&'.", locator.id("user-edit-username-error").getText());
    }

    private List<String> watchersFor(int issueId)
    {
        return navigation.gotoPageWithParams(ManageWatchersPage.class, "id=" + issueId).getCurrentWatchers();
    }

    private List<String> votersFor(int issueId)
    {
        return navigation.gotoPageWithParams(ViewVotersPage.class, "id=" + issueId).getCurrentVoters();
    }
}
