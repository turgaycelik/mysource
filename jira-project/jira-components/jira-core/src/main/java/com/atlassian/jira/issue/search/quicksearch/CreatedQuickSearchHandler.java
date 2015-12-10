package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;

public class CreatedQuickSearchHandler extends DateQuickSearchHandler
{
    public CreatedQuickSearchHandler(DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(dateTimeFormatterFactory);
    }

    protected String getPrefix()
    {
        return "created:";
    }

    protected String getSearchParamName()
    {
        return "created";
    }
}
