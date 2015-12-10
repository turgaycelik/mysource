package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * When a user mentions another user on an issue via the @username or [~username] syntax this event will be fired. It
 * contains the user that added the comment or description, all users mentioned, the issue as well as the text in which
 * the mentions occurred. Finally it also contains the fieldId of the field in which users were mentioned.
 *
 * @since v5.0
 */
@PublicApi
public class MentionIssueEvent
{
    private final Issue issue;
    private final User fromUser;
    private final Set<User> toUsers;
    private final String mentionText;
    private final String fieldId;
    private final Set<NotificationRecipient> currentRecipients;

    public MentionIssueEvent(Issue issue, User fromUser, Set<User> toUsers, String mentionText, String fieldId, Set<NotificationRecipient> currentRecipients)
    {
        this.issue = issue;
        this.fromUser = fromUser;
        this.toUsers = toUsers;
        this.mentionText = mentionText;
        this.fieldId = fieldId;
        this.currentRecipients = currentRecipients;
    }

    public Issue getIssue()
    {
        return issue;
    }

    public User getFromUser()
    {
        return fromUser;
    }

    public Set<User> getToUsers()
    {
        return Collections.unmodifiableSet(toUsers);
    }

    public String getMentionText()
    {
        return mentionText;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public Set<NotificationRecipient> getCurrentRecipients()
    {
        return currentRecipients;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        return _equals((MentionIssueEvent) o);
    }

    protected boolean _equals(MentionIssueEvent that)
    {
        if (!currentRecipients.equals(that.currentRecipients)) { return false; }
        if (!fieldId.equals(that.fieldId)) { return false; }
        if (!fromUser.equals(that.fromUser)) { return false; }
        if (issue != null ? !issue.equals(that.issue) : that.issue != null) { return false; }
        if (mentionText != null ? !mentionText.equals(that.mentionText) : that.mentionText != null) { return false; }
        if (!toUsers.equals(that.toUsers)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = issue != null ? issue.hashCode() : 0;
        result = 31 * result + fromUser.hashCode();
        result = 31 * result + toUsers.hashCode();
        result = 31 * result + (mentionText != null ? mentionText.hashCode() : 0);
        result = 31 * result + fieldId.hashCode();
        result = 31 * result + currentRecipients.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return buildToString().toString();
    }

    protected ToStringBuilder buildToString()
    {
        return new ToStringBuilder(this).
                append("issue", issue).
                append("fromUser", fromUser).
                append("toUsers", toUsers).
                append("mentionText", mentionText).
                append("fieldId", fieldId).
                append("currentRecipients", currentRecipients);
    }
}
