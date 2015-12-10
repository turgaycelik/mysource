package com.atlassian.jira.issue.comparator;

import com.atlassian.crowd.embedded.api.User;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Used to sort users based on {@link User#getName()}, not taking into account the users
 * full name. If you want to sort first on the full name then use a
 * {@link UserCachingComparator}.
 *
 * @since v4.0
 */
public class UserNameComparator implements Comparator<User>
{
    private final Collator collator;

    public UserNameComparator(Locale locale)
    {
        this.collator = Collator.getInstance(locale);
        // Make this case insensitive
        this.collator.setStrength(Collator.SECONDARY);
    }

    public int compare(final User user1, final User user2)
    {
        if (user1 == user2)
        {
            return 0;
        }
        else if (user2 == null)
        {
            return -1;
        }
        else if (user1 == null)
        {
            return 1;
        }

        String name1 = user1.getName();
        String name2 = user2.getName();
        if (name1 == null || name2 == null)
        {
            throw new RuntimeException("Null user name");
        }

        return collator.compare(name1, name2);
    }
}
