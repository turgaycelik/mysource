package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;

import java.util.Map;

public class MyIssuesQuickSearchHandler extends SingleWordQuickSearchHandler implements QuickSearchHandler
{
    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        return "my".equals(word) ? EasyMap.build("assigneeSelect", "issue_current_user") : null;
    }

}
