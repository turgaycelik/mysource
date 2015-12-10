package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.history.DateRangeBuilder;
import com.atlassian.jira.jql.resolver.NameResolver;

/**
 * Representsa a system field that can be searched by JQL
 *
 * @since v5.0
 */
public class ChangeHistoryFieldConfiguration
{
    private final DateRangeBuilder dateRangeBuilder;
    private final String emptyValue;
    private final boolean supportsIdSearching;
    private final NameResolver nameResolver;


    public ChangeHistoryFieldConfiguration(DateRangeBuilder dateRangeBuilder, String emptyValue, NameResolver nameResolver, boolean supportsIdSearching)
    {
        this.dateRangeBuilder = dateRangeBuilder;
        this.emptyValue = emptyValue;
        this.nameResolver = nameResolver;
        this.supportsIdSearching = supportsIdSearching;
    }

    public DateRangeBuilder getDateRangeBuilder()
    {
        return dateRangeBuilder;
    }

    public String getEmptyValue()
    {
        return emptyValue;
    }

    public boolean supportsIdSearching()
    {
        return supportsIdSearching;
    }

    public NameResolver getNameResolver()
    {
        return nameResolver;
    }
}
