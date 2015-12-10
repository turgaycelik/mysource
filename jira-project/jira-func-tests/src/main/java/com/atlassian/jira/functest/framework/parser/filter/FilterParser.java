package com.atlassian.jira.functest.framework.parser.filter;

/**
 * Parser for filter related functionality
 *
 * @since v3.13
 */
public interface FilterParser
{
    public final class TableId
    {
        public static final String FAVOURITE_TABLE = "mf_favourites";
        public static final String OWNED_TABLE = "mf_owned";
        public static final String POPULAR_TABLE = "mf_popular";
        public static final String SEARCH_TABLE = "mf_browse";
    }

    /**
     * Parse a list of filters created by filter-list.jsp
     *
     * @param tableId id of the table testing
     * @return object containing info on filters and the table or null if no such table exists.
     */
    FilterList parseFilterList(String tableId);
}
