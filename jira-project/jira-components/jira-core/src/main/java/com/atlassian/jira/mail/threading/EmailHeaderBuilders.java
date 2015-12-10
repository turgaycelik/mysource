package com.atlassian.jira.mail.threading;

import com.atlassian.jira.issue.Issue;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Responsible for holding the builders used to create the headers inserted into JIRA's notification emails.
 * </p>
 *
 * <p>
 * The RFCs for email specify that headers for any messages must contain a globally unique
 * identifier (the Message-ID) that allows the email recipient to distinguish between new emails and
 * emails they have already received. When JIRA sends an email about an issue, the Message-ID header field is set to:
 * </p>
 *
 * {@code JIRA.${issue-id}.${issue-created-date-millis}.${sequence}.${current-time-millis}@${host} }
 *
 * <p>
 * In JIRA, Message-ID uniqueness is achieved by using the current time in milliseconds
 * as part of the Message-ID. However, more that one email may be sent in the same millisecond by JIRA, and so in order
 * to preserve global uniqueness an atomic counter is also used as part of the Message-ID, such that
 * no two emails sent in the same millisecond can have the same value provided by this atomic counter (the "sequence"
 * member of {@code MessageIdBuilder} below corresponds to this counter: the thread-safety of "sequence" is not the
 * responsibility of this class, and is assumed to be taken care of by the caller).
 * The {@code MessageIdBuilder} class below encapsulates the Message-ID generation logic in JIRA.
 * </p>
 *
 * <p>
 * The "InReplyTo" header field in the email is used to keep track of what previous email a new email
 * is replying to. This is used by email clients to facilitate conversation threading. Typically,
 * the InReplyTo field will contain the Message-ID of the email that is being replied to. JIRA's use of this
 * field is atypical in this sense. When JIRA sends an email about an issue, the InReplyTo header field is set to:
 * </p>
 *
 * {@code <JIRA.${issue-id}.${issue-created-date-millis}@${host}>}
 *
 * <p>
 * This causes the recipient of the email to group this email with other emails with the same
 * InReplyTo header, so that hopefully emails from the same host regarding the same issue are considered part of
 * the same thread. How email threading works is specific to the email client, and it will not always be the case that
 * the InReplyTo header value will be used for this purpose, however this header value can be re-used in other
 * header values such as the "References" header value (i.e. Outlook),
 * in order to increase the likeliness that email threading
 * will be triggered appropriately in the client. The reason that JIRA does not simply use previous Message-IDs
 * for the InReplyTo header value is that multiple users may all be sent emails regarding an issue,
 * and each will have a different Message-ID. It is impractical for JIRA to keep track of all the previous emails
 * sent to different users, as this requires vast database resources.
 * </p>
 *
 * <p>
 * An important difference between the {@code MessageIdBuilder} and {@code InReplyToHeaderBuilder} is that {@code MessageIdBuilder} does
 * not wrap the generated string with angle brackets, but {@code InReplyToHeaderBuilder} does. Both the Message-ID and
 * InReplyTo header fields are wrapped with angle brackets, but the {@code MessageIdBuilder} assumes that this is
 * done after it has transferred control back to the caller, and {@code InReplyToHeaderBuilder} encapsulates this behaviour
 * as part of its functionality. This is simply the result of catering for the existing callers.
 * </p>
 *
 */
public class EmailHeaderBuilders
{
    /**
     * Responsible for building the Message-Id string values to be included in JIRA's issue email notifications.
     *
     * <p>
     * This has to be globally unique and can only contain certain ASCII characters. Used to distinguish
     * between different emails. {@code sequence} and {@code System.currentTimeMillis()} are used to make the Message-ID
     * unique.
     * </p>
     *
     * {@code JIRA.${issue-id}.${created-date-millis}.${sequence-id}.${current-time-millis}@${host} }
     *
     * <p>
     * These message-ids are parsed by
     * {@link com.atlassian.jira.mail.MailThreadManager#getAssociatedIssueObject(javax.mail.Message)}
     * </p>
     */
    public static class MessageIdBuilder
    {
        private Issue issue;

        private int sequence;

        private String hostName;

        private boolean hasSequence;

        // JRA-37319: We drop millis by default to keep email threads if it is dropped by database/driver.
        private boolean dropMillis = true;

        public MessageIdBuilder(final Issue issue)
        {
            this.issue = issue;
        }

        public MessageIdBuilder setSequence(final int sequence)
        {
            this.sequence = sequence;
            hasSequence = true;
            return this;
        }

        public MessageIdBuilder setHostName(final String hostName)
        {
            checkNotNull(hostName);
            this.hostName = hostName;
            return this;
        }

        public MessageIdBuilder setDropMillis(final boolean dropMillis)
        {
            this.dropMillis = dropMillis;
            return this;
        }

        /**
         * {@code sequence} and {@code hostname} are required for this method to work, since they are needed
         * for the returned String to be valid as a Message-ID for JIRA's purposes. It is important that
         * the resulting String is NOT wrapped in angle brackets, even though email spec requires this, since this is
         * the responsibility of the caller (in contrast to {@code InReplyToHeaderBuilder}).
         *
         * @return a String representation of a Message-ID to be used as an email header value.
         */
        public String build()
        {
            checkState(hasSequence);
            checkNotNull(hostName);
            return "JIRA." + issue.getId() + '.' + getCreatedDateInMillis(issue, dropMillis) + '.' +
                    sequence + '.' + System.currentTimeMillis() +
                    "@" + hostName;
        }
    }

    /**
     * <p>
     * Responsible for building the string value for the In-Reply-To header included in JIRA's email notifications.
     * Format of String is:
     * </p>
     *
     * <p>
     * {@code <JIRA.${issue-id}.${created-date-millis}@${hostY}> }
     * </p>
     *
     * <p>
     * {@code created-date-millis} can be the String "null" if at some point the issue corresponding to
     * {@code issue-id} had its created-date set to null.
     * </p>
     *
     */
    public static class InReplyToHeaderBuilder
    {
        private Issue issue;

        private String hostName;

        // JRA-37319: We drop millis by default to keep email threads if it is dropped by database/driver.
        private boolean dropMillis = true;

        public InReplyToHeaderBuilder(final Issue issue)
        {
            this.issue = issue;
        }

        public InReplyToHeaderBuilder setHostName(final String hostName)
        {
            this.hostName = hostName;
            return this;
        }

        public InReplyToHeaderBuilder setDropMillis(final boolean dropMillis)
        {
            this.dropMillis = dropMillis;
            return this;
        }

        /**
         * It is important that this method wraps the resulting String with angle brackets, since this is
         * NOT the responsibility of the caller, in contrast to the build method in the {@code MessageIdBuilder} class.
         *
         * @return a String representation of the In-Reply-To email header value.
         */
        public String build()
        {
            checkNotNull(hostName);
            return "<JIRA." + issue.getId() + "." + getCreatedDateInMillis(issue, dropMillis) + "@" + hostName + ">";
        }
    }

    /**
     * <p>
     * Returns the createdDateInMillis of {@code issue} as a string. This method can return the string "null"
     * if the created-date for {@code issue} is null. This can occur if the jiraissue table in the DB becomes corrupted
     * and the created-date of the issue is somehow nullified. If this happens, the work-around is to return "null"
     * here and use this where ever the created-date string might have been used. One implication of this is that
     * all emails with the InReplyTo header field generated by {@code InReplyToHeaderBuilder} equal to:
     * </p>
     *
     * {@code <JIRA.${issueX-id}.null@${hostY}> }
     *
     * <p>
     * will be considered by JIRA to be referencing the same issue, even though these emails may be referencing
     * issues on different servers that share the same issue-id. Given that database corruption is a possibility,
     * this is the best work-around currently available.
     * </p>
     *
     * @param issue the Issue to get the createdDateInMillis for.
     * @return createdDateInMillis for {@code issue} as a String
     */
    private static String getCreatedDateInMillis(final Issue issue, boolean dropMillis)
    {
        if (issue.getCreated() == null)
        {
            return "null";
        }
        long issueCreated = issue.getCreated().getTime();
        if (dropMillis)
        {
            // JRA-37319 Round off the millis because on some DBs this gets dropped between create issue and update issue
            // We want to leave the 000 on the end so we can still recognise old Message-ID's that were previously created.
            issueCreated = issueCreated - (issueCreated % 1000);
        }
        return Long.toString(issueCreated);
    }
}
