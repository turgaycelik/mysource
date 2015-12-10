package com.atlassian.jira.issue.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Uses a locale to do a correct sort that will correctly sort i18n strings.
 *
 * @since v4.0
 */
public class LocaleSensitiveStringComparator implements Comparator<String>
{
    private final Collator collator;

    public LocaleSensitiveStringComparator(final Locale locale)
    {
        this.collator = Collator.getInstance(locale);
        // Make this case insensitive
        this.collator.setStrength(Collator.SECONDARY);
    }

    public int compare(final String o1, final String o2)
    {
        if (o1 == null && o2 == null)
        {
            return 0;
        }

        if (o1 == null)
        {
            return 1;
        }

        if (o2 == null)
        {
            return -1;
        }

        return collator.compare(o1, o2);
    }
}
