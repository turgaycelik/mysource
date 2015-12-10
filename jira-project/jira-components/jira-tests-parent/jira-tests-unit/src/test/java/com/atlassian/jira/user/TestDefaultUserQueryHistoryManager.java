package com.atlassian.jira.user;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for the {@link com.atlassian.jira.user.DefaultUserQueryHistoryManager}
 * @since v4.0
 */
public class TestDefaultUserQueryHistoryManager extends MockControllerTestCase
{
    private DefaultUserQueryHistoryManager queryHistoryManager;
    private UserHistoryManager historyManager;
    private User user;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        historyManager = getMock(UserHistoryManager.class);
        applicationProperties = getMock(ApplicationProperties.class);
        queryHistoryManager = new DefaultUserQueryHistoryManager(historyManager, applicationProperties);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        queryHistoryManager = null;
    }

    @Test
    public void testAddQueryNullQuery() throws Exception
    {
        mockController.replay();

        try
        {
            queryHistoryManager.addQueryToHistory(user, null);
            fail("query cannot be null");
        }
        catch (IllegalArgumentException e)
        {
            // pass
        }

        mockController.verify();
    }

    @Test
    public void testAddQueryNullUser() throws Exception
    {
        final String query = "project = \"homosapien\"";
        historyManager.addItemToHistory(UserHistoryItem.JQL_QUERY, (com.atlassian.crowd.embedded.api.User) null, String.valueOf(query.hashCode()), query);

        historyManager.getHistory(UserHistoryItem.JQL_QUERY, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(new UserHistoryItem(UserHistoryItem.JQL_QUERY, String.valueOf(query), query)).asList());

        applicationProperties.getDefaultBackedString("jira.max.JQLQuery.history.items");
        mockController.setReturnValue("10");

        mockController.replay();

        queryHistoryManager.addQueryToHistory(null, query);

        final List<UserHistoryItem> history = queryHistoryManager.getUserQueryHistory(null);
        assertEquals(1, history.size());
        assertEquals(query, history.get(0).getData());

        mockController.verify();
    }

    @Test
    public void testAddQueries() throws Exception
    {
        final String query1 = "project = \"homosapien\"";
        final String query2 = "project = \"monkeys\"";
        historyManager.addItemToHistory(UserHistoryItem.JQL_QUERY, user, String.valueOf(query1.hashCode()), query1);
        historyManager.addItemToHistory(UserHistoryItem.JQL_QUERY, user, String.valueOf(query2.hashCode()), query2);

        historyManager.getHistory(UserHistoryItem.JQL_QUERY, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(new UserHistoryItem(UserHistoryItem.JQL_QUERY, String.valueOf(query2.hashCode()),query2), new UserHistoryItem(UserHistoryItem.JQL_QUERY, String.valueOf(query1.hashCode()),query1)).asList());

        applicationProperties.getDefaultBackedString("jira.max.JQLQuery.history.items");
        mockController.setReturnValue("10");

        mockController.replay();

        queryHistoryManager.addQueryToHistory(user, query1);
        queryHistoryManager.addQueryToHistory(user, query2);

        final List<UserHistoryItem> history = queryHistoryManager.getUserQueryHistory(user);
        assertEquals(2, history.size());
        assertEquals(query2, history.get(0).getData());
        assertEquals(query1, history.get(1).getData());

        mockController.verify();
    }

    @Test
    public void testAddMaximumQueries()
    {
        String[] queries = new String[25];
        for (int i = 0; i < queries.length; i++)
        {
            queries[i] = new String("query " + i);
            historyManager.addItemToHistory(UserHistoryItem.JQL_QUERY, user, String.valueOf(queries[i].hashCode()), queries[i]);
        }

        List<UserHistoryItem> historyItems = new ArrayList<UserHistoryItem>();

        for (int i = 0; i < queries.length; i++)
        {
            String query = queries[i];
            historyItems.add(new UserHistoryItem(UserHistoryItem.JQL_QUERY, String.valueOf(query.hashCode()),query));
        }

        historyManager.getHistory(UserHistoryItem.JQL_QUERY, user);
        mockController.setReturnValue(historyItems);

        applicationProperties.getDefaultBackedString("jira.max.JQLQuery.history.items");
        mockController.setReturnValue("10");

        mockController.replay();

        for (int i = 0; i < queries.length; i++)
        {
            queries[i] = new String("query " + i);
            queryHistoryManager.addQueryToHistory(user, queries[i]);
        }

        final List<UserHistoryItem> history = queryHistoryManager.getUserQueryHistory(user);
        assertEquals(10, history.size());
        for (int i = 0; i < 10; i++)
        {
           assertEquals(queries[i], history.get(i).getData());
        }

        mockController.verify();
    }

}
