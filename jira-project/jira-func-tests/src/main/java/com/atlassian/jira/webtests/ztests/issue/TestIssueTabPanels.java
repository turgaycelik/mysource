package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import org.junit.Ignore;

/**
 * Responsible for verifying that the tab panels in the view issue page work as expected.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueTabPanels extends FuncTestCase
{
    private final String commentLorem = "Nulla pulvinar leo et eros. Vestibulum tortor. Aenean aliquam odio a neque.";
    private static final String SORT_ICON_CONTAINER_LOCATOR =
            "//*[@id='activitymodule']//div[contains(concat(' ', normalize-space(@class), ' '), ' sortwrap ')]";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueTabPanels.xml");
    }

    /**
     * Verifies that <em>comments</em> is the default tab on the view issue page.
     */
    public void testShouldDefaultToCommentsTab()
    {
        navigation.issue().viewIssue("MKY-1");
        text.assertTextPresent(locator.page(), commentLorem);
    }

    /**
     * Verifies that the selected issue tab is sticky. It should be stored in the user's session.
     */
    public void testSelectedTabPanelIsStickyDuringTheSession()
    {
        navigation.issue().viewIssue("MKY-1");
        tester.clickLinkWithText("History");

        navigation.issue().viewIssue("MKY-1");

        assertTrue("Did not remember that we clicked on the 'History' panel.",
                locator.css("#changehistory-tabpanel.active").exists());

        assertFalse("The comments panel should not be active.",
                locator.css("#comment-tabpanel.active").exists());
    }

    /**
     * Verifies that when we go to a non-existent issue tab panel we end up on the default one.
     */
    public void testViewDefaultPanelWhenRequestedPanelDoesNotExist()
    {
        // Request a issue tab panel that does not exist
        tester.gotoPage("browse/MKY-1?page=unknown");

        // Verify we are on the Comment Issue tab panel and not getting an error screen
        text.assertTextPresent(locator.id("issue_actions_container"), commentLorem);
        text.assertTextNotPresent(locator.page(),"An unknown error occurred - actions == null. Please see logs for more details.");
    }

    /**
     * Test that when we have a panel in the session for a project that we have permission to see that when we move to
     * a project where we can't see the panel (i.e. version control) and verify that we end up on the default panel
     */
    @Ignore("The Version Control panel is no longer present. Is there another tab panel we could perform this test with?")
    public void testViewDefaultPanelWhenPanelPermissionsChange()
    {
        // browse to the monkey project issue and go to the cvs tab panel, this will set it in the session
        navigation.issue().gotoIssue("MKY-1");
        tester.clickLinkWithText("Version Control");

        // Now move straight to the homosap issue where we do not have permission to view the cvs panel, we should
        // be on the comment panel
        navigation.issue().gotoIssue("HSP-1");
        text.assertTextPresent(locator.page(), "There are no comments yet on this issue.");
    }

    /**
     * Verifies default sorting order, switching sorting order and sorting order persistence across tabs works as
     * expected. JRA-14927. TODO: Should probably be split in 3 tests methods.
     * @throws Exception caused by the sleep hack
     */
    public void testSortOrdering() throws Exception
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        final String[] ascComments = { "This is the first comment.", "This is the second comment.", "This is the third comment." };
        final String[] descComments = { "This is the third comment.", "This is the second comment.", "This is the first comment." };

        final String[] ascChangeItems = new String[] { "Status", "Open", "In Progress", "Status", "In Progress", "Open" };
        final String[] descChangeItems = new String[] { "Status", "In Progress", "Open", "Status", "Open", "In Progress" };

        for(String comment : ascComments)
        {
            navigation.issue().addComment("HSP-1", comment);            

            // TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            if (isOracle) { Thread.sleep(2000); }
        }
        tester.clickLinkWithText("Start Progress");

        // TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
        if(isOracle) { Thread.sleep(2000); }

        tester.clickLinkWithText("Stop Progress");

        // assert default order is ASC
        text.assertTextSequence(locator.page(), ascComments);
        tester.clickLinkWithText("History");
        text.assertTextSequence(locator.page(), ascChangeItems);

        // switch the order
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLinkWithText("Ascending order");
        text.assertTextSequence(locator.page(), descChangeItems);
        // ordering should persist across tabs
        tester.clickLinkWithText("Comments");
        text.assertTextSequence(locator.page(), descComments);

        // switch the order back
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLinkWithText("Descending order");
        text.assertTextSequence(locator.page(), ascComments);

        // ordering should persist across tabs
        tester.clickLinkWithText("History");
        text.assertTextSequence(locator.page(), ascChangeItems);
    }

    /**
     * Verifies that the sort order in shown in the tabs.
     */
    public void testSortOrderIsShownForIssueTabs()
    {
        tester.gotoPage("browse/MKY-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aall-tabpanel");
        text.assertTextPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");

        tester.gotoPage("browse/MKY-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Achangehistory-tabpanel");
        text.assertTextPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");

        tester.gotoPage("browse/MKY-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aworklog-tabpanel");
        text.assertTextPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");

        tester.gotoPage("browse/MKY-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel");
        text.assertTextPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");


        // HSP-1 has one or less item, so should not show sort order
        tester.gotoPage("browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aall-tabpanel");
        text.assertTextNotPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");

        tester.gotoPage("browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Achangehistory-tabpanel");
        text.assertTextNotPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");

        tester.gotoPage("browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aworklog-tabpanel");
        text.assertTextNotPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");

        tester.gotoPage("browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel");
        text.assertTextNotPresent(locator.xpath(SORT_ICON_CONTAINER_LOCATOR), "Ascending order");
    }

    /**
     * Func test for JRA-13743. Ensure that permalinking a worklog or comment opens the right tab
     */
    public void testPermalinking()
    {
        navigation.gotoDashboard();
        navigation.logout();

        final String worklogPermalink = "/browse/MKY-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aworklog-tabpanel#worklog-10001";
        final String worklogLorem = "Cras hendrerit porta tortor. Ut varius";
        final String commentPermalink = "/browse/MKY-1?focusedCommentId=10001&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-10001";

        tester.gotoPage(worklogPermalink);
        text.assertTextPresent(locator.page(), worklogLorem);

        navigation.gotoDashboard();

        tester.gotoPage(commentPermalink);
        text.assertTextPresent(locator.page(), commentLorem);
    }
}
