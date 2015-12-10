package com.atlassian.jira.user;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.atlassian.jira.user.UserHistoryItem.ISSUE;
import static com.atlassian.jira.user.UserHistoryItem.PROJECT;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class TestOfbizUserHistoryStore
{
    private OfBizDelegator ofBizDelegator;
    private ApplicationUser user;
    private ApplicationUser user2;
    private OfBizUserHistoryStore store;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        ofBizDelegator = new MockOfBizDelegator();
        user = new MockApplicationUser("Admin");
        user2 = new MockApplicationUser("Test");
        ApplicationProperties properties = new MockApplicationProperties();
        properties.setString(APKeys.JIRA_MAX_HISTORY_ITEMS, "5");
        store = new OfBizUserHistoryStore(ofBizDelegator, properties);
    }

    @After
    public void tearDown() throws Exception
    {
        ofBizDelegator = null;
        user = null;
        user2 = null;
        store = null;
    }

    @Test
    public void testAddNoCheck()
    {
        UserHistoryItem item1 = new UserHistoryItem(ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234", 2);

        List<UserHistoryItem> results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        store.addHistoryItemNoChecks(user, item1);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item1));

        store.addHistoryItemNoChecks(user, item2);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item2, item1));
    }

    @Test
    public void testUpdateNoCheck()
    {
        UserHistoryItem item1 = new UserHistoryItem(ISSUE, "123", 1);
        UserHistoryItem item1New = new UserHistoryItem(ISSUE, "123", 3);
        UserHistoryItem item2 = new UserHistoryItem(ISSUE, "1234", 2);

        List<UserHistoryItem> results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        store.updateHistoryItemNoChecks(user, item1);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item1));

        store.addHistoryItemNoChecks(user, item2);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item2, item1));

        store.updateHistoryItemNoChecks(user, item1New);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item1New, item2));
    }

    @SuppressWarnings ("ConstantConditions")
    @Test
    public void testAddHistoryItemNullValues()
    {
        UserHistoryItem item = new UserHistoryItem(ISSUE, "123");
        try
        {
            store.addHistoryItem(null, item);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            store.addHistoryItem(user, null);
            fail("history item can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            store.getHistory(ISSUE, (ApplicationUser) null);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            store.getHistory(null, user);
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
    }

    @Test
    public void testHistoryItem()
    {
        List<UserHistoryItem> results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        UserHistoryItem item1 = new UserHistoryItem(ISSUE, "123", 1);
        store.addHistoryItem(user, item1);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item1));

        results = store.getHistory(ISSUE, user2);
        assertThat(results, noHistory());

        results = store.getHistory(PROJECT, user);
        assertThat(results, noHistory());

        UserHistoryItem item1again = new UserHistoryItem(ISSUE, "123", 2);
        store.addHistoryItem(user, item1again);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item1again));

        results = store.getHistory(ISSUE, user2);
        assertThat(results, noHistory());

        results = store.getHistory(PROJECT, user);
        assertThat(results, noHistory());

        store.addHistoryItem(user2, new UserHistoryItem(ISSUE, "123", 3));
        store.addHistoryItem(user, new UserHistoryItem(PROJECT, "123", 4));

        UserHistoryItem item2 = new UserHistoryItem(ISSUE, "124", 5);
        store.addHistoryItem(user, item2);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item2, item1again));

        item1again = new UserHistoryItem(ISSUE, "123", 6);
        store.addHistoryItem(user, item1again);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item1again, item2));

        UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1233", 7);
        store.addHistoryItem(user, item3);

        UserHistoryItem item4 = new UserHistoryItem(ISSUE, "1234", 8);
        store.addHistoryItem(user, item4);

        UserHistoryItem item5 = new UserHistoryItem(ISSUE, "1235", 9);
        store.addHistoryItem(user, item5);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item5, item4, item3, item1again, item2));

        UserHistoryItem item6 = new UserHistoryItem(ISSUE, "1236", 10);
        store.addHistoryItem(user, item6);

        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item6, item5, item4, item3, item1again));

        UserHistoryItem item7 = new UserHistoryItem(ISSUE, "1237", 11);
        store.addHistoryItem(user, item7);
        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item7, item6, item5, item4, item3));

        item5 = new UserHistoryItem(ISSUE, "1235", 12);
        store.addHistoryItem(user, item5);
        results = store.getHistory(ISSUE, user);
        assertThat(results, contains(item5, item7, item6, item4, item3));
    }

    @Test
    public void testRemoveHistoryNullUser()
    {
        try
        {
            //noinspection ConstantConditions
            store.removeHistoryForUser(null);
            fail("User can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
    }

    @Test
    public void testRemoveHistory()
    {
        List<UserHistoryItem> results;
        Set<UserHistoryItem.Type> types = store.removeHistoryForUser(user);

        assertThat(types, noTypes());

        UserHistoryItem item1 = new UserHistoryItem(ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(ISSUE, "124", 2);
        UserHistoryItem item3 = new UserHistoryItem(PROJECT, "123", 3);
        store.addHistoryItem(user, item1);

        types = store.removeHistoryForUser(user);
        assertThat(types, contains(ISSUE));
        results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        store.addHistoryItem(user, item1);

        types = store.removeHistoryForUser(user);
        assertThat(types, contains(ISSUE));
        results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        types = store.removeHistoryForUser(user);
        assertThat(types, noTypes());

        store.addHistoryItem(user, item1);
        store.addHistoryItem(user, item2);

        types = store.removeHistoryForUser(user);
        assertThat(types, contains(ISSUE));
        results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        store.addHistoryItem(user, item1);
        store.addHistoryItem(user, item3);
        store.addHistoryItem(user, item2);

        types = store.removeHistoryForUser(user);
        assertThat(types, containsInAnyOrder(ISSUE, PROJECT));
        results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());
        results = store.getHistory(PROJECT, user);
        assertThat(results, noHistory());

        store.addHistoryItem(user, item1);
        store.addHistoryItem(user2, item3);
        store.addHistoryItem(user, item2);

        types = store.removeHistoryForUser(user);
        assertThat(types, contains(ISSUE));
        results = store.getHistory(PROJECT, user2);
        assertThat(results, contains(item3));
    }

    @Test
    public void expireHistoryItemsOlderThanShouldNotDeleteEntitiesYoungerThan30Days()
    {
        Long testedTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can't delete user history that is not at least 30 days old");
        store.removeHistoryOlderThan(testedTimestamp);
    }

    @Test
    public void expireHistoryItemsOlderThanShouldDeleteEntitiesOlderThan30Days()
    {
        Long youngTimestamp1 = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29);
        Long youngTimestamp2 = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(31);
        Long oldTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40);

        UserHistoryItem item1 = new UserHistoryItem(ISSUE, "12", youngTimestamp1);
        UserHistoryItem item2 = new UserHistoryItem(ISSUE, "123", youngTimestamp2);
        UserHistoryItem item3 = new UserHistoryItem(ISSUE, "1234", oldTimestamp);

        List<UserHistoryItem> results = store.getHistory(ISSUE, user);
        assertThat(results, noHistory());

        store.addHistoryItem(user, item1);
        store.addHistoryItem(user, item2);
        store.addHistoryItem(user, item3);

        List<UserHistoryItem> addedResults = store.getHistory(ISSUE, user);
        assertThat(addedResults, containsInAnyOrder(item1, item2, item3));

        Long testedTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(35);
        store.removeHistoryOlderThan(testedTimestamp);

        List<UserHistoryItem> removeResults = store.getHistory(ISSUE, user);
        assertThat(removeResults, containsInAnyOrder(item1, item2));
    }

    static Matcher<Iterable<UserHistoryItem>> noHistory()
    {
        return Matchers.emptyIterable();
    }

    static Matcher<Iterable<UserHistoryItem.Type>> noTypes()
    {
        return Matchers.emptyIterable();
    }
}
