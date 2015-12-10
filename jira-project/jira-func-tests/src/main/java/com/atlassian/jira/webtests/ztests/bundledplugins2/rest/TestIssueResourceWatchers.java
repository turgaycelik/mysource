package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Permissions;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.Watches;
import com.atlassian.jira.testkit.client.restclient.WatchersClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.testkit.client.restclient.matcher.HasErrorMessage.hasErrorMessage;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func tests for issue watching use cases.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceWatchers extends RestFuncTest
{
    private static final String TEST_XML = "TestIssueResourceWatchers.xml";

    /**
     * The user that is logged in.
     */
    private final String loginUser = FRED_USERNAME;
    private final String slashName = "kelpie/trevor";

    /**
     * The other user.
     */
    private final String anotherUser = "luser";
    private IssueClient issueClient;
    private WatchersClient watchersClient;

    public void testWatchingDisabled() throws Exception
    {
        restoreData(false);

        Issue issue = issueClient.get("HSP-1");
        assertNull(issue.fields.watches);
    }

    public void testViewWatchersRequestExpanded() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("HSP-1");
        assertEquals("HSP-1", issue.key);
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-1/watchers", issue.fields.watches.self);
    }

    public void testViewWatchersNoPermissionToViewButCanStillSeeCount() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.loginAs("luser").get("HSP-1");
        assertEquals("HSP-1", issue.key);

        // should view watcher count only
        assertEquals(2, issue.fields.watches.watchCount);
    }

    public void testViewWatchersNoPermissionToViewButCanStillSeeMyselfAndCount() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.loginAs("luser").get("HSP-2");
        assertEquals("HSP-2", issue.key);

        Watches watchers = issue.fields.watches;
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-2/watchers", watchers.self);

        // should view watcher count only
        assertEquals(2, watchers.watchCount);
    }

    public void testViewWatchersIsWatching() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.loginAs("luser").get("HSP-2");
        assertEquals("HSP-2", issue.key);

        Watches watchers = issue.fields.watches;
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-2/watchers", watchers.self);
        assertTrue(watchers.isWatching);
    }

    public void testViewWatchersIsNotWatching() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.loginAs("luser").get("HSP-1");
        assertEquals("HSP-1", issue.key);

        Watches watchers = issue.fields.watches;
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-1/watchers", watchers.self);
        assertFalse(watchers.isWatching);
    }

    public void testWatchers_Anonymous() throws Exception
    {
        restoreData(true);
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.BROWSE, "");

        Watches watchers = watchersClient.anonymous().get("HSP-1");
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-1/watchers", watchers.self);
        assertEquals(2, watchers.watchCount);
        assertFalse(watchers.isWatching);
        assertEquals(0, watchers.watchers.size());
    }

    public void testViewWatchersSubresourceExpanded() throws Exception
    {
        restoreData(true);
        Watches watchers = watchersClient.get("HSP-1");
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-1/watchers", watchers.self);

        List<User> watcherList = watchers.watchers;
        assertEquals(2, watcherList.size());

        List<String> names = new ArrayList<String>();
        for (User watcher : watcherList)
        {
            names.add(watcher.name);
        }

        assertTrue(names.contains(FRED_USERNAME));
        assertTrue(names.contains(ADMIN_USERNAME));
    }

    public void testViewWatchersSubresourceExpandedIssueDoesNotExist() throws Exception
    {
        restoreData(true);

        Response response = watchersClient.getResponse("HSP-999");
        assertEquals(404, response.statusCode);
    }

    public void testAddWatchWhenWatchingIsDisabled() throws Exception
    {
        restoreData(false);

        Response resp = watchersClient.loginAs(loginUser).postResponse("HSP-2", loginUser);
        assertEquals(404, resp.statusCode);
    }

    public void testAddWatchToIssueThatDoesNotExist() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(loginUser).postResponse("HSP-999", loginUser);
        assertEquals(404, resp.statusCode);
    }

    public void testAddWatchToIssueThatIAmAlreadyWatching() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(loginUser).postResponse("HSP-1", loginUser);
        assertEquals(204, resp.statusCode);
    }

    public void testAddWatchToIssue() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(loginUser).postResponse("HSP-2", loginUser);
        assertEquals(204, resp.statusCode);
        assertTrue(isWatching("HSP-2", loginUser));
    }

    public void testAddWatchToIssue_emptyPOST() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(loginUser).postResponse("HSP-2", null);
        assertEquals(204, resp.statusCode);
        assertTrue(isWatching("HSP-2", loginUser));
    }

    public void testAddWatchForAnotherUser() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(anotherUser).postResponse("HSP-2", loginUser);
        assertEquals(401, resp.statusCode);
    }

    public void testAddWatchForAnotherUserCannotView() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.postResponse("MKY-1", "jack");
        assertEquals(401, resp.statusCode);
        assertEquals("The user \"jack\" does not have permission to view this issue. This user will not be added to the watch list.", resp.entity.errorMessages.get(0));
    }

    public void testRemoveWatchForAnotherUserCannotView() throws Exception
    {
        restoreData(true);

        // This tests that we can remove a watcher that no longer has permission to view this issue.  (He cannot be added back)

        Response resp = watchersClient.deleteResponse("MKY-2", "jack");
        assertEquals(204, resp.statusCode);
        resp = watchersClient.postResponse("MKY-2", "jack");
        assertEquals(401, resp.statusCode);
        assertEquals("The user \"jack\" does not have permission to view this issue. This user will not be added to the watch list.", resp.entity.errorMessages.get(0));
    }

    public void testRemoveWatchWhenWatchingIsDisabled() throws Exception
    {
        restoreData(false);

        Response resp = watchersClient.loginAs(loginUser).deleteResponse("HSP-2", loginUser);
        assertEquals(404, resp.statusCode);
    }

    public void testRemoveWatchToIssueThatDoesNotExist() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(loginUser).deleteResponse("HSP-999", loginUser);
        assertEquals(404, resp.statusCode);
    }

    public void testRemoveWatchFromIssueThatIAmNotWatching() throws Exception
    {
        restoreData(true);
        assertFalse(isWatching("HSP-2", loginUser));

        Response resp = watchersClient.loginAs(loginUser).deleteResponse("HSP-2", loginUser);
        assertEquals(204, resp.statusCode);
        assertFalse(isWatching("HSP-2", loginUser));
    }

    public void testRemoveWatchForAnotherUser_Denied() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.loginAs(anotherUser).deleteResponse("HSP-2", loginUser);
        assertEquals(401, resp.statusCode);
        assertThat(resp, hasErrorMessage("User 'luser' is not allowed to remove watchers from issue 'HSP-2'"));
    }

    public void testRemoveWatchForAnotherUser_Allowed() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.deleteResponse("HSP-2", loginUser);
        assertEquals(204, resp.statusCode);
        assertFalse(isWatching("HSP-2", loginUser));
    }

    public void testRemoveWatch() throws Exception
    {
        restoreData(true);
        assertTrue(isWatching("HSP-1", loginUser));

        Response resp = watchersClient.deleteResponse("HSP-1", loginUser);
        assertEquals(204, resp.statusCode);
        assertFalse(isWatching("HSP-1", loginUser));
    }

    public void testSlashNamesWatchIssue() throws Exception
    {
        restoreData(true);

        Response resp = watchersClient.postResponse("HSP-2", slashName);
        assertEquals(204, resp.statusCode);
        assertTrue(isWatching("HSP-2", slashName));

        resp = watchersClient.deleteResponse("HSP-2", slashName);
        assertEquals(204, resp.statusCode);
        assertFalse(isWatching("HSP-2", slashName));
    }

    public void testAddingOrRemovingWatcherFromIssuesThatIAmNotAllowedToSeeReturns401() throws Exception
    {
        restoreData(true);

        // add/remove a vote as anonymous
        {
            Response addResponse = watchersClient.anonymous().postResponse("HSP-1", loginUser);
            assertThat(addResponse.statusCode, equalTo(401));
            assertThat(addResponse, hasErrorMessage("You do not have the permission to see the specified issue."));

            Response delResponse = watchersClient.anonymous().deleteResponse("HSP-1", loginUser);
            assertThat(delResponse.statusCode, equalTo(401));
            assertThat(addResponse, hasErrorMessage("You do not have the permission to see the specified issue."));
        }

        // add/remove a vote as jack
        {
            Response addResponse = watchersClient.loginAs("jack").postResponse("HSP-1", loginUser);
            assertThat(addResponse.statusCode, equalTo(401));
            assertThat(addResponse, hasErrorMessage("User 'jack' is not allowed to add watchers to issue 'HSP-1'"));

            Response delResponse = watchersClient.loginAs("jack").deleteResponse("HSP-1", loginUser);
            assertThat(delResponse.statusCode, equalTo(401));
            assertThat(delResponse, hasErrorMessage("User 'jack' is not allowed to remove watchers from issue 'HSP-1'"));
        }
    }

    protected boolean isWatching(String issueKey, String username)
    {
        Watches watchers = watchersClient.get(issueKey);
        for (User watcher : watchers.watchers)
        {
            if (username.equals(watcher.name))
            {
                return true;
            }
        }

        return false;
    }

    protected void restoreData(boolean watchingEnabled) throws IOException
    {
        administration.restoreData(TEST_XML);
        if (!watchingEnabled)
        {
            administration.generalConfiguration().disableWatching();
        }
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        watchersClient = new WatchersClient(getEnvironmentData());
    }
}
