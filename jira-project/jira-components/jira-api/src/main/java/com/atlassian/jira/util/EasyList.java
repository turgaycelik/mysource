package com.atlassian.jira.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Legacy utility class for instantiating lists. We now prefer to use <code>com.google.common.collect.Lists</code> from
 * google-collections / guava.
 *
 * @deprecated Use {@link com.google.common.collect.Lists} instead. Since v5.0.
 */
@Deprecated
public class EasyList
{
    private static final List<?> ONLY_NULL = Collections.singletonList(null);

    /**
     * Creates a list with one null value. Occasionally useful.
     * @return a list with one null value.
     * @deprecated
     */
    @SuppressWarnings ("unchecked")
    public static <T> List<T> buildNull()
    {
        return (List<T>)ONLY_NULL;
    }

    /**
     * @deprecated Use {@link com.google.common.collect.Lists#newArrayList(Object[])} instead. Since v5.0.
     */
    @Deprecated
    public static <T> List<T> build(final T elem)
    {
        // Less overhead than using Lists.newArrayList
        final List<T> list = new ArrayList<T>(6);
        list.add(elem);
        return list;
    }

    /**
     * @deprecated Use {@link com.google.common.collect.Lists#newArrayList(Object[])} instead. Since v5.0.
     */
    @Deprecated
    public static <T> List<T> build(final T... elems)
    {
        return Lists.newArrayList(elems);
    }
}
