package com.atlassian.jira.issue.comparator;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import com.atlassian.crowd.embedded.api.User;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This comparator tries to compare two users based on their 'best name'
 * ie their full name if possible, otherwise their username.
 * <p/>
 * This comparator completely ignores case, using the specified locale to make sure that
 * we correctly sort i18n characters.  It uses locale-sensitive collation, which is very
 * expensive in CPU time.  To minimise the overhead of this, it caches the collation keys
 * for users internally.  See {@link http://docs.oracle.com/javase/tutorial/i18n/text/perform.html}
 * for more information about collation keys and their performance implications.
 * <p/>
 * <strong>WARNING</strong>:
 * This class is NOT thread safe and caches information potentially about all users.
 * You should not reuse instances of this class beyond the scope of a single servlet/rest/web request.
 * <p/>
 *
 * @since v6.2
 */
@SuppressWarnings("ComparatorNotSerializable")
@NotThreadSafe
public class UserCachingComparator implements Comparator<User>
{
    private final Collator collator;
    private final Map<String, CollationKey> collationKeys = new HashMap<String, CollationKey>(4096);

    public UserCachingComparator(Locale locale)
    {
        this.collator = Collator.getInstance(locale);
        // Make this case insensitive
        this.collator.setStrength(Collator.SECONDARY);
    }

    public UserCachingComparator()
    {
        this(Locale.getDefault());
    }

    public int compare(final User user1, final User user2)
    {
        //noinspection ObjectEquality
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

        final int fullNameComparison = getKey(user1).compareTo(getKey(user2));
        if (fullNameComparison == 0) //if full names are the same, we should check the username (JRA-5847)
        {
            return collator.compare(user1.getName(), user2.getName());
        }
        else
        {
            return fullNameComparison;
        }
    }

    private CollationKey getKey(final User user)
    {
        CollationKey collationKey = collationKeys.get(user.getName());
        if (collationKey == null)
        {
            String name = user.getDisplayName();
            if (StringUtils.isBlank(name))
            {
                name = notNull("user.getName()", user.getName());
            }
            collationKey = collator.getCollationKey(name);
            collationKeys.put(user.getName(), collationKey);
        }
        return collationKey;
    }
}
