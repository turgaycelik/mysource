package com.atlassian.jira.sal;

import com.atlassian.core.filters.ServletContextThreadLocal;
import com.atlassian.fugue.Option;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.google.common.base.Supplier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.io.File;
import javax.servlet.http.HttpServletRequest;

/**
 * JIRA implementation of SAL's ApplicationProperties
 */
public class JiraApplicationProperties implements ApplicationProperties
{
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final JiraHome jiraHome;
    private final com.atlassian.jira.config.properties.ApplicationProperties jiraApplicationProperties;
    private final BuildUtilsInfo buildUtilsInfo;
    private final Supplier<String> CANONICAL_BASE_URL_SUPPLIER = new Supplier<String>()
    {
        @Override
        public String get()
        {
            return getCanonicalBaseUrl();
        }
    };
    private final Supplier<String> CANONICAL_CONTEXT_PATH_SUPPLIER = new Supplier<String>()
    {
        @Override
        public String get()
        {
            return getCanonicalContextPath();
        }
    };


    public JiraApplicationProperties(VelocityRequestContextFactory velocityRequestContextFactory, JiraHome jiraHome, final com.atlassian.jira.config.properties.ApplicationProperties jiraApplicationProperties, final BuildUtilsInfo buildUtilsInfo)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.jiraHome = jiraHome;
        this.jiraApplicationProperties = jiraApplicationProperties;
        this.buildUtilsInfo = buildUtilsInfo;
    }

    public String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }

    @Override
    public String getBaseUrl(final UrlMode urlMode)
    {
        switch (urlMode)
        {
            case CANONICAL:
                // The configured base URL
                return getCanonicalBaseUrl();
            case ABSOLUTE:
                // The base URL of the request, or the configured base URL if not request
                return getBaseUrlFromRequest().getOrElse(CANONICAL_BASE_URL_SUPPLIER);
            case RELATIVE:
                // Context path of the request, or context path of configured base URL
                return getContextPathFromRequest().getOrElse(CANONICAL_CONTEXT_PATH_SUPPLIER);
            case RELATIVE_CANONICAL:
                // Context path of configured base URL
                return getCanonicalContextPath();
            case AUTO:
                // relative URL of the request, or base URL otherwise
                return getContextPathFromRequest().getOrElse(CANONICAL_BASE_URL_SUPPLIER);
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private String getCanonicalBaseUrl()
    {
        return jiraApplicationProperties.getText(APKeys.JIRA_BASEURL);
    }

    private String getCanonicalContextPath()
    {
        final String baseUrl = getCanonicalBaseUrl();
        try
        {
            return new URL(baseUrl).getPath();
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException("Base URL misconfigured", e);
        }
    }

    private Option<String> getBaseUrlFromRequest()
    {
        final HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null)
        {
            return Option.some(JiraUrl.constructBaseUrl(request));
        }
        return Option.none();
    }

    private Option<String> getContextPathFromRequest()
    {
        final HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null)
        {
            return Option.some(request.getContextPath());
        }
        return Option.none();
    }

    public String getApplicationName()
    {
        return jiraApplicationProperties.getText(APKeys.JIRA_TITLE);
    }

    public String getDisplayName()
    {
        return "JIRA";
    }

    public String getVersion()
    {
        return buildUtilsInfo.getVersion();
    }

    public Date getBuildDate()
    {
        return buildUtilsInfo.getCurrentBuildDate();
    }

    public String getBuildNumber()
    {
        return buildUtilsInfo.getCurrentBuildNumber();
    }

    public File getHomeDirectory()
    {
        return jiraHome.getHome();
    }

    public String getPropertyValue(String key)
    {
        return jiraApplicationProperties.getDefaultBackedString(key);
    }
}
