package com.atlassian.jira.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.cache.CacheException;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingUserHistoryStore
{
    private CachingUserHistoryStore store;

    @Mock
    private OfBizUserHistoryStore delegateStore;

    @Mock
    private ApplicationProperties applicationProperties;
    private ApplicationUser user;
    private ApplicationUser user2;
    private MemoryCacheManager cacheManager;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");
        user2 = new MockApplicationUser("test");
        cacheManager = new MemoryCacheManager();
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, cacheManager);
    }

    @After
    public void tearDown() throws Exception
    {
        delegateStore = null;
        user = null;
        user2 = null;
        store = null;
        applicationProperties = null;
    }


    @Test
    @SuppressWarnings("all")
    public void testHistoryNullParams()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
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
            store.getHistory(UserHistoryItem.ISSUE, (ApplicationUser) null);
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
    public void testAddUserHistoryNullHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");
        store.addHistoryItem(user, item);
        verify(delegateStore).addHistoryItemNoChecks(user, item);
    }

    @Test
    public void testAddUserHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem itemNew = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");

        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(new ArrayList<UserHistoryItem>());
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");

        store.addHistoryItem(user, item);
        store.addHistoryItem(user, item2);
        store.addHistoryItem(user, itemNew);

        verify(delegateStore).addHistoryItemNoChecks(user, item);
        verify(delegateStore).addHistoryItemNoChecks(user, item2);
        verify(delegateStore).updateHistoryItemNoChecks(user, itemNew);
    }

    @Test
    public void testAddUserHistoryExpireOldOnes()
    {
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, cacheManager, 0);

        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(newArrayList(item2, item3, item4));
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");

        store.addHistoryItem(user, item);
        final List<UserHistoryItem> resultList = store.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> expectedList = newArrayList(item, item2);

        assertEquals(expectedList, resultList);

        verify(delegateStore).addHistoryItemNoChecks(user, item);
        verify(delegateStore).expireOldHistoryItems(user, UserHistoryItem.ISSUE, newArrayList("1235", "1236"));
    }

    @Test
    public void testAddUserHistoryExpireOldOnesWitThreshold()
    {
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, cacheManager, 2);

        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(newArrayList(item2, item3, item4));
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");

        store.addHistoryItem(user, item);

        verify(delegateStore).addHistoryItemNoChecks(user, item);
    }

    @Test
    public void testAddUserHistoryNoExpireAsReplacing()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(newArrayList(item, item2, item3, item4));
        store.addHistoryItem(user, item);

        verify(delegateStore).updateHistoryItemNoChecks(user, item);
    }

    @Test public void testAddUserHistoryCacheOutOfSyncRemovePasses()
    {
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, cacheManager, 0);
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("1");

        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey()))
                .thenReturn(newArrayList(item2, item3, item4));
        doThrow(new DataAccessException("Error")).doNothing().when(delegateStore).addHistoryItemNoChecks(user, item);
        when(delegateStore.removeHistoryItem(user, item)).thenReturn(true);

        store.addHistoryItem(user, item);
        assertEquals(Collections.singletonList(item), store.getHistory(UserHistoryItem.ISSUE, user));

        verify(delegateStore, times(1)).getHistory(UserHistoryItem.ISSUE, user.getKey());
        verify(delegateStore, times(2)).addHistoryItemNoChecks(user, item);
    }

    @Test public void testAddUserHistoryCacheOutOfSyncRemoveFails()
    {
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, cacheManager, 0);
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("1");

        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final ArrayList<UserHistoryItem> expectedItems = newArrayList(item2, item3, item4);
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey()))
                .thenReturn(expectedItems);
        doThrow(new DataAccessException("Error")).when(delegateStore).addHistoryItemNoChecks(user, item);
        when(delegateStore.removeHistoryItem(user, item)).thenReturn(false);

        store.addHistoryItem(user, item);

        assertEquals(expectedItems, store.getHistory(UserHistoryItem.ISSUE, user));

        verify(delegateStore, times(2)).getHistory(UserHistoryItem.ISSUE, user.getKey());
        verify(delegateStore, times(1)).addHistoryItemNoChecks(user, item);
    }

    @Test
    public void testGetHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final ArrayList<UserHistoryItem> expectedItems = newArrayList(item, item2, item3, item4);
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(expectedItems);

        List<UserHistoryItem> actualItems = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(4, actualItems.size());
        assertEquals(expectedItems, actualItems);
    }

    @Test
    public void testGetHistoryWithError()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final ArrayList<UserHistoryItem> expectedItems = newArrayList(item, item2, item3, item4);
        final RuntimeException expectedException = new RuntimeException("Random");
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey()))
                .thenThrow(expectedException)
                .thenThrow(new DataAccessException("Something"))
                .thenReturn(expectedItems);

        try
        {
            store.getHistory(UserHistoryItem.ISSUE, user);
            fail("Expect the fail on random error.");
        }
        catch (CacheException e)
        {
            //fine.
        }

        assertTrue(store.getHistory(UserHistoryItem.ISSUE, user).isEmpty());
        assertEquals(expectedItems, store.getHistory(UserHistoryItem.ISSUE, user));
    }

    @Test
    public void testGetHistoryAfterAdd()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> expectedItmes = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(expectedItmes);

        store.addHistoryItem(user, item);

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(4, history.size());
        assertEquals(expectedItmes, history);

        verify(delegateStore).updateHistoryItemNoChecks(user, item);
    }

    @Test
    public void testGetHistoryOrderOfAdds()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> list = newArrayList(item3, item4);
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(list);
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(list, history);

        store.addHistoryItem(user, item2);

        history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(newArrayList(item2, item3, item4), history);

        store.addHistoryItem(user, item);
        store.addHistoryItem(user, item2);

        history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(newArrayList(item2, item, item3, item4), history);

        verify(delegateStore).addHistoryItemNoChecks(user, item2);
        verify(delegateStore).addHistoryItemNoChecks(user, item);
        verify(delegateStore).updateHistoryItemNoChecks(user, item2);
    }

    @Test
    public void testGetHistoryDifferentTypes()
    {
        UserHistoryItem issueItem = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem issueItem2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 1);

        UserHistoryItem projectItem = new UserHistoryItem(UserHistoryItem.PROJECT, "123", 1);
        UserHistoryItem projectItem2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234", 1);

        final List<UserHistoryItem> issueList = newArrayList(issueItem, issueItem2);
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(issueList);

        final List<UserHistoryItem> projectList = newArrayList(projectItem, projectItem2);
        when(delegateStore.getHistory(UserHistoryItem.PROJECT, user.getKey())).thenReturn(projectList);

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);
    }

    @Test
    public void testRemoveHistory()
    {
        UserHistoryItem issueItem = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem issueItem2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 1);

        UserHistoryItem projectItem = new UserHistoryItem(UserHistoryItem.PROJECT, "123", 1);
        UserHistoryItem projectItem2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234", 1);

        final List<UserHistoryItem> issueList = CollectionBuilder.newBuilder(issueItem, issueItem2).asMutableList();
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user.getKey())).thenReturn(issueList);


        final List<UserHistoryItem> user2issueList = CollectionBuilder.newBuilder(issueItem, issueItem2).asMutableList();
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user2.getKey())).thenReturn(user2issueList);

        final List<UserHistoryItem> projectList = CollectionBuilder.newBuilder(projectItem, projectItem2).asMutableList();
        when(delegateStore.getHistory(UserHistoryItem.PROJECT, user.getKey())).thenReturn(projectList);

        final Set<UserHistoryItem.Type> types = CollectionBuilder.newBuilder(UserHistoryItem.ISSUE, UserHistoryItem.PROJECT).asSet();
        when(delegateStore.removeHistoryForUser(user)).thenReturn(types);

        // it should hit the db again

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user2);
        assertEquals(user2issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        store.removeHistoryForUser(user);

        // this wont hit the db
        history = store.getHistory(UserHistoryItem.ISSUE, user2);
        assertEquals(user2issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        verify(delegateStore, times(2)).getHistory(UserHistoryItem.PROJECT, user.getKey());
        verify(delegateStore, times(2)).getHistory(UserHistoryItem.ISSUE, user.getKey());
    }

    @Test
    public void testMaxEntries()
    {
        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn(null);

        assertEquals(2, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(CachingUserHistoryStore.DEFAULT_MAX_ITEMS, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn("");

        assertEquals(2, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(CachingUserHistoryStore.DEFAULT_MAX_ITEMS, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("2");
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn("3");

        assertEquals(2, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("bad");
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn("3");

        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn(null);
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn("3");

        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("");
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn("3");

        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(3, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));

        when(applicationProperties.getDefaultBackedString("jira.max.Issue.history.items")).thenReturn("");
        when(applicationProperties.getDefaultBackedString("jira.max.history.items")).thenReturn("");

        assertEquals(CachingUserHistoryStore.DEFAULT_MAX_ITEMS, CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUE, applicationProperties));
        assertEquals(CachingUserHistoryStore.DEFAULT_MAX_ITEMS, CachingUserHistoryStore.getMaxItems(UserHistoryItem.PROJECT, applicationProperties));
    }

    @Test
    public void cacheKeyShouldNotEqualInstanceOfOtherClass()
    {
        // Set up
        final CachingUserHistoryStore.Key key = getKeyUnderTest();

        // Invoke and check
        assertFalse("Key should not be equal to a java.lang.Object", key.equals(new Object()));
    }

    @Test
    public void cacheKeyShouldEqualItself()
    {
        // Set up
        final CachingUserHistoryStore.Key key = getKeyUnderTest();

        // Invoke and check
        assertEquals(key, key);
    }

    private CachingUserHistoryStore.Key getKeyUnderTest()
    {// Set up
        final UserHistoryItem.Type mockType = mock(UserHistoryItem.Type.class);
        return new CachingUserHistoryStore.Key("", mockType);
    }
}
