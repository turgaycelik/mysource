package com.atlassian.jira.plugin.aui;

import com.atlassian.aui.spi.AuiIntegration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;

import java.io.Serializable;

/**
 * @since v5.2
 */
public class JiraAuiIntegration implements AuiIntegration
{

    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public JiraAuiIntegration(I18nHelper.BeanFactory i18nFactory, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.i18nFactory = i18nFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public String getContextPath()
    {
        return ExecutingHttpRequest.get().getContextPath();
    }

    @Override
    public String getRawText(String key)
    {
        final I18nHelper bean = i18nFactory.getInstance(jiraAuthenticationContext.getLocale());
        return bean.getUnescapedText(key);
    }

    @Override
    public String getText(String key, Serializable... arguments)
    {
        final I18nHelper bean = i18nFactory.getInstance(jiraAuthenticationContext.getLocale());
        return bean.getText(key, arguments);
    }
}
