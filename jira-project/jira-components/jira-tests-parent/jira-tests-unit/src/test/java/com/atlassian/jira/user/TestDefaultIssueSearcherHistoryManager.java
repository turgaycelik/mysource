package com.atlassian.jira.user;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v5.2
 */
public class TestDefaultIssueSearcherHistoryManager extends MockControllerTestCase
{
    private DefaultUserIssueSearcherHistoryManager issueSearcherHistoryManager;
    private UserHistoryManager historyManager;
    private User user;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        historyManager = getMock(UserHistoryManager.class);
        applicationProperties = getMock(ApplicationProperties.class);
        issueSearcherHistoryManager = new DefaultUserIssueSearcherHistoryManager(historyManager, applicationProperties);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        issueSearcherHistoryManager = null;
    }

    @Test
    public void testAddNullIssueSearcher() throws Exception
    {
        mockController.replay();

        try
        {
            issueSearcherHistoryManager.addIssueSearcherToHistory(user, null);
            fail("query cannot be null");
        }
        catch (IllegalArgumentException e)
        {
            // pass
        }

        mockController.verify();
    }

    @Test
    public void testAddProjectNullUser()
    {
        final IssueSearcher searcher = mockController.getMock(IssueSearcher.class);
        final SearcherInformation searcherInformation = mockController.getMock(SearcherInformation.class);
        searcher.getSearchInformation();
        mockController.setReturnValue(searcherInformation);
        searcherInformation.getId();
        mockController.setReturnValue("123");

        historyManager.addItemToHistory(UserHistoryItem.ISSUESEARCHER, (com.atlassian.crowd.embedded.api.User) null, "123");

        mockController.replay();

        issueSearcherHistoryManager.addIssueSearcherToHistory(null, searcher);

        mockController.verify();
    }

    @Test
    public void testAddIssueSearcher()
    {
        final IssueSearcher issueSearcher = getMock(IssueSearcher.class);
        final SearcherInformation searcherInformation = mockController.getMock(SearcherInformation.class);

        issueSearcher.getSearchInformation();
        mockController.setReturnValue(searcherInformation);

        searcherInformation.getId();
        mockController.setReturnValue("123");

        historyManager.addItemToHistory(UserHistoryItem.ISSUESEARCHER, user, "123");

        mockController.replay();

        issueSearcherHistoryManager.addIssueSearcherToHistory(user, issueSearcher);

        mockController.verify();
    }

    @Test
    public void testGettingUsersCurrentIssueSearchers()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "1236");

        applicationProperties.getDefaultBackedString("jira.max.Searcher.history.items");
        mockController.setReturnValue("10");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.ISSUESEARCHER, user);
        mockController.setReturnValue(list);

        mockController.replay();

        List<UserHistoryItem> result = issueSearcherHistoryManager.getUserIssueSearcherHistory(user);
        assertEquals(4, result.size());

        mockController.verify();

    }

    @Test
    public void testGettingMaximumUsersCurrentIssueSearchers()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUESEARCHER, "1236");

        applicationProperties.getDefaultBackedString("jira.max.Searcher.history.items");
        mockController.setReturnValue("2");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.ISSUESEARCHER, user);
        mockController.setReturnValue(list);

        mockController.replay();

        List<UserHistoryItem> result = issueSearcherHistoryManager.getUserIssueSearcherHistory(user);
        assertEquals(2, result.size());

        mockController.verify();

    }

}
