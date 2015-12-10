package com.atlassian.jira.admin;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.velocity.JiraVelocityManager;

import static com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Helper class for displaying the announcement banner.
 *
 * @since v5.0.4
 */
@SuppressWarnings ("UnusedDeclaration")
public class AnnouncementBanner
{
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final I18nHelper.BeanFactory i18nFactory;
    private final RenderablePropertyImpl property;

    public AnnouncementBanner(JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties, RendererManager rendererManager, JiraVelocityManager jiraVelocityManager, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nFactory, RenderablePropertyFactory renderablePropertyFactory)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.i18nFactory = i18nFactory;
        this.property = renderablePropertyFactory.create(new ApplicationPropertiesPersister(applicationProperties, APKeys.JIRA_ALERT_HEADER), new AnnouncementBannerDescriptions());
    }

    public boolean isDisplay()
    {
        return isNotBlank(property.getValue()) && (headerIsPublic() || jiraAuthenticationContext.isLoggedInUser());
    }

    public String getViewHtml()
    {
        return property.getViewHtml();
    }

    public String getEditHtml()
    {
        return property.getEditHtml("announcement");
    }

    public String getIntroHtml()
    {
        if (property.isOnDemand())
        {
            return getText("admin.announcement.intro.text");
        }

        return getText("admin.announcement.description");
    }

    protected boolean headerIsPublic()
    {
        return "public".equals(applicationProperties.getDefaultBackedString(APKeys.JIRA_ALERT_HEADER_VISIBILITY));
    }

    private String getText(String key)
    {
        return i18nFactory.getInstance(jiraAuthenticationContext.getUser()).getText(key);
    }

    private class AnnouncementBannerDescriptions implements PropertyDescriptions
    {
        @Override
        public String getBtfDescriptionHtml()
        {
            return getText("admin.announcement.close.tags");
        }

        @Override
        public String getOnDemandDescriptionHtml()
        {
            return getText("admin.announcement.help.text");
        }
    }
}
