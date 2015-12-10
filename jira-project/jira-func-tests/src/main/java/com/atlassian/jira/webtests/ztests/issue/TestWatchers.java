package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestWatchers extends JIRAWebTest
{
    private static final Issue ISSUE = new Issue("HSP-1", 10000L);
    private static final String MANAGE_WATCHERS = "Watchers";

    private static final String ID_WATCH_LINK = "watching-toggle";
    private static final String ID_WATCH_SPAN = "watch-label";
    private static final String ID_VIEW_WATCHERS_LINK = "view-watcher-list";
    private static final String ID_WATCH_DATA = "watcher-data";
    private static final String ID_WATCH_ACTION = "toggle-watch-issue";
    private static final String ID_MANAGE_WATCHERS = "manage-watchers";

    private static final String XPATH_WATCHERS_SPAN_TITLE = String.format("//span[@id='%s']/@title", ID_WATCH_SPAN);

    private static final String MSG_MUST_BE_LOGGED_IN = "You have to be logged in to watch an issue.";
    private static final String MSG_GUEST_PERM = "You must log in to access this page";

    private static final String TITLE_WATCHING = "Stop watching this issue";
    private static final String TITLE_WATCH = "Start watching this issue";


    public TestWatchers(String name)
    {
        super(name);
    }

    public void testManageWatchers()
    {
        restoreData("TestWatchers.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        HttpUnitOptions.setScriptingEnabled(true);
        try
        {
            watcherOperationRemove();
            watcherOperationDeleteWatcher();
            addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
            addUserToGroup(BOB_USERNAME, Groups.DEVELOPERS);

            watcherOperationViewWatchingWithWatchPermission();
            watcherOperationWithManageandViewWatchingPermission();
            watcherOperationManageWatchingWithWatchPermission();
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testAnonymUserCannotWatch()
    {
        restoreData("TestWatchers.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        logout();
        gotoIssue(ISSUE);
        //Should not be able to watch.
        tester.assertLinkNotPresent(ID_WATCH_LINK);
        tester.assertLinkNotPresent(ID_WATCH_ACTION);
        //Should see this text when we can't watch.
        assertTextPresentInElement(ID_WATCH_SPAN, TITLE_WATCH);
        //Should see this title when we can't watch.
        assertEquals(MSG_MUST_BE_LOGGED_IN, getXpathText(XPATH_WATCHERS_SPAN_TITLE));

        //Make sure the watch count is correct
        assertEquals(0, getWatchCount());

        //Make sure we can't hit the action directly to hack our watch.
        watchIssueDirectly(ISSUE);
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), MSG_GUEST_PERM);

        //Go to the issue page again
        gotoIssue(ISSUE);

        //Make sure the watch count is still correct.
        assertEquals(0, getWatchCount());
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testUserCanWatch() throws Exception
    {
        restoreData("TestWatchers.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        gotoIssue(ISSUE);

        assertNotWatching(0);

        //Lets watch the issue and make sure everything is correct.
        tester.clickLink(ID_WATCH_LINK);
        assertWatching(1);

        //Lets try to watch the issue again and make sure this does not work.
        watchIssueDirectly(ISSUE);
        assertWatching(1);

        tester.clickLink(ID_WATCH_LINK);
        assertNotWatching(0);

        unwatchIssueDirectly(ISSUE);
        assertNotWatching(0);
    }

    // JRA-19814
    public void testCannotAddWatchersWhoDontHavePermissionToViewIssue() throws Exception
    {
        administration.restoreData("TestWatchersCannotAddUserWithNoPerms.xml");

        // HSP excludes fred by Browse permission
        _testCannotAddWatchersWhoDontHavePermissionToViewIssue("HSP-1");
        // MKY-1 excludes fred by Security Level
        _testCannotAddWatchersWhoDontHavePermissionToViewIssue("MKY-1");
    }

    public void testAddBadNames() throws Exception
    {
        administration.restoreData("TestWatchersCannotAddUserWithNoPerms.xml");
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink(ID_MANAGE_WATCHERS);

        tester.setWorkingForm("startform");
        // no names in box - should display error
        tester.submit("add");
        text.assertTextPresent(new WebPageLocator(tester), "You must select a user.");

        tester.setWorkingForm("startform");
        tester.setFormElement("userNames", "nonexistentuser");
        tester.submit("add");
        text.assertTextPresent(new WebPageLocator(tester), "The user \"nonexistentuser\" could not be found. This user will not be added to the watch list.");

        // even with multiple bad names - any good names should still be added
        tester.setWorkingForm("startform");
        tester.setFormElement("userNames", "a, b, admin, c");
        tester.submit("add");
        text.assertTextPresent(new WebPageLocator(tester), "The user \"a\" could not be found. This user will not be added to the watch list.");
        text.assertTextPresent(new WebPageLocator(tester), "The user \"b\" could not be found. This user will not be added to the watch list.");
        text.assertTextPresent(new WebPageLocator(tester), "The user \"c\" could not be found. This user will not be added to the watch list.");

        // assert that admin is now watching the issue
        tester.assertLinkNotPresentWithText("Watch Issue");
        tester.assertLinkPresentWithText("Stop Watching");
    }

    public void testBackToIssueLink()
    {
        restoreData("TestWatchers.xml");

        gotoIssue(ISSUE);
        clickLink(ID_VIEW_WATCHERS_LINK);

        assertions.assertNodeByIdExists("back-lnk");
        clickLink("back-lnk");

        assertions.assertNodeByIdExists(ID_VIEW_WATCHERS_LINK);
    }

    private void _testCannotAddWatchersWhoDontHavePermissionToViewIssue(final String issueKey)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink(ID_MANAGE_WATCHERS);

        // assert that admin is not yet watching the issue
        tester.assertLinkPresentWithText("Watch Issue");
        tester.assertLinkNotPresentWithText("Stop Watching");

        tester.setWorkingForm("startform");

        // user 'fred' doesn't have permission to browse project HSP, but admin does so he will be added
        tester.setFormElement("userNames", "fred, admin");
        tester.submit("add");

        // assert error for fred
        text.assertTextPresent(new WebPageLocator(tester), "The user \"fred\" does not have permission to view this issue. This user will not be added to the watch list.");

        // assert that admin is now watching the issue
        tester.assertLinkNotPresentWithText("Watch Issue");
        tester.assertLinkPresentWithText("Stop Watching");
    }

    /**
     * Tests the ability to view and remove watchers from the watcher list
     */
    private void watcherOperationRemove()
    {
        log("Watcher Operation: Test the ability to manage watchers");

        // Test if it is possible to watch
        startWatchingAnIssue(ISSUE.getKey());
        gotoIssue(ISSUE);
        clickLink(ID_MANAGE_WATCHERS);
        assertTextPresent(MANAGE_WATCHERS);
        assertLinkPresent("watcher_link_" + ADMIN_USERNAME);
        checkCheckbox("all");
        assertCheckboxSelected("all");
        getDialog().setWorkingForm("stopform");
        submit();
        assertTextPresent("There are no watchers.");
    }

    /**
     * Test that a user's watcher association is removed when the user is deleted
     */
    private void watcherOperationDeleteWatcher()
    {
        log("Watcher Operation: Test that a user's watcher association is removed when the user is deleted");
        startWatchingAnIssue(ISSUE.getKey(), new String[]
        { BOB_USERNAME, ADMIN_USERNAME });
        deleteUser(BOB_USERNAME);
        gotoIssue(ISSUE);
        clickLink(ID_VIEW_WATCHERS_LINK);
        assertLinkNotPresent("watcher_link_" + BOB_USERNAME);
        assertLinkPresent("watcher_link_" + ADMIN_USERNAME);

        removeAllWatchers(ISSUE.getKey());
    }

    /**
     * Test the availabilty of the 'View Watchers' Link with 'View Voters and Watchers' Permission"
     */
    private void watcherOperationViewWatchingWithWatchPermission()
    {
        log("Watcher Operation: Test the availabilty of the 'View Watchers' Link with 'View Voters and Watchers' Permission");
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(ISSUE);
        assertManageLinksPresent();

        removeGroupPermission(VIEW_VOTERS_AND_WATCHERS, Groups.DEVELOPERS);
        removeGroupPermission(MANAGE_WATCHER_LIST, Groups.ADMINISTRATORS);
        gotoIssue(ISSUE);
        assertManageLinksNotPresent();

        grantGroupPermission(VIEW_VOTERS_AND_WATCHERS, Groups.DEVELOPERS);
        grantGroupPermission(MANAGE_WATCHER_LIST, Groups.ADMINISTRATORS);
    }

    private void assertManageLinksPresent()
    {
        assertLinkPresent(ID_VIEW_WATCHERS_LINK);
        assertLinkPresent(ID_MANAGE_WATCHERS);
    }

    private void assertManageLinksNotPresent()
    {
        assertLinkNotPresent(ID_MANAGE_WATCHERS);
        assertLinkNotPresent(ID_VIEW_WATCHERS_LINK);
    }

    /**
     * Test the availabilty of the 'Manage Watchers' link with the 'Manage Watchers' Permission
     */
    private void watcherOperationManageWatchingWithWatchPermission()
    {
        log("Watcher Operation: Test the availabilty of the 'Manage Watchers'link with the 'Manage Watchers' Permission");
        removeGroupPermission(VIEW_VOTERS_AND_WATCHERS, Groups.DEVELOPERS);

        startWatchingAnIssue(ISSUE.getKey(), new String[]
        { FRED_USERNAME });

        // Check if another user cannot view the watchers
        logout();
        login(BOB_USERNAME, BOB_PASSWORD);
        gotoIssue(ISSUE);
        assertManageLinksNotPresent();
        logout();

        // check the proper user CAN view and edit the watchers
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(ISSUE);
        clickLink(ID_MANAGE_WATCHERS);
        assertTextPresent(MANAGE_WATCHERS);

        grantGroupPermission(VIEW_VOTERS_AND_WATCHERS, Groups.DEVELOPERS);

        logout();
        login(BOB_USERNAME, BOB_PASSWORD);
        gotoIssue(ISSUE);
        clickLink(ID_MANAGE_WATCHERS);
        assertTextNotPresent("Add Watchers");
        logout();

        // JRA-10308 grant the Manage permission to the reporter and check that not everyone gets it accidentally
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        grantPermissionToReporter(MANAGE_WATCHER_LIST);
        logout();
        login(BOB_USERNAME, BOB_PASSWORD);
        gotoIssue(ISSUE);
        clickLink(ID_MANAGE_WATCHERS);
        assertTextNotPresent("Add Watchers");
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    /**
     * Ensure that a user can view but not manage a watchers list
     */
    private void watcherOperationWithManageandViewWatchingPermission()
    {
        log("Watcher Operation: Test the difference between viewing and managing watcher lists");
        startWatchingAnIssue(ISSUE.getKey());

        // Check if the administrator can manage watch lists
        gotoIssue(ISSUE);
        clickLink(ID_MANAGE_WATCHERS);
        assertFormElementPresent("stopwatch_admin");
        grantGroupPermission(VIEW_VOTERS_AND_WATCHERS, Groups.USERS);

        // Check if the developer (bob) can manage but view watch lists
        logout();
        login(BOB_USERNAME, BOB_PASSWORD);
        gotoIssue(ISSUE);
        clickLink(ID_MANAGE_WATCHERS);
        assertFormElementNotPresent("stopwatch_admin");

        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        stopWatchingAnIssue(ISSUE.getKey());
        removeGroupPermission(VIEW_VOTERS_AND_WATCHERS, Groups.USERS);
    }

    private void assertTextPresentInElement(final String elementId, final String expectedText)
    {
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, elementId), expectedText);
    }

    private void assertWatching(final int watcherCount)
    {
        tester.assertLinkPresent(ID_WATCH_LINK);
        tester.assertLinkPresent(ID_WATCH_ACTION);
        tester.assertTextInElement(ID_WATCH_ACTION, "Stop Watching");

        assertTextPresentInElement(ID_WATCH_LINK, TITLE_WATCHING);
        assertEquals(watcherCount, getWatchCount());

        assertTextInElement(ID_WATCH_ACTION, "Stop Watching");
    }

    private void assertNotWatching(final int watcherCount)
    {
        tester.assertTextPresent(ID_WATCH_LINK);
        tester.assertLinkPresent(ID_WATCH_ACTION);
        tester.assertTextInElement(ID_WATCH_ACTION, "Watch");

        //Should see this text when we can watch.
        assertTextPresentInElement(ID_WATCH_LINK, TITLE_WATCH);
        //Make sure the watch count is correct.
        assertEquals(watcherCount, getWatchCount());

        assertTextInElement(ID_WATCH_ACTION, "Watch");
    }

    private void gotoIssue(final Issue issue)
    {
        navigation.issue().gotoIssue(issue.getKey());
    }

    private String getXpathText(final String xpath)
    {
        return StringUtils.trimToNull(new XPathLocator(tester, xpath).getText());
    }

    private int getWatchCount()
    {
        final String s = StringUtils.trimToNull(new IdLocator(tester, ID_WATCH_DATA).getText());
        if (s != null)
        {
            return Integer.parseInt(s);
        }
        else
        {
            fail("Unable to find watch count.");
            return Integer.MIN_VALUE;
        }
    }

    private void watchIssueDirectly(Issue issue)
    {
        navigation.gotoPage(page.addXsrfToken(String.format("/secure/VoteOrWatchIssue.jspa?id=%d&watch=watch", issue.getId())));
    }

    private void unwatchIssueDirectly(Issue issue)
    {
        navigation.gotoPage(page.addXsrfToken(String.format("/secure/VoteOrWatchIssue.jspa?id=%d&watch=unwatch", issue.getId())));
    }

    private static class Issue
    {
        private final String key;
        private final long id;

        private Issue(final String key, final long id)
        {
            this.key = key;
            this.id = id;
        }

        public String getKey()
        {
            return key;
        }

        public long getId()
        {
            return id;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
