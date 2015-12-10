/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.search.SearchRequest;

@PublicApi
public interface EditableSearchRequestColumnLayout extends EditableUserColumnLayout
{
    public SearchRequest getSearchRequest();

    public void setSearchRequest(SearchRequest searchRequest);
}
