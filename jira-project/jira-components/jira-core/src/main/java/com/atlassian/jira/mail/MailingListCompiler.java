/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.mail.builder.EmailBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.mail.MailThreader;
import com.atlassian.mail.queue.MailQueue;

import com.atlassian.mail.queue.SingleMailQueueItem;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

/**
 * Methods responsible for sending a notification email to a list of {@link NotificationRecipient}s.
 * <p/>
 * Notification format (eg. text/html) is taken into account as well as comment security levels.
 */
public class MailingListCompiler
{
    private static final Logger log = Logger.getLogger(MailingListCompiler.class);
    private static final String GETS_COMMENT_AND_ORIGINAL = "commentplusoriginal";
    private static final String GETS_COMMENT_NO_ORIGINAL = "commentnooriginal";
    private static final String NOCOMMENT = "nocomment";

    private final TemplateManager templateManager;
    private final ProjectRoleManager projectRoleManager;

    public MailingListCompiler(final TemplateManager templateManager, final ProjectRoleManager projectRoleManager)
    {
        this.templateManager = templateManager;
        this.projectRoleManager = projectRoleManager;
    }

    /**
     * Returns a comma-separated list of the given email addresses.
     * @param addresses email addresses
     * @return the list of addresses.
     * @deprecated just use Joiner.on(",").join(addresses)
     */
    @Deprecated
    public static String getEmailAddresses(Set<String> addresses)
    {
        // this appears to be dead code no longer called in production
        return Joiner.on(",").join(addresses);
    }

    /**
     * This function works out where the mail message has originated and then sets up the correct
     * parameters.  It allows comments to be hidden in the email from users that do not have permissions
     * to see them.
     *
     * @param recipients    Set of {@link NotificationRecipient}s
     * @param sender        sender
     * @param senderFrom    sender from
     * @param templateId    the velocity template ID
     * @param baseUrl       base url
     * @param contextParams map of context parameters
     * @param threader      mail threader
     * @throws VelocityException if notification compiler fails
     */
    public void sendLists(Set<NotificationRecipient> recipients, String sender, String senderFrom, Long templateId, String baseUrl, Map<String, Object> contextParams, MailThreader threader)
            throws VelocityException
    {
        NotificationCompiler compiler =
                new NotificationCompiler(recipients, sender, senderFrom, templateId, baseUrl, contextParams, threader);

        Object eventParams = contextParams.get("params");
        if (eventParams != null)
        {
            Object commentObject = contextParams.get("comment");
            Object worklogObject = contextParams.get("worklog");

            if (worklogObject != null)
            {
                Worklog worklog = (Worklog) worklogObject;
                ProjectRole roleLevel = worklog.getRoleLevel();
                String roleLevelName = roleLevel == null ? null : roleLevel.getName();

                Worklog originalWorklog = (Worklog) ((Map) eventParams).get(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER);

                String originalRoleLevelName = null;
                String originalGroupLevel = null;

                if (originalWorklog != null)
                {
                    ProjectRole originalRoleLevel = originalWorklog.getRoleLevel();
                    originalRoleLevelName = originalRoleLevel == null ? null : originalRoleLevel.getName();
                    originalGroupLevel = originalWorklog.getGroupLevel();
                }

                compiler.sendForEvent((Map) eventParams, worklog.getGroupLevel(), roleLevelName, originalGroupLevel, originalRoleLevelName);
            }
            else if (commentObject != null)
            {
                Comment comment = (Comment) commentObject;

                Comment originalComment = (Comment) ((Map) eventParams).get(CommentManager.EVENT_ORIGINAL_COMMENT_PARAMETER);

                String originalRoleLevelName = null;
                String originalGroupLevel = null;

                if (originalComment != null)
                {
                    ProjectRole originalRoleLevel = originalComment.getRoleLevel();
                    originalRoleLevelName = originalRoleLevel == null ? null : originalRoleLevel.getName();
                    originalGroupLevel = originalComment.getGroupLevel();
                }

                ProjectRole roleLevel = comment.getRoleLevel();
                String roleLevelName = roleLevel == null ? null : roleLevel.getName();
                compiler.sendForEvent((Map) eventParams, comment.getGroupLevel(), roleLevelName, originalGroupLevel, originalRoleLevelName);
            }
            else
            {
                log.debug("The event does not have a comment or a worklog. Sending to all recipients.");
                compiler.sendToAll();
            }
        }
        else
        {
            log.debug("Do not have any context params. Sending to all recipients.");
            compiler.sendToAll();
        }
    }

    /**
     * Group users by whether they get a comment, and format, eg:
     * { "nocomment": { "text": [joe@company.com, fred@company.com],
     * "html" : [john@company.com],
     * "custom" : [foo@company.com],
     * "comment" : { "text" : [bob@foo.com],
     * "html" : [joe@bar.com] }
     * }
     *
     * @param recipients          set of recipients
     * @param group               notification group level
     * @param role                notification role level
     * @param originalGroup       original group level
     * @param originalRole        original role level
     * @param issue               issue
     * @param sendOnlyWhenInGroup flag to override visibility permission
     * @return a map of recipient emails by type
     */
    private Map<String, Map<String, Set<NotificationRecipient>>> groupEmailsByType(
            Set<NotificationRecipient> recipients,
            String group,
            String role,
            String originalGroup,
            String originalRole,
            TemplateIssue issue,
            boolean sendOnlyWhenInGroup)
    {
        Map<String, Map<String, Set<NotificationRecipient>>> recipientEmailsByType = new HashMap<String, Map<String, Set<NotificationRecipient>>>();
        Map<String, Set<NotificationRecipient>> usersGettingCommentAndOriginal = new HashMap<String, Set<NotificationRecipient>>();
        Map<String, Set<NotificationRecipient>> usersGettingCommentNoOriginal = new HashMap<String, Set<NotificationRecipient>>();
        Map<String, Set<NotificationRecipient>> usersNotGettingComment = new HashMap<String, Set<NotificationRecipient>>();
        recipientEmailsByType.put(GETS_COMMENT_AND_ORIGINAL, usersGettingCommentAndOriginal);
        recipientEmailsByType.put(GETS_COMMENT_NO_ORIGINAL, usersGettingCommentNoOriginal);
        recipientEmailsByType.put(NOCOMMENT, usersNotGettingComment);

        for (NotificationRecipient recipient : recipients)
        {
            Map<String, Set<NotificationRecipient>> currentGroup;
            if (recipientHasVisibility(group, role, recipient, issue))
            {
                if (recipientHasVisibility(originalGroup, originalRole, recipient, issue))
                {
                    currentGroup = usersGettingCommentAndOriginal;
                }
                else
                {
                    currentGroup = usersGettingCommentNoOriginal;
                }
            }
            else if (!sendOnlyWhenInGroup)
            {
                // Not in the comment-level group, but it doesn't matter; send mail without comments
                currentGroup = usersNotGettingComment;
            }
            else
            {
                // Not in group and user has to be; ditch user
                continue;
            }
            String format = recipient.getFormat();
            Set<NotificationRecipient> currentUsers = currentGroup.get(format);
            if (currentUsers == null)
            {
                currentUsers = new HashSet<NotificationRecipient>();
                currentGroup.put(format, currentUsers);
            }
            currentUsers.add(recipient);
        }
        return recipientEmailsByType;
    }

    private boolean recipientHasVisibility(String group, String role, NotificationRecipient recipient, TemplateIssue issue)
    {
        return (group == null && role == null) ||
               (group != null && recipient.isInGroup(group)) ||
               (role != null && issue != null && isInRole(recipient, issue.getProjectObject(), role));
    }

    private boolean isInRole(final NotificationRecipient recipient, final Project project, final String role)
    {
        final User user = recipient.getUserRecipient();
        if (user == null)
        {
            // If we do not have a user then he/she cannot be part of a role. Return false
            return false;
        }

        // Retrieve a role by its name
        final ProjectRole projectRole = projectRoleManager.getProjectRole(role);
        // Return false if we could not find a role or if the user is not part of the role
        return projectRole != null && projectRoleManager.isUserInProjectRole(user, projectRole, project);
    }

    private void addMailsToQueue(final Set<NotificationRecipient> textRecipients, final Long templateId, final String format,
            final String baseUrl, final Map<String, Object> contextParams, final String sender, final String senderFrom,
            final MailThreader threader) throws VelocityException
    {
        final String subjectTemplate = templateManager.getTemplateContent(templateId, "subject");
        final String bodyTemplate = templateManager.getTemplateContent(templateId, format);

        if (!textRecipients.isEmpty())
        {
            final NotificationRecipientProcessor processor = new NotificationRecipientProcessor(textRecipients)
            {
                void processRecipient(final NotificationRecipient recipient) throws Exception
                {
                    final User recipientUser = recipient.getUserRecipient();

                    final String recipientEmail = recipient.getEmail();
                    // skip user with no e-mail address - http://jira.atlassian.com/browse/JRA-13558
                    if (recipientEmail == null || recipientEmail.length() == 0)
                    {
                        if (recipientUser == null) // case of e-mail address only
                        {
                            log.warn("Can not send e-mail, e-mail address ('" + recipientEmail + "') not set. Skipping...");
                        }
                        else
                        {
                            final String fullName = recipientUser.getDisplayName();
                            final String userName = recipientUser.getName();
                            log.warn("Can not send e-mail to '" + fullName + "' [" + userName + "],"
                                    + " e-mail address ('" + recipientEmail + "') not set. Skipping...");
                        }
                    }
                    else
                    {
                        // Pass the i18nHelper to the template - allows the email notification to be displayed in the language of the recipient
                        // Specify the translation file as an additional resource
                        final I18nHelper i18nBean = new I18nBean(recipientUser);
                        contextParams.put("i18n", i18nBean);
                        final IssueTemplateContext templateContext = (IssueTemplateContext) contextParams.get("context");

                        if (templateContext != null)
                        {
                            contextParams.put("eventTypeName", templateContext.getEventTypeName(i18nBean));
                        }

                        // Provide an OutlookDate formatter with the users locale
                        final OutlookDate formatter = new OutlookDate(i18nBean.getLocale());
                        contextParams.put("dateformatter", formatter);
                        contextParams.put("lfbean", LookAndFeelBean.getInstance(ComponentAccessor.getComponent(ApplicationProperties.class)));

                        // Place the recipient in the velocity context
                        if (recipientUser != null)
                        {
                            contextParams.put("recipient", recipientUser);
                        }

                        Email email = new Email(recipient.getEmail());
                        email.setFrom(sender);
                        email.setFromName(senderFrom);

                        final SingleMailQueueItem item = new EmailBuilder(email, recipient)
                                .withSubject(subjectTemplate)
                                .withBody(bodyTemplate)
                                .addParameters(contextParams)
                                .renderNowAsQueueItem();

                        item.setMailThreader(threader);
                        getMailQueue().addItem(item);
                    }
                }

                void handleException(final NotificationRecipient recipient, final Exception ex)
                {
                    if (recipient == null)
                    {
                        log.error("Failed adding mail, notification recipient was null", ex);
                    }
                    else
                    {
                        log.error("Failed adding mail for notification recipient: [email=" + recipient.getEmail()
                                + ", user=" + recipient.getUserRecipient() + "]", ex);
                    }
                }

            };
            processor.process();
        }
    }
    @VisibleForTesting
    MailQueue getMailQueue()
    {
        return ComponentAccessor.getMailQueue();
    }

    /**
     * Helper inner class.
     */
    private class NotificationCompiler
    {
        private final Set<NotificationRecipient> recipients;
        private final String sender;
        private final String senderFrom;
        private final Long templateId;
        private final String baseUrl;
        private final Map<String, Object> contextParams;
        private final MailThreader threader;

        public NotificationCompiler(Set<NotificationRecipient> recipients, String sender, String senderFrom, Long templateId, String baseUrl, Map<String, Object> contextParams, MailThreader threader)
        {
            this.recipients = recipients;
            this.sender = sender;
            this.senderFrom = senderFrom;
            this.templateId = templateId;
            this.baseUrl = baseUrl;
            this.contextParams = contextParams;
            this.threader = threader;
        }

        public void sendForEvent(Map eventParams, String groupLevel, String roleLevel, String originalGroupLevel, String originalRoleLevel)
                throws VelocityException
        {
            Object eventSource = eventParams.get("eventsource");
            //Check to see if the map has a value of where the event came from Workflow or Action
            if (eventSource != null)
            {
                //Check to see if the comment has a level
                log.debug("Do have an event source so from a comment or workflow");
                //Check what the event source was
                if (IssueEventSource.ACTION.equals(eventSource))
                {
                    log.debug("Event source is action");
                    sendLists(groupLevel, roleLevel, originalGroupLevel, originalRoleLevel, true);
                }
                else if (IssueEventSource.WORKFLOW.equals(eventSource))
                {
                    log.debug("Event source is workflow");
                    sendLists(groupLevel, roleLevel, originalGroupLevel, originalRoleLevel, false);
                }
                else
                {
                    log.debug("Event source is unknown, this should not happen");
                    sendNoLevelsIgnoreGroup();
                }
            }
            else
            {
                log.debug("No event source so must be from a subscription");
                sendNoLevelsIgnoreGroup();
            }
        }

        public void sendToAll() throws VelocityException
        {
            sendNoLevelsIgnoreGroup();
        }

        private void sendNoLevelsIgnoreGroup() throws VelocityException
        {
            sendLists(null, null, null, null, false);
        }

        /**
         * Analyzes recipients and adds relevantly formatted mails to the queue.
         *
         * @param groupLevel          group notification level for comments or worklogs, null otherwise
         * @param roleLevel           role notification level for comments or worklogs, null otherwise
         * @param originalGroupLevel  original notification group level
         * @param originalRoleLevel   original notification role level
         * @param sendOnlyWhenInGroup flag that indicates whether the users should get the notification if they are not in 'groupLevel'
         * @throws VelocityException if fails to add emails to the queue
         */
        private void sendLists(String groupLevel, String roleLevel, String originalGroupLevel, String originalRoleLevel, boolean sendOnlyWhenInGroup)
                throws VelocityException
        {
            // Note that issue can be null if a subscription is being sent
            TemplateIssue issue = (TemplateIssue) contextParams.get("issue");
            Map<String, Map<String, Set<NotificationRecipient>>> recipientEmailsByType = groupEmailsByType(recipients, groupLevel, roleLevel, originalGroupLevel, originalRoleLevel, issue, sendOnlyWhenInGroup);

            addEmailsToQueue(recipientEmailsByType.get(GETS_COMMENT_AND_ORIGINAL));

            // Remove the original comment for those who do not have the permission to see it
            // Showing the comment in the templates depends on the presence of this key in the context
            contextParams.remove("originalcomment");
            // Remove the original worklog for those who do not have the permission to see it
            contextParams.remove("originalworklog");
            addEmailsToQueue(recipientEmailsByType.get(GETS_COMMENT_NO_ORIGINAL));

            // Remove the comment from the context for those who do not have the permission to see it
            // Showing the comment in the templates depends on the presence of this key in the context
            contextParams.remove("comment");
            // NOTE: we do not need to remove the worklog from the context because it is always invoked with
            // sendOnlyWhenInGroup as true which means that in the case of the user not being able to see the
            // worklog we will not send any mail.
            addEmailsToQueue(recipientEmailsByType.get(NOCOMMENT));
        }

        /**
         * Gets the user sets by each format and adds mails to the queue
         *
         * @param userSets a map that maps format[String] to users[Set]
         * @throws VelocityException if fails to add emails to the queue
         */
        private void addEmailsToQueue(Map<String, Set<NotificationRecipient>> userSets) throws VelocityException
        {
            for (Map.Entry<String, Set<NotificationRecipient>> entry : userSets.entrySet())
            {
                String format = entry.getKey();
                Set<NotificationRecipient> users = entry.getValue();
                addMailsToQueue(users, templateId, format, baseUrl, contextParams, sender, senderFrom, threader);
            }
        }
    }
}
