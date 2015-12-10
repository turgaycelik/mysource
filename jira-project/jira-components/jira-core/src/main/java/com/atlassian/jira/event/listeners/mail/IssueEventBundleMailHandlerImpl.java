package com.atlassian.jira.event.listeners.mail;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.issue.DelegatingJiraIssueEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.JiraIssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeEntity;

import com.google.common.collect.ImmutableSet;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * Default implementation for {@link IssueEventBundleMailHandler}
 */
public class IssueEventBundleMailHandlerImpl implements IssueEventBundleMailHandler
{
    private static final Logger log = Logger.getLogger(IssueEventBundleMailHandlerImpl.class);

    private final NotificationSchemeManager notificationSchemeManager;
    private final IssueEventMailNotifier mailNotifier;

    public IssueEventBundleMailHandlerImpl(final NotificationSchemeManager notificationSchemeManager,
            final IssueEventMailNotifier mailNotifier)
    {
        this.notificationSchemeManager = notificationSchemeManager;
        this.mailNotifier = mailNotifier;
    }

    @Override
    public void handle(@Nonnull final IssueEventBundle issueEventBundle)
    {
        if (!issueEventBundle.doesSendEmailNotification())
        {
            return;
        }

        Set<NotificationRecipient> recipientsAlreadyNotified = new HashSet<NotificationRecipient>();

        for (JiraIssueEvent event : issueEventBundle.getEvents())
        {
            if (event instanceof DelegatingJiraIssueEvent)
            {
                IssueEvent issueEvent = ((DelegatingJiraIssueEvent) event).asIssueEvent();
                if (!issueEvent.isSendMail())
                {
                    log.debug("Not sending email for event " + issueEvent);
                    continue;
                }
                List<SchemeEntity> schemeEntities = getSchemeEntities(issueEvent.getProject(), issueEvent);
                Set<NotificationRecipient> recipientsNotifiedForEvent = mailNotifier.generateNotifications(
                        schemeEntities, issueEvent, ImmutableSet.copyOf(recipientsAlreadyNotified));
                recipientsAlreadyNotified.addAll(recipientsNotifiedForEvent);
            }
        }
    }

    @Nonnull
    private List<SchemeEntity> getSchemeEntities(@Nonnull final Project project, @Nonnull final IssueEvent event)
    {
        try
        {
            return notificationSchemeManager.getNotificationSchemeEntities(project, event.getEventTypeId());
        }
        catch (GenericEntityException e)
        {
            log.error("There was an error accessing the notification scheme for the project: " + project.getKey() + ".", e);
        }
        return Collections.emptyList();
    }
}
