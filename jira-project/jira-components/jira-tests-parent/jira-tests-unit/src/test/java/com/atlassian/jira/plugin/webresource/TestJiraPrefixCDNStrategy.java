package com.atlassian.jira.plugin.webresource;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.i18n.CachingI18nFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJiraPrefixCDNStrategy
{
    @Test
    public void testWebResourceIntegrationNotCdnEnabled()
    {
        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.isEnabled(JiraPrefixCDNStrategy.TOGGLE_FEATURE_KEY)).thenReturn(false);

        WebResourceIntegration wri = createWebResourceIntegration(featureManager);

        assertNull(wri.getCDNStrategy());
    }

    @Test
    public void testWebResourceIntegrationNotEnabled()
    {
        FeatureManager featureManager = mock(FeatureManager.class);
        when(featureManager.isEnabled(JiraPrefixCDNStrategy.TOGGLE_FEATURE_KEY)).thenReturn(true);

        WebResourceIntegration wri = createWebResourceIntegration(featureManager);

        assertNotNull(wri.getCDNStrategy());
    }

    @Test
    public void testStrategyNoPrefixNull()
    {
        JiraSystemProperties.getInstance().unsetProperty(JiraPrefixCDNStrategy.PREFIX_SYSTEM_PROPERTY);

        JiraPrefixCDNStrategy strategy = new JiraPrefixCDNStrategy();

        assertFalse(strategy.supportsCdn());
    }

    @Test
    public void testStrategyNoPrefixEmptyString()
    {
        JiraSystemProperties.getInstance().setProperty(JiraPrefixCDNStrategy.PREFIX_SYSTEM_PROPERTY, "");

        JiraPrefixCDNStrategy strategy = new JiraPrefixCDNStrategy();

        assertFalse(strategy.supportsCdn());
    }

    @Test
    public void testStrategyPrefix()
    {
        JiraSystemProperties.getInstance().setProperty(JiraPrefixCDNStrategy.PREFIX_SYSTEM_PROPERTY, "//blah.cdn.com/my.jira.com");

        JiraPrefixCDNStrategy strategy = new JiraPrefixCDNStrategy();

        assertTrue(strategy.supportsCdn());
        assertEquals("//blah.cdn.com/my.jira.com/my/url.js?a", strategy.transformRelativeUrl("/my/url.js?a"));
    }

    private WebResourceIntegration createWebResourceIntegration(FeatureManager featureManager)
    {
        return new JiraWebResourceIntegration(mock(PluginAccessor.class),
                mock(ApplicationProperties.class), mock(VelocityRequestContextFactory.class),
                mock(BuildUtilsInfo.class), mock(JiraAuthenticationContext.class), mock(CachingI18nFactory.class),
                mock(JiraHome.class), mock(EventPublisher.class), featureManager);
    }
}
