package com.atlassian.jira.sharing.search;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * A comparator that gets a value from a GenericValue and then delegates
 * comparison to either natural order or a specified delegate
 *
 * @since v3.13
 */
public class GenericValueComparator implements Comparator<GenericValue>
{
    private final String field;
    private final Comparator<Object> delegate;

    @SuppressWarnings("unchecked")
    public GenericValueComparator(final String field)
    {
        // can't do anything about commons-collection comparator
        this(field, ComparatorUtils.nullLowComparator(ComparatorUtils.naturalComparator()));
    }

    public GenericValueComparator(final String field, final Comparator<Object> delegate)
    {
        if (StringUtils.isBlank(field))
        {
            throw new IllegalArgumentException("field is blank");
        }
        Assertions.notNull("delegate", delegate);
        this.field = field;
        this.delegate = delegate;
    }

    public int compare(final GenericValue o1, final GenericValue o2)
    {
        Assertions.notNull("o1", o1);
        Assertions.notNull("o2", o2);

        return delegate.compare(o1.get(field), o2.get(field));
    }
}
