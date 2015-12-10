package com.atlassian.jira.mail.builder;

import java.util.Map;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.CssInliner;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailQueueUtils;
import com.atlassian.jira.mail.util.MailAttachmentsManager;
import com.atlassian.jira.mail.util.MailAttachmentsManagerImpl;
import com.atlassian.jira.mail.util.MimeTypes;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

/**
 * This class should be used to render all email messages in JIRA.
 * It ensures that TemplateContext contains AttachmentsManager which allow to add
 * image attachments from TemplateContext
 */
class EmailRenderer
{

    private static final Logger log = Logger.getLogger(EmailRenderer.class);

    private final Email email;
    private final TemplateSource subjectTemplate;
    private final TemplateSource bodyTemplate;
    private final Set<BodyPart> attachments;
    private final Map<String, Object> templateParameters;

    public EmailRenderer(final Email email, final TemplateSource subjectTemplate, final TemplateSource bodyTemplate, final Set<BodyPart> attachments, final Map<String, Object> templateParameters)
    {
        this.email = email;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.attachments = attachments;
        this.templateParameters = templateParameters;
    }

    /**
     * Returns current state of Email message. If render() was not yet invoked then it returns
     * not rendered e-mail
     * @return
     */
    public Email getEmail() {
        return email;
    }

    /**
     * This is the subject as displayed in the Mail Queue Admin page. The subject is displayed in the preference
     * language of the current user viewing items to be sent (i.e. different from items CURRENTLY being sent).
     * <p/>
     * The subject will be displayed in the preference language of the mail recipient once the mail is actually being
     * sent. When the mail is being sent, it is a SingleMailQueueItem.
     *
     * @return String the subject as displayed on the mail queue admin page
     */
    public String getSubject()
    {
        final I18nHelper i18n = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
        final Map<String, Object> contextParams = JiraMailQueueUtils.getContextParamsBody(templateParameters);
        contextParams.put("i18n", i18n);
        try
        {
            return renderEmailSubject(contextParams);
        }
        catch (VelocityException e)
        {
            log.error("Could not determine e-mail subject", e);
            throw new RuntimeException(e);
        }
    }

    private String renderEmailSubject(Map<String, Object> contextParams)
    {
        return getTemplatingEngine().render(subjectTemplate).applying(contextParams).asPlainText();
    }

    private VelocityTemplatingEngine getTemplatingEngine()
    {
        return ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
    }

    private void renderEmailBody(final Map<String, Object> contextParams) throws MessagingException
    {
        String renderedMailBody;

        final MailAttachmentsManager attachmentsManager = createAttachmentsManager();
        contextParams.put("attachmentsManager", attachmentsManager);

        if (email.getMimeType().equals(MimeTypes.Text.HTML))
        {
            final CssInliner cssInliner = ComponentAccessor.getComponent(CssInliner.class);
            //Email messages are better rendered as PlainText due to passing HTML content in contextParams
            renderedMailBody = getTemplatingEngine().render(bodyTemplate).applying(contextParams).asPlainText();
            renderedMailBody = cssInliner.applyStyles(renderedMailBody);
        }
        else
        {
            renderedMailBody = getTemplatingEngine().render(bodyTemplate).applying(contextParams).asPlainText();
        }

        if (attachmentsManager.getAttachmentsCount() > 0) {
            final String contentType = String.format("%s; charset=%s", email.getMimeType(), email.getEncoding());
            final Multipart multiPart = buildMailWithAttachments(attachmentsManager, renderedMailBody, contentType);
            email.setMultipart(multiPart);
        }
        else
        {
            email.setBody(renderedMailBody);
        }
    }

    private MailAttachmentsManager createAttachmentsManager()
    {
        final AvatarManager avatarManager = ComponentAccessor.getComponent(AvatarManager.class);
        final AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
        final UserManager userManager = ComponentAccessor.getComponent(UserManager.class);
        final ApplicationProperties applicationProperties = ComponentAccessor.getComponent(ApplicationProperties.class);
        return new MailAttachmentsManagerImpl(avatarService, userManager, avatarManager, applicationProperties);
    }

    private Multipart buildMailWithAttachments(MailAttachmentsManager attachmentsManager, String text, String textContentMimeType)
            throws MessagingException
    {
        final MimeMultipart multipart = new MimeMultipart("related");

        final BodyPart textPart = new MimeBodyPart();
        textPart.setContent(text, textContentMimeType);
        multipart.addBodyPart(textPart);

        final Iterable<BodyPart> bodyParts = Iterables.concat(attachmentsManager.buildAttachmentsBodyParts(), attachments);
        for (BodyPart bodyPart : bodyParts)
        {
            multipart.addBodyPart(bodyPart);
        }
        return multipart;
    }

    /**
     * Renders email's body and subject. Rendered email message will contain any required attachments
     * @return rendered email
     * @throws MessagingException
     */
    public Email render() throws MessagingException
    {
        email.setSubject(renderEmailSubject(templateParameters));
        renderEmailBody(templateParameters);
        return email;
    }
}
