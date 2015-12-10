package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.UserAgentUtil;
import com.atlassian.jira.util.UserAgentUtilImpl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.webresource.DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF;
import static com.atlassian.plugin.webresource.DefaultResourceBatchingConfiguration.PLUGIN_WEB_RESOURCE_JAVASCRIPT_TRY_CATCH_WRAPPING;

/**
 * Determines which resources are included superbatched on every page!
 *
 * @since v4.3
 */
public class JiraWebResourceBatchingConfiguration implements ResourceBatchingConfiguration
{
    @SuppressWarnings ("RedundantStringToString")
    public static final String PLUGIN_WEB_RESOURCE_BATCH_CONTENT_TRACKING = "plugin.webresource.batch.content.tracking".toString();
    private static final String USER_AGENT = "USER-AGENT";

    private static final List<String> resources = new ArrayList<String>();

    static
    {
        resources.add("jira.webresources:superbatch-default");
    }

    private final JiraProperties jiraSystemProperties;

    public JiraWebResourceBatchingConfiguration(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    @Override
    public boolean isSuperBatchingEnabled()
    {
        return (!resources.isEmpty() && !jiraSystemProperties.isSuperBatchingDisabled()) || forceBatchingInThisRequest();
    }

    @Override
    public boolean isContextBatchingEnabled()
    {
        return !jiraSystemProperties.isDevMode() || forceBatchingInThisRequest();
    }

    @Override
    public boolean isPluginWebResourceBatchingEnabled()
    {
        final boolean forced = forceBatchingInThisRequest();
        if (forced)
        {
            return forced;
        }

        if (jiraSystemProperties.getProperty(PLUGIN_WEBRESOURCE_BATCHING_OFF) != null)
        {
            return !Boolean.parseBoolean(jiraSystemProperties.getProperty(PLUGIN_WEBRESOURCE_BATCHING_OFF));
        }
        else
        {
            return !jiraSystemProperties.isDevMode();
        }
    }

    @Override
    public List<String> getSuperBatchModuleCompleteKeys()
    {
        return resources;
    }

    @Override
    public boolean isJavaScriptTryCatchWrappingEnabled()
    {
        return jiraSystemProperties.getBoolean(PLUGIN_WEB_RESOURCE_JAVASCRIPT_TRY_CATCH_WRAPPING);
    }

    @Override
    public boolean isBatchContentTrackingEnabled()
    {
        return jiraSystemProperties.getBoolean(PLUGIN_WEB_RESOURCE_BATCH_CONTENT_TRACKING);
    }

    @Override
    public boolean resplitMergedContextBatchesForThisRequest()
    {
        return isCurrentRequestIE();
    }

    private boolean forceBatchingInThisRequest()
    {
        // only force batching if in DEV mode -- this whole "forcing" concept is a developer helper only
        return jiraSystemProperties.isDevMode() && isCurrentRequestIE();
    }

    private boolean isCurrentRequestIE()
    {
        final HttpServletRequest httpRequest = ExecutingHttpRequest.get();
        if (httpRequest == null)
        {
            return false;
        }
        final String userAgent = httpRequest.getHeader(USER_AGENT);
        final UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
        final UserAgentUtil.UserAgent userAgentInfo = userAgentUtil.getUserAgentInfo(userAgent);

        // force batching if we are on IE, disable otherwise
        return userAgentInfo.getBrowser().getBrowserFamily().equals(UserAgentUtil.BrowserFamily.MSIE)
                || userAgentInfo.getBrowser().getBrowserFamily().equals(UserAgentUtil.BrowserFamily.IE);
    }
}
