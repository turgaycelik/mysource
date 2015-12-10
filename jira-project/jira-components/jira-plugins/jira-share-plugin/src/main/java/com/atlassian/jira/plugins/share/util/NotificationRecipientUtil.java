package com.atlassian.jira.plugins.share.util;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class NotificationRecipientUtil
{

    private static final Logger log = Logger.getLogger(NotificationRecipientUtil.class);
    private final UserManager userManager;

    @Autowired
    public NotificationRecipientUtil(@ComponentImport UserManager userManager)
    {
        this.userManager = userManager;
    }

    public Set<NotificationRecipient> filterOutAuthorAndReceiver(final ApplicationUser authorUser, final List<NotificationRecipient> allRecipients, final NotificationRecipient notificationReceiver)
    {
        return Sets.filter(Sets.newHashSet(allRecipients), new Predicate<NotificationRecipient>()
        {
            @Override
            public boolean apply(@Nullable final NotificationRecipient recipient)
            {
                return isNotNotificationReceiver(recipient) && !isAuthor(recipient);
            }

            private boolean isAuthor(final NotificationRecipient recipient)
            {
                if (authorUser == null || recipient == null)
                {
                    return false;
                }
                else if (recipient.getUser() != null)
                {
                    final ApplicationUser user = recipient.getUser();
                    return authorUser.equals(user);
                }
                else if ((recipient.getEmail() != null) && (authorUser.getEmailAddress() != null))
                {
                    return recipient.getEmail().equals(authorUser.getEmailAddress());
                }
                return false;
            }

            private boolean isNotNotificationReceiver(final NotificationRecipient recipient)
            {
                return recipient != notificationReceiver;
            }
        });
    }

    public List<NotificationRecipient> getRecipients(ShareBean shareBean)
    {
        List<NotificationRecipient> recipients = newArrayList();

        final List<NotificationRecipient> userNamesRecipients = getRecipientsFromUserNames(shareBean);
        final List<NotificationRecipient> emailRecipients = getRecipientsFromEmails(shareBean);

        recipients.addAll(userNamesRecipients);
        recipients.addAll(emailRecipients);

        return recipients;
    }

    private List<NotificationRecipient> getRecipientsFromUserNames(final ShareBean shareBean)
    {
        final List<NotificationRecipient> recipients = Lists.newArrayList();
        if (shareBean.getUsernames() != null)
        {
            for (String toUsername : shareBean.getUsernames())
            {
                ApplicationUser user = userManager.getUserByName(toUsername);
                if (user != null)
                {
                    recipients.add(new NotificationRecipient(user));
                }
                else
                {
                    // The front should normally catch this, more likely someone hit the REST resource directly.
                    log.warn("No user found for name: " + toUsername);
                }
            }
        }
        return recipients;
    }

    private List<NotificationRecipient> getRecipientsFromEmails(final ShareBean shareBean)
    {
        final List<NotificationRecipient> recipients = Lists.newArrayList();
        if (shareBean.getEmails() != null)
        {
            for (String toEmail : shareBean.getEmails())
            {
                recipients.add(new NotificationRecipient(toEmail));
            }
        }
        return recipients;
    }

}
