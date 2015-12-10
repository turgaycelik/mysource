package com.atlassian.jira.web.component.multiuserpicker;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A bean for pickertable.vm that is used for displaying the user info.
 *
 * @since v3.13
 */
public class UserBean
{
    private final String username;
    private final String fullName;
    private final boolean isReal;

    /**
     * Converst a collection of usernames to an ordered list of user beans
     *
     * @param usernames a collection of usernames
     * @return a list of user beans, never null
     */
    public static List<UserBean> convertUsernamesToUserBeans(final Locale userLocale, final Collection<String> usernames)
    {
        final UserManager userManager = ComponentAccessor.getUserManager();
        final List<UserBean> userBeans = new ArrayList<UserBean>(usernames.size());
        for (final String username : usernames)
        {
            final ApplicationUser user = userManager.getUserByName(username);
            if (user == null)
            {
                userBeans.add(new UserBean(username));
            }
            else
            {
                userBeans.add(new UserBean(username, user.getDisplayName()));
            }
        }
        Collections.sort(userBeans, new UserBeanComparator(userLocale));
        return userBeans;
    }

    /**
     * Converts a collection of user objects to an oredered list of user beans.
     *
     * @param users collection of user objects
     * @return list of user beans, never null
     */
    public static List<UserBean> convertUsersToUserBeans(final Locale userLocale, final Collection<User> users)
    {
        final List<UserBean> userBeans = new ArrayList<UserBean>(users.size());
        for (final User user : users)
        {
            userBeans.add(new UserBean(user.getName(), user.getDisplayName()));
        }
        Collections.sort(userBeans, new UserBeanComparator(userLocale));
        return userBeans;
    }

    /**
     * Constructs a bean for an existing user
     *
     * @param username username
     * @param fullName user's full name
     */
    public UserBean(final String username, final String fullName)
    {
        this.username = username;
        this.fullName = fullName;
        isReal = true;
    }

    /**
     * Constructs a bean for a non-existing user
     *
     * @param username username
     */
    public UserBean(final String username)
    {
        this.username = username;
        fullName = "";
        isReal = false;
    }

    /**
     * Returns the username
     *
     * @return username
     */
    public String getName()
    {
        return username;
    }

    /**
     * Returns user's full name if this bean represents an existing user, empty string otherwise
     *
     * @return user's full name if this bean represents an existing user, empty string otherwise
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * Returns true if this bean represents an existing user, false otherwise
     *
     * @return true if this bean represents an existing user, false otherwise
     */
    public boolean isReal()
    {
        return isReal;
    }

    /**
     * Compares to user beans by full name comparison first, then username.
     * <p/>
     * It sorts users alphabetically by full name, if the user does not exist,
     * only usernames is known, then it makes sure that such user bean comes last.
     */
    static class UserBeanComparator implements Comparator<UserBean>
    {
        private Collator collator;

        UserBeanComparator(Locale userLocale)
        {
            this.collator = Collator.getInstance(userLocale);
            // Make this case insensitive
            this.collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final UserBean bean1, final UserBean bean2)
        {

            String name1 = bean1.getFullName();
            String name2 = bean2.getFullName();

            if (StringUtils.isBlank(name1))
            {
                name1 = bean1.getName();
            }
            if (StringUtils.isBlank(name2))
            {
                name2 = bean2.getName();
            }

            if (name1 == null || name2 == null)
            {
                throw new RuntimeException("Null user name");
            }

            final int fullNameComparison = collator.compare(name1, name2);
            if (fullNameComparison == 0) //if full names are the same, we should check the username (JRA-5847)
            {
                return collator.compare(bean1.getName(), bean2.getName());
            }
            else
            {
                return fullNameComparison;
            }
        }
    };
}
