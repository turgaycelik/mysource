package com.atlassian.jira.web.action.user;

import java.util.Collection;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.builder.EmailBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;

import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ContactAdministrators extends JiraWebActionSupport
{
    private static final String EMAIL_TEMPLATES = "templates/email";
    private static final String EMAIL_TEMPLATE = "contactadministrator.vm";
    private static final Integer PADSIZE = 20;

    private static class MimeTypes
    {
        static final String TEXT_HTML = "text/html";
        static final String TEXT_PLAIN = "text/plain";
    }

    private static final String FORM_DISABLED_STATE = "modebreach";

    private final UserPropertyManager userPropertyManager;
    private final RendererManager rendererManager;
    private final MailQueue mailQueue;
    private final UserUtil userUtil;
    private final JiraContactHelper jiraContactHelper;

    private String to;
    private String replyTo;
    private String subject;
    private String details;

    public ContactAdministrators(final RendererManager rendererManager, final MailQueue mailQueue,
            final UserUtil userUtil,
            final UserPropertyManager userPropertyManager,
            final JiraContactHelper jiraContactHelper
            )
    {
        this.rendererManager = rendererManager;
        this.mailQueue = mailQueue;
        this.userUtil = userUtil;
        this.userPropertyManager = userPropertyManager;
        this.jiraContactHelper = jiraContactHelper;

        to = getText("admin.global.permissions.administer");
        if (getLoggedInUser() != null)
        {
            replyTo = getLoggedInUser().getEmailAddress();
        }

    }

    public String doDefault() throws Exception
    {
        if (!getShouldDisplayForm())
        {
            return FORM_DISABLED_STATE;
        }
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (isEmpty(replyTo) || !TextUtils.verifyEmail(replyTo))
        {
            addError("from", getText("admin.errors.must.specify.valid.from.address"));
        }
        if (isEmpty(subject))
        {
            addError("subject", getText("admin.errors.must.specify.subject"));
        }
        if (isEmpty(details))
        {
            addError("details", getText("admin.errors.must.specify.request.details"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!getShouldDisplayForm())
        {
            return FORM_DISABLED_STATE;
        }
        send();
        return getRedirect("/secure/MyJiraHome.jspa");
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(final String to)
    {
        this.to = to;
    }

    public String getFrom()
    {
        return replyTo;
    }

    public void setFrom(final String from)
    {
        this.replyTo = from;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(final String subject)
    {
        this.subject = subject;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(final String details)
    {
        this.details = details;
    }

    public boolean getShouldDisplayForm()
    {
        return jiraContactHelper.isAdministratorContactFormEnabled();
    }

    @ActionViewData(key="renderedMessageContent")
    public String getRenderedMessage()
    {
        String message = getApplicationProperties().getDefaultBackedText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        if (isEmpty(message) || !getShouldDisplayForm())
        {
            message = getText("admin.generalconfiguration.contact.administrators.message.default");
        }

        return rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(message, null);
    }

    @ActionViewData
    public boolean hasCustomMessage()
    {
        String message = getApplicationProperties().getDefaultBackedText(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        return !isEmpty(message);
    }

    @ActionViewDataMappings({"input", "error"})
    public Map<String, Object> getDataMap()
    {
        Map<String, Object> data = Maps.newHashMap();
        data.put("to", getTo());
        data.put("from", getFrom());
        data.put("subject", getSubject());
        data.put("details", getDetails());
        data.put("errors", getErrors());
        data.put("atlToken", getXsrfToken());

        return data;
    }

    public void send() throws MailException
    {
        final Collection<User> administrators = userUtil.getJiraAdministrators();
        for (final User administrator : administrators)
        {
            sendTo(administrator);
        }
    }

    private void sendTo(final User administrator) throws MailException
    {
        try
        {
            final Map<String, Object> velocityParams = Maps.newHashMap();
            velocityParams.put("from", replyTo);
            velocityParams.put("content", details);
            velocityParams.put("padSize", PADSIZE);

            final Email email = new Email(administrator.getEmailAddress());
            email.setReplyTo(replyTo);

            final MailQueueItem item = new EmailBuilder(email, getMimeType(administrator), I18nBean.getLocaleFromUser(administrator))
                    .withSubject(subject)
                    .withBodyFromFile(getTemplateDirectory(administrator) + EMAIL_TEMPLATE)
                    .addParameters(velocityParams)
                    .renderLater();

            mailQueue.addItem(item);
        }
        catch (Exception e)
        {
            log.error("Error sending JIRA Administrator email", e);
        }
    }


    private String getTemplateDirectory(final User to)
    {
        return EMAIL_TEMPLATES + "/" + getFormat(to) + "/";
    }

    private String getMimeType(final User to)
    {
        if (getFormat(to).equals(NotificationRecipient.MIMETYPE_HTML))
        {
            return MimeTypes.TEXT_HTML;
        }
        return MimeTypes.TEXT_PLAIN;
    }

    public String getFormat(final User user)
    {
        final String prefFormat = userPropertyManager.getPropertySet(user).
                getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        // Default to html if the property is not configured.
        if (isNotBlank(prefFormat) && (prefFormat.equals(NotificationRecipient.MIMETYPE_HTML) || prefFormat.equals(NotificationRecipient.MIMETYPE_TEXT)))
        {
            return prefFormat;
        }
        else
        {
            return NotificationRecipient.MIMETYPE_HTML;
        }
    }
}
