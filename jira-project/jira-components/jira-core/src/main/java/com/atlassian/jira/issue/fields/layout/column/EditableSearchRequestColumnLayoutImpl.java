/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;

import java.util.List;

public class EditableSearchRequestColumnLayoutImpl extends EditableUserColumnLayoutImpl implements EditableSearchRequestColumnLayout
{
    private SearchRequest searchRequest;

    public EditableSearchRequestColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems, User user, SearchRequest searchRequest, ColumnConfig columnConfig)
    {
        super(columnLayoutItems, user, columnConfig);
        this.searchRequest = searchRequest;
    }

    public EditableSearchRequestColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems, User user, SearchRequest searchRequest)
    {
        this(columnLayoutItems, user, searchRequest, ColumnConfig.FILTER);
    }

    public SearchRequest getSearchRequest()
    {
        return searchRequest;
    }

    public void setSearchRequest(SearchRequest searchRequest)
    {
        this.searchRequest = searchRequest;
    }
}
