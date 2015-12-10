package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.issue.Issue;
import com.google.common.base.Objects;

/**
 * IssuePreDeleteEvent is triggered before an issue is deleted.
 *
 * @since 5.2.6
 */
@PublicApi
public final class IssuePreDeleteEvent extends AbstractEvent implements IssueRelatedEvent
{
    private final Issue issue;
    private final User user;

    public IssuePreDeleteEvent(final Issue issue, final User user)
    {
        this.issue = issue;
        this.user = user;
    }

    @Override
    public Issue getIssue()
    {
        return issue;
    }

    public User getUser()
    {
        return user;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o instanceof IssuePreDeleteEvent) {
            final IssuePreDeleteEvent that = (IssuePreDeleteEvent) o;
            return super.equals(that)
                    && Objects.equal(this.user, that.user)
                    && Objects.equal(this.issue, that.issue);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(super.hashCode(), issue, user);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("time", time)
                .add("params", params)
                .add("issue", issue)
                .add("user", user)
                .toString();
    }
}
