package com.atlassian.jira.user;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;

import net.jcip.annotations.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the UserHistoryManager.
 *
 * @since v4.0
 */
@ThreadSafe
public class DefaultUserHistoryManager implements UserHistoryManager
{
    private final UserHistoryStore store;

    public DefaultUserHistoryManager(@Nonnull final UserHistoryStore store)
    {
        this.store = notNull("store", store);
    }

    @Override
    public void addUserToHistory(UserHistoryItem.Type type, User user, User entity)
    {
        notNull("entity", entity);
        addItemToHistory(type, user, ApplicationUsers.getKeyFor(entity), null);
    }

    @Override
    public void addUserToHistory(UserHistoryItem.Type type, ApplicationUser user, ApplicationUser entity)
    {
        notNull("entity", entity);
        addItemToHistory(type, user, entity.getKey(), null);
    }

    public void addItemToHistory(@Nonnull final UserHistoryItem.Type type, @Nonnull final User user, @Nonnull final String entityId)
    {
        addItemToHistory(type, user, entityId, null);
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, ApplicationUser user, String entityId)
    {
        addItemToHistory(type, user, entityId, null);
    }

    public void addItemToHistory(@Nonnull final UserHistoryItem.Type type, @Nullable final User user, @Nonnull final String entityId, @Nullable final String data)
    {
        notNull("type", type);
        notNull("entityId", entityId);
        store.addHistoryItem(ApplicationUsers.from(user), new UserHistoryItem(type, entityId, data));
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, ApplicationUser user, String entityId, String data)
    {
        notNull("type", type);
        notNull("entityId", entityId);
        store.addHistoryItem(user, new UserHistoryItem(type, entityId, data));
    }

    public boolean hasHistory(@Nonnull final UserHistoryItem.Type type, @Nonnull final User user)
    {
        notNull("type", type);
        notNull("user", user);
        final List<UserHistoryItem> history = store.getHistory(type, ApplicationUsers.from(user));
        return (history != null) && !history.isEmpty();
    }

    @Override
    public boolean hasHistory(UserHistoryItem.Type type, ApplicationUser user)
    {
        notNull("type", type);
        notNull("user", user);
        final List<UserHistoryItem> history = store.getHistory(type, user);
        return (history != null) && !history.isEmpty();
    }

    @Nonnull
    public List<UserHistoryItem> getHistory(@Nonnull final UserHistoryItem.Type type, @Nullable final User user)
    {
        notNull("type", type);
        return store.getHistory(type, ApplicationUsers.from(user));
    }

    @Override
    public List<UserHistoryItem> getHistory(UserHistoryItem.Type type, ApplicationUser user)
    {
        notNull("type", type);
        return store.getHistory(type, user);
    }

    public void removeHistoryForUser(@Nonnull final User user)
    {
        notNull("user", user);
        store.removeHistoryForUser(ApplicationUsers.from(user));
    }

    @Override
    public void removeHistoryForUser(@Nonnull ApplicationUser user)
    {
        notNull("user", user);
        store.removeHistoryForUser(user);
    }

}
