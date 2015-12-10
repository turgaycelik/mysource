package com.atlassian.jira.plugins.share.event;

import java.util.Set;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

/**
 * Defines a payload for sharing a JIRA Issue.
 *
 * @since v5.0
 */
@PublicApi
public class ShareIssueEvent extends AbstractShareEvent
{
    private final Issue issue;

    public ShareIssueEvent(Issue issue, User fromUser, Set<String> toUsernames, Set<String> toEmails, String comment)
    {
        super(fromUser, toUsernames, toEmails, comment);
        this.issue = issue;
    }

    public Issue getIssue()
    {
        return issue;
    }
}
