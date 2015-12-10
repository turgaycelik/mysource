package com.atlassian.jira.plugins.share.event;

import java.util.Set;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;

/**
 * Defines a payload for sharing a filter.
 *
 * @since v5.0
 */
@PublicApi
public class ShareSearchRequestEvent extends AbstractShareEvent
{
    private final SearchRequest filter;

    public ShareSearchRequestEvent(User fromUser, Set<String> toUsernames, Set<String> toEmails, String comment, final SearchRequest filter)
    {
        super(fromUser, toUsernames, toEmails, comment);
        this.filter = filter;
    }

    public SearchRequest getFilter()
    {
        return filter;
    }
}
