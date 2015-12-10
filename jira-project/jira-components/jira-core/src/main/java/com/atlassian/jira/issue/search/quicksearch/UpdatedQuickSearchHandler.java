package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;

public class UpdatedQuickSearchHandler extends DateQuickSearchHandler
{
    public UpdatedQuickSearchHandler(DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(dateTimeFormatterFactory);
    }

    protected String getPrefix()
    {
        return "updated:";
    }

    protected String getSearchParamName()
    {
        return "updated";
    }
}
