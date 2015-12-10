package com.atlassian.jira.user;

import java.util.List;
import java.util.Set;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @since v4.0
 */
public class TestSessionBasedAnonymousUserHistoryStore extends MockControllerTestCase
{
    private SessionBasedAnonymousUserHistoryStore store;
    private UserHistoryStore delegateStore;
    private ApplicationProperties applicationProperties;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private VelocityRequestContext velocityRequestContext;
    private VelocityRequestSession session;
    private ApplicationUser user;
    private UserManager userManager;

    @Before
    public void setUp() throws Exception
    {
        user = new DelegatingApplicationUser("admin", new MockUser("admin"));
        delegateStore = mockController.getMock(OfBizUserHistoryStore.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
        velocityRequestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        velocityRequestContext = mockController.getMock(VelocityRequestContext.class);
        session = mockController.getMock(VelocityRequestSession.class);
        userManager = new MockUserManager();

        store = new SessionBasedAnonymousUserHistoryStore(delegateStore, applicationProperties, userManager, velocityRequestContextFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        delegateStore = null;
        user = null;
        store = null;
        applicationProperties = null;
        velocityRequestContext = null;
        session = null;
        velocityRequestContextFactory = null;
    }

    @Test
    public void testNullValues()
    {
        mockController.replay();

        try
        {
            store.addHistoryItem(user, null);
            fail("history can not be null");
        }
        catch (final IllegalArgumentException e)
        {
            //pass
        }
        try
        {
            store.getHistory(null, user);
            fail("type can not be null");
        }
        catch (final IllegalArgumentException e)
        {
            //pass
        }
        mockController.verify();

    }

    @Test
    public void testAddHistoryNoSessionNoUser()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(null);

        mockController.replay();

        store.addHistoryItem(null, item);

        mockController.verify();
    }

    @Test
    public void testAddHistoryNoSession()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(null);

        delegateStore.addHistoryItem(user, item);

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();

    }

    @Test
    public void testAddHistoryNoUserNoHistory()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(null);

        session.setAttribute(sessionKey, CollectionBuilder.newBuilder(item).asList());

        mockController.replay();

        store.addHistoryItem(null, item);

        mockController.verify();

    }

    @Test
    public void testAddHistoryNoHistory()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(null);

        delegateStore.addHistoryItem(user, item);

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();

    }

    @Test
    public void testAddHistoryEmptyHistory()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        delegateStore.addHistoryItem(user, item);

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();

    }

    @Test
    public void testAddNewHistoryNoUser()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item2, item3, item4).asMutableList();
        final List<UserHistoryItem> expectedHistory = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("10");

        mockController.replay();

        store.addHistoryItem(null, item);

        assertEquals(expectedHistory, history);

        mockController.verify();
    }

    @Test
    public void testAddNewHistoryNoUserExpireOld()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item2, item3, item4).asMutableList();
        final List<UserHistoryItem> expectedHistory = CollectionBuilder.newBuilder(item, item2).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        mockController.replay();

        store.addHistoryItem(null, item);

        assertEquals(expectedHistory, history);

        mockController.verify();
    }

    @Test
    public void testAddExistingHistory()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem newItem = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 3);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item2, item3, item, item4).asMutableList();
        final List<UserHistoryItem> expectedHistory = CollectionBuilder.newBuilder(newItem, item2, item3, item4).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        mockController.replay();

        // even though ther are more items, we don't expire as we are only moving an existing item
        store.addHistoryItem(null, newItem);

        assertEquals(expectedHistory, history);

        mockController.verify();
    }

    @Test
    public void testAddHistoryUserJustLoggedIn()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item2, item3, item4).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        delegateStore.addHistoryItem(user, item4);
        delegateStore.addHistoryItem(user, item3);
        delegateStore.addHistoryItem(user, item2);

        session.removeAttribute(sessionKey);

        delegateStore.addHistoryItem(user, item);

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();
    }

    @Test
    public void testGetHistoryNoSessionNoUser()
    {
        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(store.getHistory(UserHistoryItem.ISSUE, (ApplicationUser) null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetHistoryNoSession()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(null);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();

    }

    @Test
    public void testGetHistoryNoUserNoHistory()
    {
        final List<UserHistoryItem> history = CollectionBuilder.<UserHistoryItem> newBuilder().asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(null);

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, (ApplicationUser) null));

        mockController.verify();
    }

    @Test
    public void testGetHistoryNoUserEmptyHistory()
    {
        final List<UserHistoryItem> history = CollectionBuilder.<UserHistoryItem> newBuilder().asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, (ApplicationUser) null));

        mockController.verify();
    }

    @Test
    public void testGetHistoryNoUser()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, (ApplicationUser) null));

        mockController.verify();
    }

    @Test
    public void testGetHistoryNoHistoryInSession()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(null);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testGetHistoryEmptyHistoryInSession()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        final List<UserHistoryItem> emptyHistory = CollectionBuilder.<UserHistoryItem> newBuilder().asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(emptyHistory);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testGetHistoryUserJustLoggedIn()
    {
        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item2, item3, item4).asMutableList();
        final List<UserHistoryItem> newHistory = CollectionBuilder.newBuilder(item2, item3, item4, item).asMutableList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setReturnValue(history);

        delegateStore.addHistoryItem(user, item4);
        delegateStore.addHistoryItem(user, item3);
        delegateStore.addHistoryItem(user, item2);

        session.removeAttribute(sessionKey);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(newHistory);

        mockController.replay();

        assertEquals(newHistory, store.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testGetHistoryErrorThrown()
    {
        final List<UserHistoryItem> history = emptyList();

        velocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(velocityRequestContext);

        velocityRequestContext.getSession();
        mockController.setReturnValue(session);

        session.getId();
        mockController.setDefaultReturnValue("sessionId");

        final String sessionKey = SessionBasedAnonymousUserHistoryStore.SESSION_PREFIX + UserHistoryItem.ISSUE.getName();
        session.getAttribute(sessionKey);
        mockController.setThrowable(new IllegalArgumentException());

        mockController.replay();

        assertEquals(history, store.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testRemoveHistoryNullUser()
    {
        final Set<UserHistoryItem.Type> typesRemoved = CollectionBuilder.<UserHistoryItem.Type> newBuilder().asSet();

        mockController.replay();

        assertEquals(typesRemoved, store.removeHistoryForUser(null));

        mockController.verify();

    }

    @Test
    public void testRemoveHistory()
    {
        final Set<UserHistoryItem.Type> typesRemoved = CollectionBuilder.<UserHistoryItem.Type> newBuilder(UserHistoryItem.ISSUE,
            UserHistoryItem.PROJECT).asSet();

        delegateStore.removeHistoryForUser(user);
        mockController.setReturnValue(typesRemoved);

        mockController.replay();

        assertEquals(typesRemoved, store.removeHistoryForUser(user));

        mockController.verify();

    }

}
