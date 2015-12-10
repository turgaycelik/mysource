package com.atlassian.jira.mail;

import java.util.Locale;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mail.builder.EmailBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.I18nBean;

import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.collect.Maps;

public class MailServiceQueueItemBuilder
{
    private final NotificationRecipient recipient;
    private final Map<String, Object> context;
    private final User replyTo;
    private final String subjectTemplatePath;
    private final String bodyTemplatePath;

    public MailServiceQueueItemBuilder(User replyTo, NotificationRecipient recipient, String subjectTemplatePath,
            String bodyTemplatePath, Map<String, Object> context)
    {
        this.replyTo = replyTo;
        this.recipient = recipient;
        this.subjectTemplatePath = subjectTemplatePath;
        this.bodyTemplatePath = bodyTemplatePath;
        this.context = context;
    }

    public MailQueueItem buildQueueItem()
    {
        String projectEmail = null;
        IssueEvent issueEvent = null;
        if (context.containsKey("issue"))
        {
            final Issue issue = (Issue) context.get("issue");
            projectEmail = JiraMailUtils.getProjectEmailFromIssue(issue);

            // Need to create an IssueEvent because currently that's what TemplateContextFactory expects. Not touching that!
            issueEvent = new IssueEvent(issue, Maps.newHashMap(), replyTo, 0L);
        }

        Email email = new Email(recipient.getEmail());
        email.setFrom(projectEmail);
        email.setFromName(JiraMailUtils.getFromNameForUser(replyTo));
        email.setReplyTo(replyTo.getEmailAddress());

        final EmailBuilder emailBuilder;
        if(issueEvent != null) {
            emailBuilder = new EmailBuilder(email, recipient, issueEvent);
        } else {
            emailBuilder = new EmailBuilder(email, recipient);
        }
        return emailBuilder
                .withSubjectFromFile(subjectTemplatePath)
                .withBodyFromFile(bodyTemplatePath)
                .addParameters(context)
                .renderLater();
    }
}
