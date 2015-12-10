package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.notification.NotificationRecipient;

import java.util.Set;

/**
 * @see MentionIssueEvent except that only fires for mentions on comments.
 *
 * @since v5.0.2
 */
@PublicApi
public class MentionIssueCommentEvent extends MentionIssueEvent
{
    private final Comment comment;

    public MentionIssueCommentEvent(Issue issue, User fromUser, Set<User> toUsers, String mentionText, String fieldId, Set<NotificationRecipient> currentRecipients, Comment comment)
    {
        super(issue, fromUser, toUsers, mentionText, fieldId, currentRecipients);
        this.comment = comment;
    }

    public Comment getComment()
    {
        return comment;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MentionIssueCommentEvent that = (MentionIssueCommentEvent) o;

        if (!comment.equals(that.comment)) { return false; }

        return super._equals(that);
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + comment.hashCode();
    }

    @Override
    public String toString()
    {
        return buildToString().append("comment", comment).toString();
    }
}
