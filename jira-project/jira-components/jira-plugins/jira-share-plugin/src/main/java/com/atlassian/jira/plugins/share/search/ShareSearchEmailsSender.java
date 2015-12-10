package com.atlassian.jira.plugins.share.search;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.notification.JiraNotificationReason;
import com.atlassian.jira.notification.NotificationFilterContext;
import com.atlassian.jira.notification.NotificationFilterManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.plugins.share.util.NotificationRecipientUtil;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ShareSearchEmailsSender
{
    public static final String SAVED_SEARCH_TEMPLATE = "share-saved-search.vm";
    public static final String SHARE_JQL_TEMPLATE = "share-jql-search.vm";
    public static final String BODY_TEMPLATE_PATH = "templates/email/";
    public static final String SUBJECT_TEMPLATE_PATH = "templates/email/subject/";
    public static final char PATH_SEPARATOR = '/';
    private final MailService mailService;
    private final ShareManager shareManager;
    private final NotificationFilterManager notificationFilterManager;
    private final NotificationRecipientUtil notificationRecipientUtil;

    @Autowired
    public ShareSearchEmailsSender(
            @ComponentImport MailService mailService,
            @ComponentImport ShareManager shareManager,
            @ComponentImport NotificationFilterManager notificationFilterManager,
            final NotificationRecipientUtil notificationRecipientUtil)
    {
        this.mailService = mailService;
        this.shareManager = shareManager;
        this.notificationFilterManager = notificationFilterManager;
        this.notificationRecipientUtil = notificationRecipientUtil;
    }

    public void sendShareSearchEmails(ShareService.ValidateShareSearchRequestResult result, Map<String, Object> params)
    {
        ApplicationUser from = result.getUser();
        ShareBean shareBean = result.getShareBean();
        SearchRequest searchRequest = result.getSearchRequest();
        List<NotificationRecipient> recipients = notificationRecipientUtil.getRecipients(shareBean);

        // Each email might be of two kinds - if it's to a user with permission to see the specified saved search (if
        // specified), then share the filter. If not, share the JQL.
        Iterable<NotificationRecipient> filteredRecipients = filterRecipients(recipients);

        for (NotificationRecipient recipient : filteredRecipients)
        {
            final Set<NotificationRecipient> shareRecipients = notificationRecipientUtil.filterOutAuthorAndReceiver(from, recipients, recipient);
            setSharedWithParamsForRecipient(recipient, shareRecipients, params);

            String template = getAppropriateTemplateName(searchRequest, recipient);
            String subjectTemplatePath = SUBJECT_TEMPLATE_PATH + template;
            String bodyTemplatePath = BODY_TEMPLATE_PATH + recipient.getFormat() + PATH_SEPARATOR + template;
            mailService.sendRenderedMail(from.getDirectoryUser(), recipient, subjectTemplatePath, bodyTemplatePath, params);
        }
    }

    private Iterable<NotificationRecipient> filterRecipients(final List<NotificationRecipient> recipients)
    {
        final NotificationFilterContext context = notificationFilterManager.makeContextFrom(JiraNotificationReason.SHARED);
        return Iterables.filter(recipients, new Predicate<NotificationRecipient>()
        {
            @Override
            public boolean apply(@Nullable final NotificationRecipient input)
            {
                return !notificationFilterManager.filtered(input, context);
            }
        });
    }

    private String getAppropriateTemplateName(final SearchRequest searchRequest, final NotificationRecipient recipient)
    {
        boolean userAllowedToSeeSearchResult = isUserAllowedToSeeSearchResult(searchRequest, recipient);
        return userAllowedToSeeSearchResult ? SAVED_SEARCH_TEMPLATE : SHARE_JQL_TEMPLATE;
    }

    private boolean isUserAllowedToSeeSearchResult(final SearchRequest searchRequest, final NotificationRecipient recipient)
    {
        ApplicationUser userRecipient = recipient.getUser();
        return searchRequest != null && userRecipient != null && shareManager.isSharedWith(userRecipient, searchRequest);
    }

    private void setSharedWithParamsForRecipient(final NotificationRecipient recipient, final Set<NotificationRecipient> shareWithRecipients, final Map<String, Object> params)
    {
        params.put("recipient", recipient);
        params.put("involvedUsers", Collections.unmodifiableSet(shareWithRecipients));
    }

}
