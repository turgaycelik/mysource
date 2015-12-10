package com.atlassian.jira.mail;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;

import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

/**
 * Provides the template with all required objects.
 */
public class DefaultTemplateContext implements TemplateContext
{
    private final Locale locale;
    private final WebResourceUrlProvider resourceUrlProvider;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory beanFactory;

    public DefaultTemplateContext(Locale locale, WebResourceUrlProvider resourceUrlProvider, ApplicationProperties applicationProperties, I18nHelper.BeanFactory beanFactory)
    {
        this.locale = locale;
        this.resourceUrlProvider = resourceUrlProvider;
        this.applicationProperties = applicationProperties;
        this.beanFactory = beanFactory;
    }

    @Override
    public Map<String, Object> getTemplateParams()
    {
        // NOTE: if adding a parameter here please update the doc online at
        // https://developer.atlassian.com/display/JIRADEV/Velocity+Context+for+Email+Templates

        final I18nHelper i18nHelper = beanFactory.getInstance(locale);
        
        final Map<String, Object> templateParams = Maps.newHashMap();
        templateParams.put("i18n", i18nHelper);
        final OutlookDate formatter = new OutlookDate(locale);
        templateParams.put("dateformatter", formatter);
        
        final LookAndFeelBean landf = LookAndFeelBean.getInstance(applicationProperties);
        templateParams.put("lfbean", landf);
        templateParams.put("baseurl", applicationProperties.getString(APKeys.JIRA_BASEURL));

        String jiraLogo = landf.getLogoUrl();
        if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = resourceUrlProvider.getStaticResourcePrefix(UrlMode.ABSOLUTE) + jiraLogo;
        }
        templateParams.put("jiraLogoUrl", jiraLogo);
        
        return templateParams;
    }
}
