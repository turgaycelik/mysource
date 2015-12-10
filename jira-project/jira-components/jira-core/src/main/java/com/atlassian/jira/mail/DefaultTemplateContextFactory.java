/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.changehistory.metadata.renderer.HistoryMetadataRenderHelper;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;

import java.util.Locale;

public class DefaultTemplateContextFactory implements TemplateContextFactory
{
    private final TemplateIssueFactory templateIssueFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final EventTypeManager eventTypeManager;
    private final UserManager userManager;
    private final DiffViewRenderer diffViewRenderer;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory beanFactory;
    private final HistoryMetadataManager historyMetadataManager;
    private final HistoryMetadataRenderHelper historyMetadataRenderHelper;

    public DefaultTemplateContextFactory(TemplateIssueFactory templateIssueFactory, FieldLayoutManager fieldLayoutManager,
            RendererManager rendererManager, JiraDurationUtils jiraDurationUtils, EventTypeManager eventTypeManager,
            UserManager userManager, DiffViewRenderer diffViewRenderer, WebResourceUrlProvider webResourceUrlProvider,
            I18nHelper.BeanFactory beanFactory, ApplicationProperties applicationProperties,
            HistoryMetadataManager historyMetadataManager, HistoryMetadataRenderHelper historyMetadataRenderHelper)
    {
        this.templateIssueFactory = templateIssueFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.eventTypeManager = eventTypeManager;
        this.userManager = userManager;
        this.diffViewRenderer = diffViewRenderer;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.beanFactory = beanFactory;
        this.applicationProperties = applicationProperties;
        this.historyMetadataManager = historyMetadataManager;
        this.historyMetadataRenderHelper = historyMetadataRenderHelper;
    }

    @Override
    public TemplateContext getTemplateContext(final Locale locale)
    {
        return new DefaultTemplateContext(locale, webResourceUrlProvider, applicationProperties, beanFactory);
    }

    @Override
    public TemplateContext getTemplateContext(final Locale locale, final IssueEvent issueEvent)
    {
        return new IssueTemplateContext(locale, issueEvent, templateIssueFactory, fieldLayoutManager,
                rendererManager, userManager, jiraDurationUtils, eventTypeManager, diffViewRenderer,
                webResourceUrlProvider, applicationProperties, beanFactory,
                historyMetadataManager, historyMetadataRenderHelper);
    }
}
