package com.atlassian.jira.user;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import javax.annotation.Nonnull;

import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;
import com.google.common.collect.ImmutableList;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Session based caching implementation of {@link UserHistoryStore}.  Allows anonymous users to have history.
 *
 * @since v4.0
 */
public class SessionBasedAnonymousUserHistoryStore implements UserHistoryStore
{
    private static final int DEFAULT_MAX_ITEMS = 20;
    private static final Logger log = Logger.getLogger(SessionBasedAnonymousUserHistoryStore.class);

    /**
     * Lock on the sessionID.
     */
    @ClusterSafe("Locks user sessions, which are node-specific thanks to session affinity")
    private final Function<VelocityRequestSession, ManagedLock> lockManager = ManagedLocks.weakManagedLockFactory(new Function<VelocityRequestSession, String>()
    {
        public String get(final VelocityRequestSession input)
        {
            return input.getId();
        }
    });

    private final UserHistoryStore delegatingStore;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    static final String SESSION_PREFIX = "history-";

    // Rather than generating tens of thousands of these dynamically we just reuse a few and save the odd few Megabytes of memory.
    final private static Map<String, String> KNOWN_KEYS;
    static
    {
        Map<String, String> keys = new HashMap<String, String>();
        keys.put(UserHistoryItem.ISSUE.getName(), SESSION_PREFIX + UserHistoryItem.ISSUE.getName());
        keys.put(UserHistoryItem.PROJECT.getName(), SESSION_PREFIX + UserHistoryItem.PROJECT.getName());
        keys.put(UserHistoryItem.JQL_QUERY.getName(), SESSION_PREFIX + UserHistoryItem.JQL_QUERY.getName());
        keys.put(UserHistoryItem.ADMIN_PAGE.getName(), SESSION_PREFIX + UserHistoryItem.ADMIN_PAGE.getName());
        keys.put(UserHistoryItem.ASSIGNEE.getName(), SESSION_PREFIX + UserHistoryItem.ASSIGNEE.getName());
        keys.put(UserHistoryItem.USED_USER.getName(), SESSION_PREFIX + UserHistoryItem.USED_USER.getName());
        keys.put(UserHistoryItem.DASHBOARD.getName(), SESSION_PREFIX + UserHistoryItem.DASHBOARD.getName());
        keys.put(UserHistoryItem.ISSUELINKTYPE.getName(), SESSION_PREFIX + UserHistoryItem.ISSUELINKTYPE.getName());
        keys.put(UserHistoryItem.RESOLUTION.getName(), SESSION_PREFIX + UserHistoryItem.RESOLUTION.getName());
        KNOWN_KEYS = Collections.unmodifiableMap(keys);
    }



    public SessionBasedAnonymousUserHistoryStore(final UserHistoryStore delegatingStore, final ApplicationProperties applicationProperties, final UserManager userManager, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.delegatingStore = delegatingStore;
        this.applicationProperties = applicationProperties;
        this.userManager = userManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }



    public void addHistoryItem(final ApplicationUser user, @Nonnull final UserHistoryItem historyItem)
    {
        notNull("historyItem", historyItem);

        if (user != null)
        {
            moveAnonymousSessionLeftoversToUserStore(user, historyItem.getType());
            // JRADEV-15768
            // Don't call the delegatingStore while the lock is held!  Was causing deadlock between DB and JIRA.
            delegatingStore.addHistoryItem(user, historyItem);
            return;
        }

        final VelocityRequestSession session = getSession();
        if (session != null)
        {
            lockManager.get(session).withLock(new Runnable()
            {
                public void run()
                {
                    addAnonymousSessionHistoryItem(session, historyItem);
                }
            });
        }
    }

    @GuardedBy("lockManager.get(session)")
    void addAnonymousSessionHistoryItem(@Nonnull final VelocityRequestSession session, @Nonnull final UserHistoryItem historyItem)
    {
        final String sessionKey = getSessionKeyForType(historyItem.getType());
        @SuppressWarnings("unchecked")
        final List<UserHistoryItem> sessionHistory = (List<UserHistoryItem>) session.getAttribute(sessionKey);

        // Anonymous user with no history; create a new one
        if (sessionHistory == null)
        {
            final ArrayList<UserHistoryItem> newHistory = new ArrayList<UserHistoryItem>();
            newHistory.add(historyItem);
            session.setAttribute(sessionKey, newHistory);
            return;
        }

        // Existing history already had this item; list did not grow
        if (removeAnonymousSessionHistoryItem(sessionHistory, historyItem))
        {
            sessionHistory.add(0, historyItem);
            return;
        }

        // New item; keep the list bounded
        sessionHistory.add(0, historyItem);
        final int maxItems = getMaxItems(historyItem.getType());
        while (sessionHistory.size() > maxItems)
        {
            sessionHistory.remove(sessionHistory.size() - 1);
        }
    }

    /**
     * Looks for a history item in the existing history.  If it finds it, then the item
     * is removed, presumably so that it can be added back at the beginning of the list
     * instead.
     *
     * @param history the history list
     * @param historyItem the item to remove
     * @return {@code true} if it was found and removed; {@code false} otherwise
     */
    @GuardedBy("lockManager.get(session)")
    private boolean removeAnonymousSessionHistoryItem(@Nonnull final List<UserHistoryItem> history, @Nonnull final UserHistoryItem historyItem)
    {
        for (int i = 0; i < history.size(); i++)
        {
            final UserHistoryItem currentHistoryItem = history.get(i);
            if (currentHistoryItem.getEntityId().equals(historyItem.getEntityId()))
            {
                history.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Called whenever we have a known user to check for an existing session holding its own
     * history.  This can happen if the user was browsing anonymously (so got history saved to
     * the session) but has now logged in.  We want to move those items from the session to
     * its proper home in the delegating store.
     *
     * @param user the current user
     * @param type the type of user history items to check for
     */
    private void moveAnonymousSessionLeftoversToUserStore(@Nonnull final ApplicationUser user, UserHistoryItem.Type type)
    {
        final VelocityRequestSession session = getSession();
        if (session != null)
        {
            final List<UserHistoryItem> removed = removeAnonymousSession(session, type);
            if (removed != null)
            {
                copyAnonymousSessionItemsToStore(user, removed);
            }
        }
    }

    // TODO: This could be handled more efficiently if UserHistoryStore had a bulk add...
    private void copyAnonymousSessionItemsToStore(final ApplicationUser user, final List<UserHistoryItem> sessionItems)
    {
        for (int i = sessionItems.size(); i > 0; i--)
        {
            final UserHistoryItem userHistoryItem = sessionItems.get(i - 1);
            delegatingStore.addHistoryItem(user, userHistoryItem);
        }
    }

    private List<UserHistoryItem> removeAnonymousSession(final VelocityRequestSession session, final UserHistoryItem.Type type)
    {
        final String sessionKey = getSessionKeyForType(type);
        return lockManager.get(session).withLock(new Supplier<List<UserHistoryItem>>()
        {
            public List<UserHistoryItem> get()
            {
                @SuppressWarnings("unchecked")
                final List<UserHistoryItem> sessionHistory = (List<UserHistoryItem>) session.getAttribute(sessionKey);
                if (sessionHistory != null && !sessionHistory.isEmpty())
                {
                    session.removeAttribute(sessionKey);
                }
                return sessionHistory;
            }
        });
    }

    @Nonnull
    @Override
    public List<UserHistoryItem> getHistory(@Nonnull final UserHistoryItem.Type type, final String userKey)
    {
        return getHistory(type, userKey == null ? null : userManager.getUserByKey(userKey));
    }

    @Nonnull
    public List<UserHistoryItem> getHistory(@Nonnull final UserHistoryItem.Type type, final ApplicationUser user)
    {
        notNull("type", type);

        try
        {
            if (user != null)
            {
                moveAnonymousSessionLeftoversToUserStore(user, type);
                return delegatingStore.getHistory(type, user);
            }

            final VelocityRequestSession session = getSession();
            if (session == null)
            {
                // can't do anything for this poor fella
                return Collections.emptyList();
            }
            return getAnonymousSessionHistory(session, type);
        }
        catch (final RuntimeException e)
        {
            log.error("Exception thrown while retrieving UserHistoryItems.", e);
        }
        return Collections.emptyList();
    }

    private List<UserHistoryItem> getAnonymousSessionHistory(final VelocityRequestSession session, UserHistoryItem.Type type)
    {
        final String sessionKey = getSessionKeyForType(type);
        return lockManager.get(session).withLock(new Supplier<List<UserHistoryItem>>()
        {
            public List<UserHistoryItem> get()
            {
                @SuppressWarnings("unchecked")
                final List<UserHistoryItem> sessionHistory = (List<UserHistoryItem>) session.getAttribute(sessionKey);
                if (sessionHistory == null)
                {
                    return Collections.emptyList();
                }
                // Don't allow our not-thread-safe, mutable list to escape!
                return ImmutableList.copyOf(sessionHistory);
            }
        });
    }

    public Set<UserHistoryItem.Type> removeHistoryForUser(@Nonnull final ApplicationUser user)
    {
        // The session is most probably not related to the User being passed in, so we will not remove it.  We let the
        // session die a natural death.

        if (user == null)
        {
            // can't do anything for this poor fella
            return Collections.emptySet();
        }
        return delegatingStore.removeHistoryForUser(user);
    }

    @Override
    public void removeHistoryOlderThan(@Nonnull final Long timestamp)
    {
        delegatingStore.removeHistoryOlderThan(timestamp);
    }

    private VelocityRequestSession getSession()
    {
        final VelocityRequestSession session = velocityRequestContextFactory.getJiraVelocityRequestContext().getSession();
        if (session == null || session.getId() == null)
        {
            return null;
        }
        return session;
    }

    private String getSessionKeyForType(UserHistoryItem.Type type)
    {
        String key = KNOWN_KEYS.get(type.getName());
        if (key != null)
        {
            return key;
        }
        return SESSION_PREFIX + type.getName();
    }

    private int getMaxItems(final UserHistoryItem.Type type)
    {
        final String maxItemsForTypeStr = applicationProperties.getDefaultBackedString("jira.max." + type.getName() + ".history.items");
        final int maxItems = DEFAULT_MAX_ITEMS;
        try
        {
            if (StringUtils.isNotBlank(maxItemsForTypeStr))
            {
                return Integer.parseInt(maxItemsForTypeStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
        }

        final String maxItemsStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_HISTORY_ITEMS);
        try
        {
            if (StringUtils.isNotBlank(maxItemsStr))
            {
                return Integer.parseInt(maxItemsStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
        }
        return maxItems;
    }

}