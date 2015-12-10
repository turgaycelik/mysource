/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class MailThreadManagerImpl implements MailThreadManager
{
    private static final Logger log = Logger.getLogger(MailThreadManagerImpl.class);

    private final OfBizDelegator ofBizDelegator;
    private final IssueManager issueManager;
    private final EventTypeManager eventTypeManager;

    public MailThreadManagerImpl(OfBizDelegator ofBizDelegator, IssueManager issueManager, EventTypeManager eventTypeManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.issueManager = issueManager;
        this.eventTypeManager = eventTypeManager;
    }

    @Override
    public void storeIncomingMessageId(String messageId, String senderAddress, Issue issue, MailAction action)
    {
        Assertions.notNull("messageId", messageId);
        Assertions.notNull("issue", issue);
        Assertions.notNull("action", action);
        // We kinda don't care about the senderAddress as it is not really used.

        FieldMap fields = new FieldMap();
        fields.put("type", action.toString());
        fields.put("source", issue.getId());
        fields.put("email", senderAddress);
        fields.put("messageid", messageId);

        ofBizDelegator.createValue("NotificationInstance", fields);
    }

    @Override
    public void createMailThread(String type, Long source, String emailAddress, String messageId)
    {
        // Check that the message id is passed, otherwise there is nothing to record
        if (messageId == null || messageId.trim().isEmpty())
        { return; }
        // Check that this is a valid incoming message type
        if (type != null && (type.equals(ISSUE_CREATED_FROM_EMAIL) || type.equals(ISSUE_COMMENTED_FROM_EMAIL)))
        {
            FieldMap fields = new FieldMap();
            fields.put("type", type);
            fields.put("source", source);
            fields.put("email", emailAddress);
            fields.put("messageid", messageId);

            try
            {
                ofBizDelegator.createValue("NotificationInstance", fields);
            }
            catch (DataAccessException e)
            {
                log.warn("Unable to store messageId details for email for issue '" + source + "' emailAddress '" + emailAddress + "'.", e);
            }
        }
    }

    @Override
    public void threadNotificationEmail(Email email, Long issueId)
    {
        Issue issue = issueManager.getIssueObject(issueId);
        if (issue == null)
            throw new IllegalArgumentException("Invalid issueId " + issueId);

        threadNotificationEmail(email, issue);
    }

    @Override
    public void threadNotificationEmail(Email email, Issue issue)
    {
        // JRADEV-14311 Craft a fake Message-ID that all notifications will set as "In-Reply-To"
        List<String> replyToIds = JiraMailUtils.getReplyToIds(issue);
        email.setInReplyTo(replyToIds.get(0));
        // JRA-29761 Also add "References" header for Outlook to do threading
        StringBuilder replyToIdsStringBuilder = new StringBuilder();
        for (int i = 0; i < replyToIds.size(); i++)
        {
            if (i > 0)
                replyToIdsStringBuilder.append(" ");
            replyToIdsStringBuilder.append(replyToIds.get(i));
        }
        email.addHeader("References", replyToIdsStringBuilder.toString());
    }

    @SuppressWarnings ("deprecation")
    @Override
    public String getThreadType(Long eventTypeId)
    {
        if (EventType.ISSUE_CREATED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_CREATED;
        }
        else if (EventType.ISSUE_ASSIGNED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_ASSIGNED;
        }
        else if (EventType.ISSUE_RESOLVED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_RESOLVED;
        }
        else if (EventType.ISSUE_CLOSED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_CLOSED;
        }
        else if (EventType.ISSUE_REOPENED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_REOPENED;
        }
        else if (EventType.ISSUE_COMMENTED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_COMMENTED;
        }
        else if (EventType.ISSUE_COMMENT_EDITED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_COMMENT_EDITED;
        }
        else if (EventType.ISSUE_DELETED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_DELETED;
        }
        else if (EventType.ISSUE_UPDATED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_UPDATED;
        }
        else if (EventType.ISSUE_WORKLOGGED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKLOGGED;
        }
        else if (EventType.ISSUE_WORKLOG_UPDATED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKLOG_UPDATED;
        }
        else if (EventType.ISSUE_WORKLOG_DELETED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKLOG_DELETED;
        }
        else if (EventType.ISSUE_WORKSTARTED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKSTARTED;
        }
        else if (EventType.ISSUE_WORKSTOPPED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKSTOPPED;
        }
        else if (EventType.ISSUE_GENERICEVENT_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_GENERICEVENT;
        }
        else
        {
            if (eventTypeManager.isEventTypeExists(eventTypeId))
            {
                return NOTIFICATION_KEY + eventTypeId;
            }
            else
            {
                throw new IllegalArgumentException("Unable to thread this notification as the event type " + eventTypeId + " is unknown.");
            }
        }
    }

    @Override
    public GenericValue getAssociatedIssue(Message message)
    {
        final Issue issue = getAssociatedIssueObject(message);
        if (issue == null)
        {
            return null;
        }
        return issue.getGenericValue();
    }

    @Nullable
    @Override
    public Issue getAssociatedIssueObject(Message message)
    {
        try
        {
            // First check the In-Reply-To header
            final String[] messageIds = message.getHeader("In-Reply-To");
            if (messageIds != null && messageIds.length > 0)
            {
                for (String messageId : messageIds)
                {
                    Issue issue = getAssociatedIssueFromMessageId(messageId);
                    if (issue != null)
                        return issue;
                }
            }
            else
            {
                log.debug("No In-Reply-To header found");
            }
            // JRA-11181: Also check the "References" header; some clients don't send In-Reply-To
            // Going to iterate backwards as it seems more correct to check later Message-IDs first.
            final String[] values = message.getHeader("References");
            if (values != null && values.length > 0)
            {
                for (String value : values)
                {
                    for (String messageId : extractMessageIdsFromReferences(value))
                    {
                        Issue issue = getAssociatedIssueFromMessageId(messageId);
                        if (issue != null)
                            return issue;
                    }
                }
            }
            else
            {
                log.debug("No References header found for message '" + message.getSubject() + "'");
            }
            return null;
        }
        catch (MessagingException e)
        {
            log.error("Error occurred while determining message id of an e-mail message.", e);
            return null;
        }
    }

    private List<String> extractMessageIdsFromReferences(String value)
    {
        List<String> messageIds = new ArrayList<String>();
        if (value == null)
            return messageIds;
        // Looks like eg "<JIRA.123.1340592806000.4.1347495168235@marky2>\r\n <50512531.80500@atlassian.com> <50512597.808@atlassian.com>"
        // Find message-IDs between angle brackets
        int nextMessageIdIndex = value.indexOf('<');
        while (nextMessageIdIndex > -1)
        {
            final int endIndex = value.indexOf('>', nextMessageIdIndex);
            // JRA-31374 Allow for bad header value (missing right angle-bracket)
            if (endIndex == -1)
            {
                // Invalid value because it is missing the '>'
                // Ignore this value and leave the loop
                nextMessageIdIndex = -1;
            }
            else
            {
                // We include the leading and trailing angle brackets because that is what the underlying code expects
                messageIds.add(value.substring(nextMessageIdIndex, endIndex + 1));
                nextMessageIdIndex = value.indexOf('<', endIndex);
            }
        }
        return messageIds;
    }

    private Issue getAssociatedIssueFromMessageId(String messageId)
    {
        // Some e-mail clients append extra information to the message's massage id when
        // setting the in-reply-to message header. We need to strip that extra information
        final int index = messageId.indexOf(";");
        if (index > 0)
        {
            messageId = messageId.substring(0, index);
        }

        // The message has a message id. See if we can find any issues associated with the message-id.
        // JRADEV-14311: First we try to parse the Message-ID assuming the new custom Message-ID format
        if (messageId.startsWith("<JIRA."))
        {
            return parseIssueFromMessageId(messageId);
        }
        else
        {
            // Try to match legacy Message-ID in the NotificationInstance table - eventually we will kill this ... JRADEV-14311
            return findIssueFromMessageId(messageId);
        }
    }

    private Issue parseIssueFromMessageId(final String messageId)
    {
        // This Message-ID is created by JiraMailThreader.getCustomMessageId(Email)
        String[] sections = messageId.split("\\.");
        // section 0 is "<JIRA"
        // section 1 is the Issue ID
        // section 2 is the issue created date (safety mechanism for multiple servers)
        Long issueId;
        Long createdDate;
        try
        {
            issueId = new Long(sections[1]);

            // JRA-35238: the createdDate of an issue can be set to null in the DB, which puts it into a
            // corrupted state that we need to check for. If at some point an issue's createdDate does become
            // null, we allow for this and simply use the other information in the messageId to connect and email
            // with an issue.
            if ("null".equals(sections[2]))
            {
                createdDate = null;
            }
            else
            {
                createdDate = Long.parseLong(sections[2]);
            }
        }
        catch (RuntimeException ex)
        {
            log.error("Unable to parse incoming In-Reply-To header " + messageId);
            return null;
        }
        Issue issue = issueManager.getIssueObject(issueId);
        if (issue == null)
        {
            // JDEV-26014: The issue may have been deleted or perhaps the email originated on another instance.
            return null;
        }

        final Timestamp created = issue.getCreated();
        if (createdDate == null || created == null)
        {
            // In either case, we are processing a reply to an issue that has a corrupted created date in the jiraissue
            // table in the db, so we will consider this to be a reply to that issue.
            return issue;
        }
        // JRA-30293 Strip milliseconds because these can get zero-ed out on some DBs due to JDBC / javax.sql.Timestamp
        // New emails will actually get milliseconds zero-ed out due to JRA-37319, but continue to strip for old emails.
        if (created.getTime() / 1000 == createdDate / 1000)
        {
            // passed safety mechanism, the create date in both the email and the database match so we will reply to
            // this issue.
            return issue;
        }
        else
        {
            log.warn("Received In-Reply-To header " + messageId + " but issue " + issue.getKey() +
                    " does not match incoming creation date - assuming this is from another server and ignoring.");
            return null;
        }
    }

    @Override
    public Issue findIssueFromMessageId(String messageId)
    {
        final List notificationInstanceGVs = ofBizDelegator.findByAnd("NotificationInstance", FieldMap.build("messageid", messageId));
        if (notificationInstanceGVs == null || notificationInstanceGVs.isEmpty())
        {
            // Cannot find any associated issues with the message id
            log.debug("Cannot find any associated issues with message id '" + messageId + "'.");
            return null;
        }
        else
        {
            // Found records with associated issue
            GenericValue notificationInstanceGV = (GenericValue) notificationInstanceGVs.get(0);
            final Long issueId = notificationInstanceGV.getLong("source");

            // Retrieve the issue with the given issue id
            return issueManager.getIssueObject(issueId);
        }
    }

    @Override
    public int removeAssociatedEntries(Long issueId)
    {
        return ofBizDelegator.removeByAnd("NotificationInstance", FieldMap.build("source", issueId));
    }
}
