package com.atlassian.jira.plugins.share.issue;

import java.util.List;
import java.util.Set;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.AdhocNotificationService;
import com.atlassian.jira.notification.NotificationBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.plugins.share.event.ShareIssueEvent;
import com.atlassian.jira.plugins.share.util.NotificationRecipientUtil;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.jira.notification.AdhocNotificationService.ValiationOption.CONTINUE_ON_NO_RECIPIENTS;

@Component
public class ShareIssueService
{
    private static final Logger log = Logger.getLogger(ShareIssueService.class);
    private final EventPublisher eventPublisher;
    private final AdhocNotificationService adhocNotificationService;
    private final NotificationBuilderFactory notificationBuilderFactory;
    private final NotificationRecipientUtil notificationRecipientUtil;

    @Autowired
    public ShareIssueService(
            @ComponentImport EventPublisher eventPublisher,
            @ComponentImport AdhocNotificationService adhocNotificationService,
            final NotificationBuilderFactory notificationBuilderFactory,
            final NotificationRecipientUtil notificationRecipientUtil)
    {
        this.eventPublisher = eventPublisher;
        this.adhocNotificationService = adhocNotificationService;
        this.notificationBuilderFactory = notificationBuilderFactory;
        this.notificationRecipientUtil = notificationRecipientUtil;
    }

    public void shareIssue(ShareService.ValidateShareIssueResult result)
    {
        final Issue issue = result.getIssue();
        final ApplicationUser from = result.getUser();
        final ShareBean shareBean = result.getShareBean();

        sendShareIssueEmails(result);

        eventPublisher.publish(new ShareIssueEvent(issue, from.getDirectoryUser(), shareBean.getUsernames(), shareBean.getEmails(), shareBean.getMessage()));
    }

    private void sendShareIssueEmails(ShareService.ValidateShareIssueResult result)
    {
        final List<NotificationRecipient> recipients = notificationRecipientUtil.getRecipients(result.getShareBean());
        for (final NotificationRecipient recipient : recipients)
        {
            sendShareIssueMail(result, recipients, recipient);
        }
    }

    private void sendShareIssueMail(ShareService.ValidateShareIssueResult result, List<NotificationRecipient> allRecipients, NotificationRecipient currentRecipient)
    {
        final Set<NotificationRecipient> sharedWithRecipients = notificationRecipientUtil.filterOutAuthorAndReceiver(result.getUser(), allRecipients, currentRecipient);
        final NotificationBuilder notificationBuilder = notificationBuilderFactory.createNotificationBuilder(result.getShareBean().getMessage(), currentRecipient, sharedWithRecipients);

        final AdhocNotificationService.ValidateNotificationResult validateNotificationResult = adhocNotificationService.validateNotification(
                notificationBuilder,
                result.getUser().getDirectoryUser(),
                result.getIssue(),
                CONTINUE_ON_NO_RECIPIENTS);

        if (validateNotificationResult.isValid())
        {
            adhocNotificationService.sendNotification(validateNotificationResult);
        }
        else
        {
            log.warn("Errors in Notification data: " + validateNotificationResult.getErrorCollection().getErrorMessages());
        }
    }

}
