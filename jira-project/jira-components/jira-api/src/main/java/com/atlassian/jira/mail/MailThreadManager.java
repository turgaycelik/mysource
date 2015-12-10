/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import javax.mail.Message;

@PublicApi
public interface MailThreadManager
{
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_KEY = "NOTIFICATION_";

    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_CREATED = NOTIFICATION_KEY + EventType.ISSUE_CREATED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_UPDATED = NOTIFICATION_KEY + EventType.ISSUE_UPDATED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_ASSIGNED = NOTIFICATION_KEY + EventType.ISSUE_ASSIGNED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_RESOLVED = NOTIFICATION_KEY + EventType.ISSUE_RESOLVED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_CLOSED = NOTIFICATION_KEY + EventType.ISSUE_CLOSED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_COMMENTED = NOTIFICATION_KEY + EventType.ISSUE_COMMENTED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_COMMENT_EDITED = NOTIFICATION_KEY + EventType.ISSUE_COMMENTED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_REOPENED = NOTIFICATION_KEY + EventType.ISSUE_REOPENED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_DELETED = NOTIFICATION_KEY + EventType.ISSUE_DELETED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_WORKLOGGED = NOTIFICATION_KEY + EventType.ISSUE_WORKLOGGED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_WORKLOG_UPDATED = NOTIFICATION_KEY + EventType.ISSUE_WORKLOG_UPDATED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_WORKLOG_DELETED = NOTIFICATION_KEY + EventType.ISSUE_WORKLOG_DELETED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_MOVED = NOTIFICATION_KEY + EventType.ISSUE_MOVED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_WORKSTARTED = NOTIFICATION_KEY + EventType.ISSUE_WORKSTARTED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_WORKSTOPPED = NOTIFICATION_KEY + EventType.ISSUE_WORKSTOPPED_ID;
    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public static final String NOTIFICATION_ISSUE_GENERICEVENT = NOTIFICATION_KEY + EventType.ISSUE_GENERICEVENT_ID;

    /**
     * @deprecated Use {@link MailThreadManager.MailAction#ISSUE_CREATED_FROM_EMAIL} instead. Since v5.2.
     */
    public static final String ISSUE_CREATED_FROM_EMAIL = "ISSUE_CREATED_FROM_EMAIL";
    /**
     * @deprecated Use {@link MailThreadManager.MailAction#ISSUE_COMMENTED_FROM_EMAIL} instead. Since v5.2.
     */
    public static final String ISSUE_COMMENTED_FROM_EMAIL = "ISSUE_COMMENTED_FROM_EMAIL";

    /**
     * Indicates an action in response to an incoming email.
     * @since v5.2.3
     */
    public enum MailAction {ISSUE_CREATED_FROM_EMAIL, ISSUE_COMMENTED_FROM_EMAIL}

    /**
     * Remembers the given incoming MessageID.
     * <p>
     * This is used when someone emails JIRA and CCs some else, and that person in turn does a reply-all. Such an email
     * will not have the Issue Key in the subject.
     *
     * @param messageId The incoming Message-ID
     * @param senderAddress The sender
     * @param issue the issue that was affected (created or commented)
     * @param action Issue created or Issue commented
     *
     * @since v5.2.3
     */
    public void storeIncomingMessageId(String messageId, String senderAddress, Issue issue, MailAction action);

    /**
     * Remembers the given incoming MessageID.
     * <p>
     * As of v5.2, this should only be used for incoming messages, outgoing "Notification" messages are ignored.
     * Instead we craft a special Message-ID that we can parse the Issue ID out of if someone replies to the notification.
     *
     * @param type
     * @param source
     * @param emailAddress
     * @param messageId
     *
     * @deprecated Use {@link #storeIncomingMessageId(String, String, com.atlassian.jira.issue.Issue, com.atlassian.jira.mail.MailThreadManager.MailAction)} instead. Since v5.2.3.
     */
    public void createMailThread(String type, Long source, String emailAddress, String messageId);

    /**
     * Thread the given email which is related to the given issue.
     *
     * @param email the email to be sent
     * @param issueId the issue that the email is about
     *
     * @deprecated Use {@link #threadNotificationEmail(Email, com.atlassian.jira.issue.Issue)} instead. Since v5.2.
     */
    public void threadNotificationEmail(Email email, Long issueId);

    /**
     * Thread the given email which is related to the given issue.
     *
     * @param email the email to be sent
     * @param issue the issue that the email is about
     */
    public void threadNotificationEmail(Email email, Issue issue);

    /**
     * No longer used because we don't use store all outgoing notifications (see JRA-9979).
     *
     * @param eventTypeId eventTypeId
     *
     * @deprecated No longer used because we don't use store all outgoing notifications (see JRA-9979). Since v5.2.
     */
    public String getThreadType(Long eventTypeId);

    /**
     * Looks for an issue associated with the given message by inspecting the "In-Reply-To" header of the message.
     * <p>
     * Notifications sent from JIRA have a special form that allows us to parse out the Issue ID.
     * We also remember the incoming Message-IDs so we can tell if another recipient replies to that message.
     *
     * @param message message to analyse
     * @return associated issue or null if no issue is associated with this message.
     */
    @Nullable
    public Issue getAssociatedIssueObject(Message message);

    /**
     * Looks for an issue associated with the given message by inspecting the "Message-ID" header of the message.
     *
     * @param messageId Message-ID to be checked
     * @return Issue that is already associated with this Message-ID or null if none
     */
    @Nullable
    public Issue findIssueFromMessageId(String messageId);

    /**
     * Looks for an issue associated with the given message.
     * <p>
     * The "In-Reply-To" header of the message is parsed to see if it was sent by this JIRA server and if so we retrieve the Issue ID from it.
     * @param message message to analyse
     * @return associated issue or null if no issue is associated with this message.
     *
     * @deprecated use instead {@link #getAssociatedIssueObject} method. Since 26/11/2011
     */
    @Deprecated
    public GenericValue getAssociatedIssue(Message message);

    /**
     * Removes rows from NotificationInstance table associated with the given issue.
     * Used when we delete an issue.
     * @param issueId the issue
     * @return row count
     */
    public int removeAssociatedEntries(Long issueId);
}
