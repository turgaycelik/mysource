package com.atlassian.jira.issue.search.quicksearch;

import java.util.Collection;

public interface QuickSearchResult
{
    public String getQueryString();

    public void addSearchParameter(String key, String value);

    public Collection getSearchParameters(String key);

    public String getSearchInput();

    public void setSearchInput(String searchInput);

}
