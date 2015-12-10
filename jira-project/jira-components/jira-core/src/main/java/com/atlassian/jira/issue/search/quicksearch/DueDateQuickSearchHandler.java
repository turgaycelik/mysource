package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.timezone.TimeZoneManager;

public class DueDateQuickSearchHandler extends LocalDateQuickSearchHandler
{
    public DueDateQuickSearchHandler(TimeZoneManager timeZoneManager)
    {
        super(timeZoneManager);
    }

    protected String getPrefix()
    {
        return "due:";
    }

    protected String getSearchParamName()
    {
        return "duedate";
    }
}
