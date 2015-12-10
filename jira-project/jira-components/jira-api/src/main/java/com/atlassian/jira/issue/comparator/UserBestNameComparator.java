package com.atlassian.jira.issue.comparator;

import com.atlassian.crowd.embedded.api.User;
import org.apache.commons.lang.StringUtils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * This comparator tries to compare two users based on their 'best name'
 * ie their full name if possible, otherwise their username.
 * <p/>
 * This comparator completely ignores case. This uses the users locale to make sure that we correctly sort
 * i18n characters.
 *
 * @deprecated since v6.2. This class has poor performance and has been replaced by {@link UserCachingComparator} which
 * should be preferred.
 *
 * @since v4.0
 */
@Deprecated
public class UserBestNameComparator implements Comparator<User>
{
    private final Collator collator;

    public UserBestNameComparator(Locale locale)
    {
        this.collator = Collator.getInstance(locale);
        // Make this case insensitive
        this.collator.setStrength(Collator.SECONDARY);
    }

    public UserBestNameComparator()
    {
        this.collator = Collator.getInstance();
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

        String name1 = user1.getDisplayName();
        String name2 = user2.getDisplayName();
        if (StringUtils.isBlank(name1))
        {
            name1 = user1.getName();
        }
        if (StringUtils.isBlank(name2))
        {
            name2 = user2.getName();
        }
        if (name1 == null || name2 == null)
        {
            throw new RuntimeException("Null user name");
        }

        final int fullNameComparison = collator.compare(name1, name2);
        if (fullNameComparison == 0) //if full names are the same, we should check the username (JRA-5847)
        {
            return collator.compare(user1.getName(), user2.getName());
        }
        else
        {
            return fullNameComparison;
        }
    }
}
