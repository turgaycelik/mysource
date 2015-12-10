/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.mail.builder.EmailBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.queue.MailQueue;

import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

public class UserMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(UserMailQueueItem.class);

    private static final String EMAIL_TEMPLATES = "templates/email/";
    private static final Integer PADSIZE = 20;

    private final UserEvent event;
    private final String subjectKey;
    private final String template;
    private final ApplicationProperties applicationProperties;
    private final FeatureManager featureManager;

    public UserMailQueueItem(UserEvent event, String subject, String subjectKey, String template)
    {
        super(subject);
        this.event = event;
        this.subjectKey = subjectKey;
        this.template = template;
        this.applicationProperties = ComponentAccessor.getComponent(ApplicationProperties.class);
        this.featureManager = ComponentAccessor.getComponent(FeatureManager.class);
    }

    public void send() throws MailException
    {
        incrementSendCount();

        try
        {
            final User user = event.getUser();
            if (user != null)
            {
                Map<String, Object> params = getUserContextParamsBody(event);
                params.put("initiatingUser", event.getInitiatingUser());
                params.put("applicationName", getApplicationName());
                params.put("product", getProduct()); // TODO JRADEV-12245 - Make OnDemand's emails the same as JIRA's.
                // Text email formatting - used to leftPad strings in text emails.
                params.put("padSize", PADSIZE);
                params.put("stringUtils", new StringUtils());

                final String templatePath = getTemplatePath(template);
                final I18nHelper i18nBean = new I18nBean(user);
                final Email email = new Email(user.getEmailAddress());

                final MailQueueItem item = new EmailBuilder(email, getMimeType(templatePath), i18nBean.getLocale())
                        .withSubject(i18nBean.getText(getSubjectKey()))
                        .withBodyFromFile(templatePath)
                        .addParameters(params)
                        .renderLater();
                getMailQueue().addItem(item);
            }
            else
            {
                log.warn("Mail with subject '" + getSubject() + "' not sent since user '" + user + "' no longer exists.");
            }
        }
        catch (Exception ex)
        {
            throw new MailException(ex);
        }
    }

    public String getSubjectKey()
    {
        return subjectKey;
    }

    @VisibleForTesting
    MailQueue getMailQueue()
    {
        return ComponentAccessor.getMailQueue();
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    protected Map<String, Object> getUserContextParamsBody(UserEvent uEvent)
    {
        Map<String, Object> contextParams = new HashMap<String, Object>();

        contextParams.put("user", uEvent.getUser());
        contextParams.put("params", uEvent.getParams());

        return contextParams;
    }

    private String getApplicationName()
    {
        return applicationProperties.getText(APKeys.JIRA_TITLE);
    }

    private String getProduct()
    {
        return featureManager.isOnDemand() ? "ondemand" : "jira";
    }

    private String getTemplatePath(final String template)
    {
        final boolean renderAsHtml = (ClassLoaderUtils.getResource(EMAIL_TEMPLATES + "html/" + template, this.getClass()) != null);
        final String prefix = (renderAsHtml) ? "html/" : "text/";
        return EMAIL_TEMPLATES + prefix + template;
    }

    private String getMimeType(final String template)
    {
        boolean renderAsHtml = template.contains("html");
        return (renderAsHtml) ? "text/html" : "text/plain";
    }
}
