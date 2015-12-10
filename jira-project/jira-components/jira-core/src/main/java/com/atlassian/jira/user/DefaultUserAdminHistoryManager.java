package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A convenience wrapper around the {@link com.atlassian.jira.user.UserHistoryManager} to work directly with admin pages
 * and perform permission checks. The key stored and used for admin pag eretrieval is the link id of the
 * WebItemModuleDescriptor
 *
 * @since v4.1
 */
public class DefaultUserAdminHistoryManager implements UserAdminHistoryManager
{
    private final UserHistoryManager userHistoryManager;


    public DefaultUserAdminHistoryManager(final UserHistoryManager userHistoryManager)
    {
        this.userHistoryManager = userHistoryManager;
    }

    @Override
    public void addAdminPageToHistory(final User user, final String key, final String data)
    {
        notNull("key", key);
        userHistoryManager.addItemToHistory(UserHistoryItem.ADMIN_PAGE, user, key, data);
    }

    @Override
    public List<UserHistoryItem> getAdminPageHistoryWithoutPermissionChecks(final User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.ADMIN_PAGE, user);
        final List<UserHistoryItem> returnList = new ArrayList<UserHistoryItem>();

        if (history != null)
        {
            for (UserHistoryItem userHistoryItem : history)
            {
                final String key = userHistoryItem.getEntityId();
                if (key != null)
                {
                    returnList.add(userHistoryItem);
                }
            }
        }
        return returnList;
    }
}
