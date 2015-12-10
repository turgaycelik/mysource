package com.atlassian.jira.plugins.share.event;

import java.util.Set;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Defines a payload for sharing a search on the issue navigator.
 *
 * @since v5.0
 */
@PublicApi
public class ShareJqlEvent extends AbstractShareEvent
{
    private final String jql;

    public ShareJqlEvent(User fromUser, Set<String> toUsernames, Set<String> toEmails, String comment, final String jql)
    {
        super(fromUser, toUsernames, toEmails, comment);
        this.jql = jql;
    }

    public String getJql()
    {
        return jql;
    }
}
