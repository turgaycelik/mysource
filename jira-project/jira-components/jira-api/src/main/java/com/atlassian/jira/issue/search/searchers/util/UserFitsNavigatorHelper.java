package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.user.UserUtils;

import java.util.Collection;

/**
 * This is a helper class for determining if a user fits in the navigator.
 *
 * @since v4.0
 */
public class UserFitsNavigatorHelper
{
    private final UserPickerSearchService userPickerSearchService;

    public UserFitsNavigatorHelper(UserPickerSearchService userPickerSearchService)
    {
        this.userPickerSearchService = userPickerSearchService;
    }

    /**
     * This method checks if the user exists and will fit in the navigator, and returns the value that should be shown
     * in the navigator. It first checks if the user exists under the given name as the username but lowercased. If that
     * fails and the user full name search option is on, it checks if the user exists with the given name as the
     * fullname or email, if it does, null is returned because that means that the query will not fit in the simple
     * navigator.
     * <p/>
     * If the user is not found by any means, then the passed in name is returned.
     *
     * @param name the username or fullname of the user to search for
     * @return the username of the user if found by username, null if found by the full name or the passed in name if
     *         not found.
     */
    public String checkUser(final String name)
    {
        String user = findUserName(name);

        if (user != null)
        {
            return user;
        }
        else if (userExistsByFullNameOrEmail(name))
        {
            return null;
        }
        return name;
    }

    ///CLOVER:OFF
    String findUserName(final String name)
    {
        User user = UserUtils.getUser(name.toLowerCase());
        if (user != null)
        {
            return user.getName();
        }
        else
        {
            return null;
        }

    }

    boolean userExistsByFullNameOrEmail(final String name)
    {
        final Collection<User> users = UserUtils.getAllUsers();
        for (User u : users)
        {
            final String fullName = u.getDisplayName();
            final String email = u.getEmailAddress();
            if (fullName != null && fullName.equalsIgnoreCase(name))
            {
                return true;
            }
            else if (email != null && email.equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    ///CLOVER:ON
}
