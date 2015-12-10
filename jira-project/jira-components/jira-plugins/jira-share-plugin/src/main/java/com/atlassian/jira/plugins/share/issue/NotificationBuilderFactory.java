package com.atlassian.jira.plugins.share.issue;

import java.util.Set;

import com.atlassian.jira.notification.AdhocNotificationService;
import com.atlassian.jira.notification.NotificationBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class NotificationBuilderFactory
{

    private final AdhocNotificationService adhocNotificationService;

    @Autowired
    public NotificationBuilderFactory(@ComponentImport final AdhocNotificationService adhocNotificationService)
    {
        this.adhocNotificationService = adhocNotificationService;
    }

    public NotificationBuilder createNotificationBuilder(String message, NotificationRecipient recipient, Set<NotificationRecipient> otherRecipients)
    {
        final NotificationBuilder notificationBuilder = adhocNotificationService.makeBuilder();
        addRecipientToNotification(notificationBuilder, recipient);

        final ImmutableMap<String, Object> params = createNotificationParams(message, recipient, otherRecipients);

        notificationBuilder.setTemplate("share-issue.vm");
        notificationBuilder.setTemplateParams(params);

        return notificationBuilder;
    }

    private ImmutableMap<String, Object> createNotificationParams(final String message, final NotificationRecipient recipient, final Set<NotificationRecipient> otherRecipients)
    {
        final ImmutableMap.Builder<String, Object> paramsBuilder = ImmutableMap.builder();
        if (StringUtils.isNotBlank(message))
        {
            paramsBuilder.put("comment", message);
            paramsBuilder.put("htmlComment", TextUtils.htmlEncode(message));  // required by templates/email/html/includes/fields/comment.vm
        }
        paramsBuilder.put("recipient", recipient);
        paramsBuilder.put("involvedUsers", otherRecipients);
        return paramsBuilder.build();
    }

    private void addRecipientToNotification(final NotificationBuilder notificationBuilder, final NotificationRecipient recipient)
    {
        if (recipient.getUser() != null)
        {
            notificationBuilder.addToUser(recipient.getUser().getName());
        }
        else if (recipient.getEmail() != null)
        {
            notificationBuilder.addToEmail(recipient.getEmail());
        }
    }
}
