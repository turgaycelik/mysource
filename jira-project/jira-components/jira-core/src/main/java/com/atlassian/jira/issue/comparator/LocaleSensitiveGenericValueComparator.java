package com.atlassian.jira.issue.comparator;

import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;
import java.util.Locale;

/**
 * Compares a generic value using the provided locale to do an i18n string compare on the value specified by the
 * entity column.
 *
 * @since v4.0
 */
public class LocaleSensitiveGenericValueComparator implements Comparator<GenericValue>
{
    private final LocaleSensitiveStringComparator stringComparator;
    private final String entityColumn;

    public LocaleSensitiveGenericValueComparator(final Locale locale, final String entityColumn)
    {
        this.entityColumn = entityColumn;
        stringComparator = new LocaleSensitiveStringComparator(locale);
    }

    public int compare(final GenericValue gv1, final GenericValue gv2)
    {
        if (gv1 == null && gv2 == null)
        {
            return 0;
        }

        if (gv1 == null)
        {
            return 1;
        }

        if (gv2 == null)
        {
            return -1;
        }

        String name1 = gv1.getString(entityColumn);
        String name2 = gv2.getString(entityColumn);

        return stringComparator.compare(name1, name2);
    }
}
