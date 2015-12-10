package com.atlassian.jira.mail.builder;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailQueueUtils;
import com.atlassian.jira.mail.TemplateContextFactory;
import com.atlassian.jira.mail.util.MimeTypes;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.mail.queue.SingleMailQueueItem;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import static com.atlassian.jira.template.TemplateSources.file;
import static com.atlassian.jira.template.TemplateSources.fragment;

/**
 * This should be used for creation of all email messages in JIRA, it allows to encapsulate the logic
 * of rendering emails.
 *
 * @since v6.0.8
 */
public class EmailBuilder
{
    private final Email email;
    private TemplateSource subjectTemplate;
    private TemplateSource bodyTemplate;
    private Set<BodyPart> attachments = Sets.newHashSet();
    private Map<String, Object> templateParameters;

    public EmailBuilder(Email email, String mimeType, Locale recipientLocale, IssueEvent issueEvent)
    {
        this.email = email;
        this.email.setMimeType(mimeType);

        final TemplateContextFactory templateContextFactory = ComponentAccessor.getComponent(TemplateContextFactory.class);
        final Map<String, Object> templateParams = templateContextFactory.getTemplateContext(recipientLocale, issueEvent).getTemplateParams();
        this.templateParameters = JiraMailQueueUtils.getContextParamsBody(templateParams);
    }

    public EmailBuilder(Email email, String mimeType, Locale recipientLocale)
    {
        this.email = email;
        this.email.setMimeType(mimeType);

        final TemplateContextFactory templateContextFactory = ComponentAccessor.getComponent(TemplateContextFactory.class);
        final Map<String, Object> templateParams = templateContextFactory.getTemplateContext(recipientLocale).getTemplateParams();
        this.templateParameters = JiraMailQueueUtils.getContextParamsBody(templateParams);
    }

    public EmailBuilder(Email email, NotificationRecipient notificationRecipient, IssueEvent event) {
        this(email, getMimeTypeForFormat(notificationRecipient), I18nBean.getLocaleFromUser(notificationRecipient.getUser()), event);
    }

    public EmailBuilder(Email email, NotificationRecipient notificationRecipient) {
        this(email, getMimeTypeForFormat(notificationRecipient), I18nBean.getLocaleFromUser(notificationRecipient.getUser()));
    }

    /**
     * Specified attachment will be added to rendered e-mail unchanged.
     * @param bodyPart
     * @return this builder for chain invocations
     */
    public EmailBuilder addAttachment(BodyPart bodyPart)
    {
        attachments.add(bodyPart);
        return this;
    }

    /**
     * Specified attachments will be added to rendered e-mail unchanged.
     * @param bodyParts
     * @return this builder for chain invocations
     */
    public EmailBuilder addAttachments(Collection<BodyPart> bodyParts)
    {
        attachments.addAll(bodyParts);
        return this;
    }

    /**
     * Render subject with specified Velocity template
     * This is optional
     * @param subjectTemplate Velocity template
     * @return this builder for chain invocations
     */
    public EmailBuilder withSubject(String subjectTemplate)
    {
        this.subjectTemplate = fragment(subjectTemplate);
        return this;
    }

    /**
     * Render subject with Velocity template from specified file
     * This is optional
     * @param subjectTemplatePath Path to Velocity file
     * @return this builder for chain invocations
     */
    public EmailBuilder withSubjectFromFile(String subjectTemplatePath)
    {
        this.subjectTemplate = file(subjectTemplatePath);
        return this;
    }

    public EmailBuilder withBody(String bodyTemplate)
    {
        this.bodyTemplate = fragment(bodyTemplate);
        return this;
    }

    public EmailBuilder withBodyFromFile(String bodyTemplatePath)
    {
        this.bodyTemplate = file(bodyTemplatePath);
        return this;
    }

    public EmailBuilder addParameters(Map<String, Object> templateParameters) {
        this.templateParameters.putAll(templateParameters);
        return this;
    }

    private void validate() {
        Preconditions.checkNotNull(subjectTemplate);
        Preconditions.checkNotNull(bodyTemplate);
        Preconditions.checkNotNull(templateParameters);
    }

    private EmailRenderer createEmailRenderer() {
        return new EmailRenderer(email, subjectTemplate, bodyTemplate, attachments, templateParameters);
    }


    /**
     * Invokes renderNow() and wraps the result in SingleMailQueueItem
     * @return
     * @throws MessagingException
     */
    public SingleMailQueueItem renderNowAsQueueItem() throws MessagingException
    {
        return new SingleMailQueueItem(renderNow());
    }

    /**
     * Blocking method - renders e-mail message in current thread
     * @return Rendered email
     * @throws MessagingException
     */
    public Email renderNow() throws MessagingException
    {
        return createEmailRenderer().render();
    }

    /**
     * Returns a MailQueueItem which will render email message during send
     * @return
     */
    public MailQueueItem renderLater() {

        return new RenderingMailQueueItem(createEmailRenderer());
    }

    private static String getMimeTypeForFormat(NotificationRecipient recipient)
    {
        if (recipient.isHtml())
        {
            return MimeTypes.Text.HTML;
        }
        else
        {
            return MimeTypes.Text.PLAIN;
        }
    }
}
