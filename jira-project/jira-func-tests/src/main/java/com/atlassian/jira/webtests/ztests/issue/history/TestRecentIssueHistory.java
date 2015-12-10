package com.atlassian.jira.webtests.ztests.issue.history;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;

import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;

/**
 * Test that the recent issue history action returns expected data
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestRecentIssueHistory extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    /**
     * Order matters.  The most recently visited issue goes first
     */
    public void testVisitedIssues() throws Exception
    {
        final IssueNavigation issueNavigation = navigation.issue();

        // at first there should be no history
        assertHistoryhasTheseIssues(new String[0]);

        String HSP_1 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-1|");
        assertHistoryhasTheseIssues(new String[] { HSP_1 });

        String HSP_2 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-2|");
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1 });

        String HSP_3 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-3|");
        assertHistoryhasTheseIssues(new String[] { HSP_3, HSP_2, HSP_1 });

        issueNavigation.viewIssue(HSP_1);
        assertHistoryhasTheseIssues(new String[] { HSP_1, HSP_3, HSP_2 });

        issueNavigation.viewIssue(HSP_2);
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1, HSP_3 });

    }

    public void testVisitedIssuesForNotLoggedIn() throws Exception
    {
        final IssueNavigation issueNavigation = navigation.issue();

        tester.gotoPage(page.addXsrfToken("secure/admin/AddPermission.jspa?permissions=" + BROWSE_PROJECTS.permissionKey() + "&schemeId=0&type=group"));

        // at first there should be no history
        assertHistoryhasTheseIssues(new String[0]);

        String HSP_1 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-1|");
        assertHistoryhasTheseIssues(new String[] { HSP_1 });

        String HSP_2 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-2|");
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1 });

        String HSP_3 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-3|");
        assertHistoryhasTheseIssues(new String[] { HSP_3, HSP_2, HSP_1 });
        navigation.logout();

        assertHistoryhasTheseIssues(new String[0]);

        issueNavigation.viewIssue(HSP_1);
        assertHistoryhasTheseIssues(new String[] { HSP_1});

        issueNavigation.viewIssue(HSP_2);
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1});

        navigation.login(ADMIN_USERNAME);
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1, HSP_3});

        navigation.logout();

        assertHistoryhasTheseIssues(new String[0]);

        issueNavigation.viewIssue(HSP_1);
        assertHistoryhasTheseIssues(new String[] { HSP_1});

        issueNavigation.viewIssue(HSP_2);
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1});

        issueNavigation.viewIssue(HSP_1);
        assertHistoryhasTheseIssues(new String[] { HSP_1, HSP_2});

        navigation.login(ADMIN_USERNAME);
        issueNavigation.viewIssue(HSP_3);
        assertHistoryhasTheseIssues(new String[] { HSP_3, HSP_1, HSP_2});

    }

    public void testDeletedIssuesChangesHistory() throws Exception
    {
        final IssueNavigation issueNavigation = navigation.issue();

        String HSP_1 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-1|");
        String HSP_2 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-2|");
        String HSP_3 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-3|");
        assertHistoryhasTheseIssues(new String[] { HSP_3, HSP_2, HSP_1 });

        // now start deleting them
        deleteIssue(HSP_3);
        assertHistoryhasTheseIssues(new String[] { HSP_2, HSP_1 });
        deleteIssue(HSP_2);
        assertHistoryhasTheseIssues(new String[] { HSP_1 });
        deleteIssue(HSP_1);
        assertHistoryhasTheseIssues(new String[] { });
    }

    public void testOrdering() throws Exception
    {
        final IssueNavigation issueNavigation = navigation.issue();

        String HSP_1 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-1|");
        String HSP_2 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-2|");
        String HSP_3 = issueNavigation.createIssue("homosapien", null, "summary of |HSP-3|");

        final SearchClient searchClient = new SearchClient(environmentData);
        final SearchRequest searchRequest = new SearchRequest();
        searchRequest.fields("key").jql("order by lastViewed DESC");

        // DESC
        SearchResult result = searchClient.getSearch(searchRequest);

        assertEquals(3, result.issues.size());
        assertEquals("HSP-3", result.issues.get(0).key);
        assertEquals("HSP-2", result.issues.get(1).key);
        assertEquals("HSP-1", result.issues.get(2).key);

        // ASC
        searchRequest.jql("order by lastViewed ASC");
        result = searchClient.getSearch(searchRequest);

        assertEquals(3, result.issues.size());
        assertEquals("HSP-1", result.issues.get(0).key);
        assertEquals("HSP-2", result.issues.get(1).key);
        assertEquals("HSP-3", result.issues.get(2).key);


        // With empty
        tester.gotoPage(page.addXsrfToken("secure/admin/AddPermission.jspa?permissions=" + BROWSE_PROJECTS + "&schemeId=0&type=group"));
        navigation.logout();
        navigation.login("fred");
        searchClient.loginAs("fred");

        searchRequest.jql("order by lastViewed DESC");
        result = searchClient.getSearch(searchRequest);

        assertEquals(3, result.issues.size());
        assertEquals("HSP-1", result.issues.get(0).key);
        assertEquals("HSP-2", result.issues.get(1).key);
        assertEquals("HSP-3", result.issues.get(2).key);

        issueNavigation.viewIssue(HSP_3);

        result = searchClient.getSearch(searchRequest);

        assertEquals(3, result.issues.size());
        assertEquals("HSP-3", result.issues.get(0).key);
        assertEquals("HSP-1", result.issues.get(1).key);
        assertEquals("HSP-2", result.issues.get(2).key);

        issueNavigation.viewIssue(HSP_2);

        result = searchClient.getSearch(searchRequest);

        assertEquals(3, result.issues.size());
        assertEquals("HSP-2", result.issues.get(0).key);
        assertEquals("HSP-3", result.issues.get(1).key);
        assertEquals("HSP-1", result.issues.get(2).key);

        searchRequest.jql("order by lastViewed ASC");
        result = searchClient.getSearch(searchRequest);

        assertEquals(3, result.issues.size());
        assertEquals("HSP-1", result.issues.get(0).key);
        assertEquals("HSP-3", result.issues.get(1).key);
        assertEquals("HSP-2", result.issues.get(2).key);

    }

    private void deleteIssue(String issueKey) {
        tester.setWorkingForm("quicksearch");
        tester.setFormElement("searchString", issueKey);
        tester.submit();
        tester.clickLink("delete-issue");
        tester.submit("Delete");
    }

    private void assertHistoryhasTheseIssues(String issueKeys[])
    {

        //
        // test the JSON view
        tester.gotoPage("secure/RecentIssueHistory.jspa?json=true&decorator=none");
        final String webPageText = tester.getDialog().getResponseText();
        if (issueKeys.length == 0)
        {
            text.assertTextSequence(webPageText, new String[] { "[", "]" });
        }
        else
        {
            text.assertTextSequence(webPageText, issueKeys);
        }

        //
        // now test the HTML view
        tester.gotoPage("secure/RecentIssueHistory.jspa");
        if (issueKeys.length == 0)
        {
            // special case it should have no data
            text.assertTextPresent(new WebPageLocator(tester), "No issues in your history");
            //
            // assert that the Issue History link is NOT there
//            Locator historyLink = new XPathLocator(tester,"//a[@id='user_history']");
//            assertNull(historyLink.getNode());
        }
        else
        {
            TableLocator tableLocator = new TableLocator(tester, "recent_history_list");
            int rowCount = tableLocator.getTable().getRowCount();
            int colCount = tableLocator.getTable().getColumnCount();
            assertEquals(2, colCount);
            for (int i = 0; i < issueKeys.length; i++)
            {
                String issueKey = issueKeys[i];
                TableCellLocator cellLocator = new TableCellLocator(tester, "recent_history_list", i, 0);
                assertEquals(issueKey, cellLocator.getText());
            }
            //
            // assert that the Issue History link IS there
//            Locator historyLink = new XPathLocator(tester,"//a[@id='user_history']");
//            assertNotNull(historyLink.getNode());
        }



    }
}
