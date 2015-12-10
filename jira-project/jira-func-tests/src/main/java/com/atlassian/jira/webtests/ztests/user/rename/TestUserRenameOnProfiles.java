package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.IssueSearchPage;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.page.ViewProfilePage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static java.util.Arrays.asList;

/**
 * These tests essentially mirror those of {@link TestUserRenameOnIssues},
 * but using the usernames instead of the full names.  Also included are
 * tests for the ViewUserHover action, which provides the content for the
 * pop-up when you hover on a user's full name.
 *
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER })
public class TestUserRenameOnProfiles extends FuncTestCase
{
    private static final int CFID_CC = 10200;
    private static final int CFID_TESTER = 10300;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("user_rename.xml");
    }

    //    KEY       USERNAME    NAME
    //    bb        betty       Betty Boop
    //    ID10001   bb          Bob Belcher
    //    cc        cat         Crazy Cat
    //    ID10101   cc          Candy Chaos

    public void testUserRenameOnProfiles() throws Exception
    {
        // As "Adam Ant" (the admin)
        assertCurrentUserProfileNames("admin", "Adam Ant");
        assertSpecificUserProfileNames("cc", "Candy Chaos");
        assertSpecificUserProfileNames("cat", "Crazy Cat");

        try
        {
            safeLogin("betty", "betty");
            assertCurrentUserProfileNames("betty", "Betty Boop");
            assertSpecificUserProfileNames("bb", "Bob Belcher");

            safeLogin("bb", "bb");
            assertCurrentUserProfileNames("bb", "Bob Belcher");
            assertSpecificUserProfileNames("betty", "Betty Boop");
            assertSpecificUserProfileNames("admin", "Adam Ant");

            safeLogin("bb#asdf", "bb#asdf");
            navigation.userProfile().gotoCurrentUserProfile();
            assertViewProfileNames("bb#asdf", "BB Asdf");
            assertTrue(new ViewProfilePage(locator).getChangePasswordLink().endsWith("username=bb%23asdf"));
        }
        finally
        {
            safeLogin("admin", "admin");
        }
    }

    // Check that user hover fields are using the correct username for the user hovers
    // to work as expected
    public void testUserHoverLinks()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // COW-1
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-1");
        assertEquals("betty", viewIssuePage.getAssigneeUsername());
        assertEquals("cat", viewIssuePage.getReporterUsername());
        assertEquals(asList("admin", "bb"), viewIssuePage.getCustomFieldRelUsernames(CFID_CC));
        assertEquals("cc", viewIssuePage.getCustomFieldRelUsername(CFID_TESTER));

        // COW-3
        viewIssuePage = navigation.issue().viewIssue("COW-3");
        assertEquals("bb", viewIssuePage.getAssigneeUsername());
        assertEquals("cc", viewIssuePage.getReporterUsername());
        assertEquals(asList("admin", "betty"), viewIssuePage.getCustomFieldRelUsernames(CFID_CC));
        assertEquals("cat", viewIssuePage.getCustomFieldRelUsername(CFID_TESTER));

        // Now test Search
        IssueSearchPage issueSearchPage = navigation.issueNavigator().runSearch("");
        HtmlTable searchResultsTable = issueSearchPage.getResultsTable();

        // COW-3
        HtmlTable.Row row = searchResultsTable.getRow(2);
        assertTableUsername("bb", row, "Assignee");
        assertTableUsername("cc", row, "Reporter");
        assertTableUsername("cat", row, "Tester");
        assertTableUsernames(asList("admin", "betty"), row, "CC");

        // COW-1
        row = searchResultsTable.getRow(4);
        assertTableUsername("betty", row, "Assignee");
        assertTableUsername("cat", row, "Reporter");
        assertTableUsername("cc", row, "Tester");
        assertTableUsernames(asList("admin", "bb"), row, "CC");

        administration.usersAndGroups().gotoUserBrowser();
        tester.clickLinkWithText("bb#asdf");
        tester.assertLinkPresentWithText("bb_asdf@example.com");
    }

    public void testUserHoverResults()
    {
        assertUserHoverDetailsForMissingUser("doesnotexist");
        assertUserHoverDetails("admin", "Adam Ant", "mlassau@atlassian.com");
        assertUserHoverDetails("bb", "Bob Belcher", "bob@example.com");
        assertUserHoverDetails("betty", "Betty Boop", "betty@example.com");
        assertUserHoverDetails("cat", "Crazy Cat", "cat@example.com");
        assertUserHoverDetails("cc", "Candy Chaos", "candy@example.com");
    }

    public void testRenameUser() throws Exception
    {
        // COW-1
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("COW-1");
        assertEquals("cat", viewIssuePage.getReporterUsername());
        assertEquals("cc", viewIssuePage.getCustomFieldRelUsername(CFID_TESTER));

        // Rename cc to candy
        navigation.gotoPage("secure/admin/user/EditUser!default.jspa?editName=cc");
        tester.setFormElement("username", "candy");
        tester.submit("Update");
        // Now we are on View User
        assertEquals("/secure/admin/user/ViewUser.jspa?name=candy", navigation.getCurrentPage());
        assertEquals("candy", locator.id("username").getText());

        // COW-1 - Issue should still have Candy in the CF, but with the new username
        viewIssuePage = navigation.issue().viewIssue("COW-1");
        assertEquals("cat", viewIssuePage.getReporterUsername());
        assertEquals("candy", viewIssuePage.getCustomFieldRelUsername(CFID_TESTER));

        navigation.userProfile().gotoUserProfile("candy");
        assertViewProfileNames("candy", "Candy Chaos");

        // cc is now unknown, but candy is the real deal
        assertUserHoverDetailsForMissingUser("cc");
        assertUserHoverDetails("candy", "Candy Chaos", "candy@example.com");
    }



    private void safeLogin(String username, String password)
    {
        try
        {
            navigation.logout();
        }
        finally
        {
            navigation.login(username, password);
        }
    }

    private void gotoUserHover(String username)
    {
        navigation.gotoPage("/secure/ViewUserHover!default.jspa?decorator=none&username=" + username);
    }



    private void assertUserHoverDetailsForMissingUser(String username)
    {
        gotoUserHover(username);
        assertFalse(locator.id("avatar-full-name-link").exists());
        assertEquals("User does not exist: " + username, locator.css("div.user-hover-details").getText());
        assertEquals("", locator.id("user-hover-email").getText());
    }

    private void assertUserHoverDetails(String username, String fullName, String emailAddress)
    {
        gotoUserHover(username);
        final Element link = (Element)locator.id("avatar-full-name-link").getNode();
        assertEquals(username, link.getAttribute("title"));
        assertEquals(fullName, link.getTextContent().trim());
        assertEquals(emailAddress, locator.id("user-hover-email").getText());
    }



    private void assertCurrentUserProfileNames(String username, String fullname)
    {
        navigation.userProfile().gotoCurrentUserProfile();
        assertViewProfileNames(username, fullname);
        assertTrue(new ViewProfilePage(locator).getChangePasswordLink().endsWith("username=" + username));
    }

    private void assertSpecificUserProfileNames(String username, String fullname)
    {
        navigation.userProfile().gotoUserProfile(username);
        assertViewProfileNames(username, fullname);
    }

    private void assertViewProfileNames(String username, String fullName)
    {
        ViewProfilePage viewProfilePage = new ViewProfilePage(locator);
        assertEquals(username, viewProfilePage.getUsername());
        assertEquals(fullName, viewProfilePage.getFullName());
    }



    private void assertTableUsername(String username, HtmlTable.Row row, String heading)
    {
        final Node cellNode = row.getCellNodeForHeading(heading);
        assertEquals(username, ViewIssuePage.getRelUsername(cellNode));
    }

    private void assertTableUsernames(List<String> usernames, HtmlTable.Row row, String heading)
    {
        final Node cellNode = row.getCellNodeForHeading(heading);
        assertEquals(usernames, ViewIssuePage.getRelUsernames(cellNode));
    }
}
