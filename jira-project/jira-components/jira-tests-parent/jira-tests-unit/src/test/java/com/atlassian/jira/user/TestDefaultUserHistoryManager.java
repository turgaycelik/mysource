package com.atlassian.jira.user;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import net.jcip.annotations.ThreadSafe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Default implementation of the UserHistoryManager.
 *
 * @since v4.0
 */
@ThreadSafe
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultUserHistoryManager
{

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock
    private UserHistoryStore delegateStore;



    private UserHistoryManager historyManager;
    private ApplicationUser user;


    @Before
    public void setUp() throws Exception
    {
        user = new DelegatingApplicationUser("admin", new MockUser("admin"));
        historyManager = new DefaultUserHistoryManager(delegateStore);
    }

    @After
    public void tearDown() throws Exception
    {
        historyManager = null;
    }

    @Test
    public void testNullArgs()
    {
        try
        {
            historyManager.addItemToHistory(null, user.getDirectoryUser(), "123");
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
        try
        {
            historyManager.addItemToHistory(UserHistoryItem.ISSUE, user.getDirectoryUser(), null);
            fail("entity can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.hasHistory(null, user.getDirectoryUser());
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.getHistory(null, user.getDirectoryUser());
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.removeHistoryForUser((User) null);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
    }

    @Test
    public void testNullArgsWithAppUser()
    {
        try
        {
            historyManager.addItemToHistory(null, user, "123");
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
        try
        {
            historyManager.addItemToHistory(UserHistoryItem.ISSUE, user, null);
            fail("entity can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.hasHistory(null, user);
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.getHistory(null, user);
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.removeHistoryForUser((ApplicationUser) null);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
    }

    @Test
    public void testAddUserToHistory()
    {
        MockUserKeyService mockUserKeyService = new MockUserKeyService();
        mockUserKeyService.setMapping("KeyOTHER", "OTHER");
        container.addMock(UserKeyService.class, mockUserKeyService);

        final User entity = new MockUser("OTHER");
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "KeyOTHER");

        historyManager.addUserToHistory(UserHistoryItem.ISSUE, user.getDirectoryUser(), entity);
        verify(delegateStore).addHistoryItem(eq(user), eqHistoryItem(item));
    }

    @Test
    public void testAddApplicationUserToHistory()
    {
        final ApplicationUser entity = new DelegatingApplicationUser("KeyOTHER", new MockUser("OTHER"));
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "KeyOTHER");

        historyManager.addUserToHistory(UserHistoryItem.ISSUE, user, entity);

        verify(delegateStore).addHistoryItem(eq(user), eqHistoryItem(item));
    }

    @Test
    public void testAddItemToHistory()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        historyManager.addItemToHistory(UserHistoryItem.ISSUE, user.getDirectoryUser(), "123");

        verify(delegateStore).addHistoryItem(eq(user), eqHistoryItem(item));
    }


    @Test
    public void testApplicationUserAddsItemToHistory()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        historyManager.addItemToHistory(UserHistoryItem.ISSUE, user, "123");

        verify(delegateStore).addHistoryItem(eq(user), eqHistoryItem(item));
    }


    @Test
    public void testHasAndGetHistory()
    {
        final List<UserHistoryItem> history = ImmutableList.of(
            new UserHistoryItem(UserHistoryItem.ISSUE, "123"),
            new UserHistoryItem(UserHistoryItem.ISSUE, "1234"),
            new UserHistoryItem(UserHistoryItem.ISSUE, "1235"),
            new UserHistoryItem(UserHistoryItem.ISSUE, "1236")
        );
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(history);

        assertTrue(historyManager.hasHistory(UserHistoryItem.ISSUE, user.getDirectoryUser()));
        assertSame(history, historyManager.getHistory(UserHistoryItem.ISSUE, user.getDirectoryUser()));

    }

    @Test
    public void testAppUserHasAndGetHistory()
    {
        final List<UserHistoryItem> history = ImmutableList.of(
            new UserHistoryItem(UserHistoryItem.ISSUE, "123"),
            new UserHistoryItem(UserHistoryItem.ISSUE, "1234"),
            new UserHistoryItem(UserHistoryItem.ISSUE, "1235"),
            new UserHistoryItem(UserHistoryItem.ISSUE, "1236")
        );
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(history);

        assertTrue(historyManager.hasHistory(UserHistoryItem.ISSUE, user));
        assertSame(history, historyManager.getHistory(UserHistoryItem.ISSUE, user));
    }

    @Test
    public void testHasAndGetHistoryEmptyHistory()
    {
        final List<UserHistoryItem> history = ImmutableList.of();
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(history);

        assertFalse(historyManager.hasHistory(UserHistoryItem.ISSUE, user.getDirectoryUser()));
        assertSame(history, historyManager.getHistory(UserHistoryItem.ISSUE, user.getDirectoryUser()));
    }

    @Test
    public void testHasAndGetHistoryEmptyHistoryAppUser()
    {
        final List<UserHistoryItem> history = ImmutableList.of();
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(history);

        assertFalse(historyManager.hasHistory(UserHistoryItem.ISSUE, user));
        assertSame(history, historyManager.getHistory(UserHistoryItem.ISSUE, user));
    }

    @Test
    public void testHasAndGetHistoryNoHistory()
    {
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(null);

        assertFalse(historyManager.hasHistory(UserHistoryItem.ISSUE, user.getDirectoryUser()));
        assertNull(historyManager.getHistory(UserHistoryItem.ISSUE, user.getDirectoryUser()));
    }

    @Test
    public void testHasAndGetHistoryNoHistoryAppUser()
    {
        when(delegateStore.getHistory(UserHistoryItem.ISSUE, user)).thenReturn(null);

        assertFalse(historyManager.hasHistory(UserHistoryItem.ISSUE, user));
        assertNull(historyManager.getHistory(UserHistoryItem.ISSUE, user));
    }


    @Test
    public void testRemoveHistoryForUser()
    {
        final Set<UserHistoryItem.Type> typesRemoved = Collections.emptySet();
        when(delegateStore.removeHistoryForUser(user)).thenReturn(typesRemoved);

        historyManager.removeHistoryForUser(user.getDirectoryUser());

        verify(delegateStore).removeHistoryForUser(user);
    }

    @Test
    public void testRemoveHistoryForApplicationUser()
    {
        final Set<UserHistoryItem.Type> typesRemoved = Collections.emptySet();
        when(delegateStore.removeHistoryForUser(user)).thenReturn(typesRemoved);

        historyManager.removeHistoryForUser(user);

        verify(delegateStore).removeHistoryForUser(user);
    }

    /**
     * Argument matcher for a UserHistoryItem that ignores the timestamp field
     * to keep tests from being flakey due to race conditions.
     *
     * @param expectedItem the expected value
     * @return argument matcher with type UserHistoryItem
     */
    private UserHistoryItem eqHistoryItem(final UserHistoryItem expectedItem)
    {
        return argThat(new ArgumentMatcher<UserHistoryItem>()
        {
            @Override
            public boolean matches(Object obj)
            {
                UserHistoryItem historyItem = (UserHistoryItem) obj;
                return Objects.equal(historyItem.getType(), expectedItem.getType())
                        && Objects.equal(historyItem.getEntityId(), expectedItem.getEntityId());
            }
        });
    }

}
