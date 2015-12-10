package com.atlassian.jira.rest.api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;

/**
 * This class is used for dealing with query parameters that contain comma-separated lists of strings. The utility
 * methods {@link #fromQueryParam(String)} and {@link #toQueryParam()} can be used to convert back and forth between
 * String and StringList.
 *
 * @since v5.0
 */
@Immutable
public class StringList
{
    /**
     * The separator used in the query parameter.
     */
    private static final String QUERY_PARAM_SEPARATOR = ",";

    /**
     * Creates a new StringList from an array of strings.
     *
     * @param strings an array of String
     * @return a new StringList
     */
    public static StringList fromList(String... strings)
    {
        return new StringList(Arrays.asList(strings));
    }

    /**
     * Creates a new StringList from an Iterable of strings.
     *
     * @param strings an array of String
     * @return a new StringList
     */
    public static StringList fromList(Iterable<String> strings)
    {
        return new StringList(strings);
    }

    /**
     * Creates a new StringList from a query parameter. The input string is expected to be a comma-separated list of
     * strings.
     *
     * @param queryParam a comma-separated String list
     * @return a new StringList
     */
    public static StringList fromQueryParam(String queryParam)
    {
        return new StringList(queryParam);
    }

    private final ImmutableList<String> list;

    /**
     * Creates a new, empty, StringList.
     */
    public StringList()
    {
        this(Collections.<String>emptyList());
    }

    /**
     * Constructs the List of Strings from a query param string.
     *
     * @param queryParam a String containing the query param value
     */
    public StringList(@Nullable String queryParam)
    {
        this(queryParam != null ? Arrays.asList(split(queryParam, QUERY_PARAM_SEPARATOR)) : null);
    }

    /**
     * Constructs the List of Strings from another List.
     *
     * @param fields a List of Strings
     */
    public StringList(@Nullable Iterable<String> fields)
    {
        this.list = fields != null ? ImmutableList.copyOf(fields) : ImmutableList.<String>of();
    }

    /**
     * Returns an immutable list of strings.
     *
     * @return an ImmutableList&lt;String&gt;
     */
    public ImmutableList<String> asList()
    {
        return list;
    }

    /**
     * Returns a string representation of this StringList, suitable for using in a query param. This essentially a
     * "{@value #QUERY_PARAM_SEPARATOR}"-separated list of values.
     *
     * @return a String containing a representation of this StringList
     */
    public String toQueryParam()
    {
        return join(list, QUERY_PARAM_SEPARATOR);
    }

    /**
     * Returns a new StringList containing the concatenation of this StringList's fields and the fields in the given
     * string list.
     *
     * @param stringList a StringList
     * @return a new StringList
     */
    public StringList extend(StringList stringList)
    {
        return new StringList(Iterables.concat(list, stringList.list));
    }

    /**
     * Returns a new StringList containing the concatenation of all of the StringLists in the given List.
     *
     * @param stringLists a List of StringLists
     * @return a new StringList
     */
    public static StringList joinLists(final List<StringList> stringLists)
    {
        StringList result = new StringList();
        for (StringList stringList : stringLists)
        {
            if (stringList != null) // Jersey creates null entries when the queryparam is the empty string
            {
                result = result.extend(stringList);
            }
        }

        return result;
    }

    @Override
    public String toString()
    {
        return "StringList" + list.toString();
    }
}
