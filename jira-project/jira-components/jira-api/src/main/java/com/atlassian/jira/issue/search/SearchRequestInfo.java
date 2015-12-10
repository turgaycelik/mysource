package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.query.Query;

/**
 * A class representing information related to a Search Request.
 */
@PublicApi
public class SearchRequestInfo
{
    public final Query query;
    public final Long id;
    public final boolean modified;

    public SearchRequestInfo(SearchRequest searchRequest)
    {
        this.query = searchRequest.getQuery();
        this.id = searchRequest.getId();
        this.modified = searchRequest.isModified();
    }

    public SearchRequestInfo(Query query, Long id, boolean modified)
    {
        this.query = query;
        this.id = id;
        this.modified = modified;
    }

    public Query getQuery()
    {
        return query;
    }

    public Long getId()
    {
        return id;
    }

    public boolean isModified()
    {
        return modified;
    }
}