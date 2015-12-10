package com.atlassian.jira.mail;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;

import com.google.common.annotations.VisibleForTesting;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * Renders wikiContent using wiki renderer. Resulting markup is added to context with "htmlComment" key. Delegates
 * further rendering to MailServiceQueueItem by enqueuing it to mail queue.
 *
 * @since v6.1
 */
/*
 * JRA-32828 Required so that we can populate the Wiki render context from the mail-queue thread rather than the
 * thread that triggered the sending of the mail. In turn, that is required because the thread will get things like
 * baseUrl incorrect (it renders as a relative rather than absolute link).
 */
public class MentionMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(MentionMailQueueItem.class);

    private final User from;
    private final NotificationRecipient recipient;
    private final RendererManager rendererManager;
    private final Map<String, Object> context;
    private final MailQueue mailQueue;
    private final IssueRenderContext issueRenderContext;

    private final String subjectTemplatePath = "templates/email/subject/issuementioned.vm";


    public MentionMailQueueItem(final User from,
                                final NotificationRecipient recipient,
                                final Map<String, Object> context,
                                final IssueRenderContext issueRenderContext,
                                final RendererManager rendererManager,
                                final MailQueue mailQueue)
    {
        this.from = from;
        this.recipient = recipient;
        this.rendererManager = rendererManager;
        this.context = context;
        this.mailQueue = mailQueue;
        this.issueRenderContext = issueRenderContext;
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
    @Override
    public String getSubject()
    {
        final I18nHelper i18n = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
        final Map<String, Object> contextParams = Maps.newHashMap(context);
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

    @Override
    public void send() throws MailException
    {
        String format = recipient.getFormat();
        String bodyTemplatePath = "templates/email/" + format + "/issuementioned.vm";

        String comment = (String) context.get("comment");
        String content = rendererManager.getRenderedContent(
                AtlassianWikiRenderer.RENDERER_TYPE, comment, issueRenderContext);
        context.put("htmlComment", content);

        MailQueueItem item = new MailServiceQueueItemBuilder(from, recipient, subjectTemplatePath,
                bodyTemplatePath, context).buildQueueItem();
        mailQueue.addItem(item);
    }

    private String renderEmailSubject(Map<String, Object> contextParams)
    {
        return getTemplatingEngine().render(file(subjectTemplatePath)).applying(contextParams).asPlainText();
    }

    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
    }
}
