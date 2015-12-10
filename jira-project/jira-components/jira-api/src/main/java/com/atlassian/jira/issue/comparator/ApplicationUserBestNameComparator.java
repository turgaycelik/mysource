package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.user.ApplicationUser;
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
 * @since v6.0
 */
public class ApplicationUserBestNameComparator implements Comparator<ApplicationUser>
{
    private final Collator collator;

    public ApplicationUserBestNameComparator(Locale locale)
    {
        this.collator = Collator.getInstance(locale);
        // Make this case insensitive
        this.collator.setStrength(Collator.SECONDARY);
    }

    public ApplicationUserBestNameComparator()
    {
        this.collator = Collator.getInstance();
        // Make this case insensitive
        this.collator.setStrength(Collator.SECONDARY);
    }

    public int compare(final ApplicationUser user1, final ApplicationUser user2)
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
            name1 = user1.getUsername();
        }
        if (StringUtils.isBlank(name2))
        {
            name2 = user2.getUsername();
        }
        if (name1 == null || name2 == null)
        {
            throw new RuntimeException("Null user name");
        }

        final int fullNameComparison = collator.compare(name1, name2);
        if (fullNameComparison == 0) //if full names are the same, we should check the username (JRA-5847)
        {
            return collator.compare(user1.getUsername(), user2.getUsername());
        }
        else
        {
            return fullNameComparison;
        }
    }
}
