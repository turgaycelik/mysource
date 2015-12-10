package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;

import java.util.Map;

public class OverdueQuickSearchHandler extends SingleWordQuickSearchHandler implements QuickSearchHandler
{
    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        return "overdue".equals(word) ? EasyMap.build("duedate:next", "0") : null;
    }

}
