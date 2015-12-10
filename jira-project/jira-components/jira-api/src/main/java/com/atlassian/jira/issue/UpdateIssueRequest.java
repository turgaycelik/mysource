package com.atlassian.jira.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.google.common.base.Objects;

import javax.annotation.Nullable;

/**
 * Groups parameters for {{@link com.atlassian.jira.issue.IssueManager#updateIssue(com.atlassian.jira.user.ApplicationUser, MutableIssue, UpdateIssueRequest)}}
 * specifying details for the issue update
 *
 * @since JIRA 6.3
 */
@PublicApi
public final class UpdateIssueRequest
{
    private EventDispatchOption eventDispatchOption;
    private boolean sendMail;
    @Nullable
    private HistoryMetadata historyMetadata;

    private UpdateIssueRequest(final UpdateIssueRequestBuilder builder)
    {
        this.eventDispatchOption = builder.eventDispatchOption;
        this.sendMail = builder.sendMail;
        this.historyMetadata = builder.historyMetadata;
    }

    /**
     * Defines an event dispatching strategy for the update
     * @see com.atlassian.jira.event.type.EventDispatchOption
     */
    public EventDispatchOption getEventDispatchOption()
    {
        return eventDispatchOption;
    }

    /**
     * If true mail notifications will be sent, otherwise mail notifications will be suppressed.
     */
    public boolean isSendMail()
    {
        return sendMail;
    }

    /**
     * Additional metadata to be persisted in history with the update operation, null if none
     * @see com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata
     */
    @Nullable
    public HistoryMetadata getHistoryMetadata()
    {
        return historyMetadata;
    }

    /**
     * Provides a builder to create the UpdateIssueRequest object.
     */
    public static UpdateIssueRequestBuilder builder() {
        return new UpdateIssueRequestBuilder();
    }

    /**
     * This method is implemented for usage in Unit Tests.
     */
    @Override
    public int hashCode() {return Objects.hashCode(eventDispatchOption, sendMail, historyMetadata);}

    /**
     * This method is implemented for usage in Unit Tests.
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) {return true;}
        if (obj == null || getClass() != obj.getClass()) {return false;}
        final UpdateIssueRequest other = (UpdateIssueRequest) obj;
        return Objects.equal(this.eventDispatchOption, other.eventDispatchOption)
                && Objects.equal(this.sendMail, other.sendMail) && Objects.equal(this.historyMetadata, other.historyMetadata);
    }

    public static class UpdateIssueRequestBuilder {
        private EventDispatchOption eventDispatchOption = EventDispatchOption.ISSUE_UPDATED;
        private boolean sendMail = true;
        @Nullable
        private HistoryMetadata historyMetadata;

        private UpdateIssueRequestBuilder() {}

        public UpdateIssueRequestBuilder eventDispatchOption(final EventDispatchOption eventDispatchOption)
        {
            this.eventDispatchOption = eventDispatchOption;
            return this;
        }

        public UpdateIssueRequestBuilder sendMail(final boolean sendMail)
        {
            this.sendMail = sendMail;
            return this;
        }

        public UpdateIssueRequestBuilder historyMetadata(@Nullable final HistoryMetadata historyMetadata)
        {
            this.historyMetadata = historyMetadata;
            return this;
        }

        public UpdateIssueRequest build()
        {
            return new UpdateIssueRequest(this);
        }
    }
}
