package com.atlassian.jira.plugins.share.event;

import java.util.Set;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

/**
 * An abstract event to represent the act of sharing an entity via e-mail with a set of users or e-mail addresses.
 * Entities will typically be issues or filter results.
 *
 * @since v5.0
 */
@PublicSpi
public abstract class AbstractShareEvent
{
    private final User fromUser;
    private final Set<String> toUsernames;
    private final Set<String> toEmails;
    private final String comment;

    public AbstractShareEvent(User fromUser, Set<String> toUsernames, Set<String> toEmails, String comment)
    {
        this.fromUser = fromUser;
        this.toUsernames = toUsernames;
        this.toEmails = toEmails;
        this.comment = comment;
    }

    public User getFromUser()
    {
        return fromUser;
    }

    public Set<String> getToUsernames()
    {
        return toUsernames;
    }

    public Set<String> getToEmails()
    {
        return toEmails;
    }

    public String getComment()
    {
        return comment;
    }
}