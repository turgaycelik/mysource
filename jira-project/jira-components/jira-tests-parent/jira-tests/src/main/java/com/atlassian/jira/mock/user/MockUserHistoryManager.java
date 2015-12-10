package com.atlassian.jira.mock.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @since v4.4
 */
public class MockUserHistoryManager implements UserHistoryManager
{

    private List<UserHistoryItem> userHistoryItems;
    private List<String> addedUsers;

    public MockUserHistoryManager()
    {
        this(Collections.<UserHistoryItem>emptyList());
    }
    public MockUserHistoryManager(List<UserHistoryItem> userHistoryItems)
    {
        this.userHistoryItems = userHistoryItems;
        this.addedUsers = new LinkedList<String>();
    }

    @Override
    public void addUserToHistory(UserHistoryItem.Type type, User user, User entity)
    {
        addedUsers.add(entity.getName());
    }

    @Override
    public void addUserToHistory(UserHistoryItem.Type type, ApplicationUser user, ApplicationUser entity)
    {
        addedUsers.add(entity.getUsername());
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, User user, String entityId)
    {
        addedUsers.add(entityId);
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, ApplicationUser user, String entityId)
    {
        addedUsers.add(entityId);
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, User user, String entityId, String data)
    {
        addedUsers.add(entityId);
    }

    @Override
    public void addItemToHistory(UserHistoryItem.Type type, ApplicationUser user, String entityId, String data)
    {
        addedUsers.add(entityId);
    }

    @Override
    public boolean hasHistory(UserHistoryItem.Type type, User user)
    {
        return false;
    }

    @Override
    public boolean hasHistory(UserHistoryItem.Type type, ApplicationUser user)
    {
        return false;
    }

    @Override
    public List<UserHistoryItem> getHistory(UserHistoryItem.Type type, User user)
    {
        return userHistoryItems;
    }

    @Override
    public List<UserHistoryItem> getHistory(UserHistoryItem.Type type, ApplicationUser user)
    {
        return userHistoryItems;
    }

    @Override
    public void removeHistoryForUser(@Nonnull User user)
    {
    }

    @Override
    public void removeHistoryForUser(@Nonnull ApplicationUser user)
    {
    }

    public List<String> getAddedUsers()
    {
        return addedUsers;
    }
}
