package com.atlassian.jira.plugin.webresource;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.InitializingComponent;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.i18n.CachingI18nFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.plugin.webresource.cdn.CDNStrategy;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The implementation of the {@link com.atlassian.plugin.webresource.WebResourceIntegration} for JIRA.
 */
public class JiraWebResourceIntegration implements WebResourceIntegration, InitializingComponent
{
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final CachingI18nFactory i18nFactory;
    private final JiraHome jiraHome;
    private final EventPublisher eventPublisher;
    private final FeatureManager featureManager;

    public JiraWebResourceIntegration(
            final PluginAccessor pluginAccessor, final ApplicationProperties applicationProperties,
            final VelocityRequestContextFactory requestContextFactory, final BuildUtilsInfo buildUtilsInfo,
            final JiraAuthenticationContext jiraAuthenticationContext, CachingI18nFactory i18nFactory, JiraHome jiraHome,
            final EventPublisher eventPublisher, final FeatureManager featureManager)
    {
        this.i18nFactory = i18nFactory;
        this.pluginAccessor = notNull("pluginAccessor", pluginAccessor);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.requestContextFactory = notNull("requestContextFactory", requestContextFactory);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraAuthenticationContext = notNull("jiraAuthenticationContext", jiraAuthenticationContext);
        this.jiraHome = notNull("jiraHome", jiraHome);
        this.eventPublisher = eventPublisher;
        this.featureManager = featureManager;
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    @EventListener
    // If the superbatch is retrieved during plugin system startup / bootstrap, an incomplete set of dependencies may be cached (JRA-32692).
    // Increment the version of the superbatch once the plugin framework is done starting up to invalidate the cached superbatch.
    public void pluginFrameworkStarted(final PluginFrameworkStartedEvent event)
    {
        incrementSuperbatchVersion();
    }

    private void incrementSuperbatchVersion()
    {
        String versionString = getSuperBatchVersion();
        Long superbatchVersion = StringUtils.isNotEmpty(versionString) ? Long.parseLong(versionString) : 1L;
        applicationProperties.setString(APKeys.WEB_RESOURCE_SUPER_BATCH_FLUSH_COUNTER, Long.toString(superbatchVersion + 1));
    }

    public PluginAccessor getPluginAccessor()
    {
        return pluginAccessor;
    }

    public Map<String, Object> getRequestCache()
    {
        return JiraAuthenticationContextImpl.getRequestCache();
    }

    public String getSystemCounter()
    {
        return applicationProperties.getDefaultBackedString(APKeys.WEB_RESOURCE_FLUSH_COUNTER);
    }

    public String getSystemBuildNumber()
    {
        return buildUtilsInfo.getCurrentBuildNumber();
    }

    public String getBaseUrl()
    {
        return getBaseUrl(UrlMode.AUTO);
    }

    public String getBaseUrl(final UrlMode urlMode)
    {
        switch (urlMode)
        {
            case RELATIVE:
            case AUTO:
                return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            case ABSOLUTE:
                return requestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
            default:
                throw new AssertionError("Unsupported URLMode: " + urlMode);
        }
    }

    public String getSuperBatchVersion()
    {
        return applicationProperties.getDefaultBackedString(APKeys.WEB_RESOURCE_SUPER_BATCH_FLUSH_COUNTER);
    }

    @Override
    public String getStaticResourceLocale()
    {
        final I18nHelper i18n = jiraAuthenticationContext.getI18nHelper();
        return i18n.getLocale().toString() + i18nFactory.getStateHashCode();
    }

    @Nonnull
    @Override
    public File getTemporaryDirectory()
    {
        return new File(jiraHome.getLocalHome(), "tmp" + File.separator + "webresources");
    }

    @Override
    public void afterInstantiation() throws Exception
    {
        eventPublisher.register(this);
    }

    @Override
    public CDNStrategy getCDNStrategy()
    {
        return featureManager.isEnabled(JiraPrefixCDNStrategy.TOGGLE_FEATURE_KEY) ? new JiraPrefixCDNStrategy() : null;
    }

    @Override
    public Locale getLocale()
    {
        return jiraAuthenticationContext.getLocale();
    }

    @Override
    public String getI18nRawText(Locale locale, String key)
    {
        return i18nFactory.getInstance(locale).getUnescapedText(key);
    }

    @Override
    public String getI18nText(Locale locale, String key)
    {
        return i18nFactory.getInstance(locale).getText(key);
    }
}
