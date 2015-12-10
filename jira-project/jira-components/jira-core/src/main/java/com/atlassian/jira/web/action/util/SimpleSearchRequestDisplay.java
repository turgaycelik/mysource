package com.atlassian.jira.web.action.util;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;

public class SimpleSearchRequestDisplay
{
    private final Long id;
    private final String name;
    private final ApplicationUser owner;

    public SimpleSearchRequestDisplay(final SearchRequest searchRequest)
    {
        id = searchRequest.getId();
        name = searchRequest.getName();
        owner = searchRequest.getOwner();
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getOwnerUserName()
    {
        return owner == null ? null : owner.getUsername();
    }
}
