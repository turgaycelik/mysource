package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.renderer.RendererConfiguration;
import org.apache.commons.lang.StringUtils;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;


/**
 * The Jira specific implementation of the RendererConfiguration required by the wiki renderer.
 *
 */
public class JiraRendererConfiguration implements RendererConfiguration
{
    private ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraRendererConfiguration(ApplicationProperties applicationProperties,
            VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.applicationProperties = applicationProperties;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    public String getWebAppContextPath()
    {
        HttpServletRequest request = getRequest();

        if (request != null)
        {
            return request.getContextPath();
        }
        else
        {
            String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            if (StringUtils.isWhitespace(baseUrl))
            {
                baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
            }
            return StringUtils.isBlank(baseUrl) ? applicationProperties.getString(APKeys.JIRA_BASEURL) : baseUrl;
        }
    }

    public boolean isNofollowExternalLinks()
    {
        return true;
    }

    public boolean isAllowCamelCase()
    {
        return false;
    }

    public String getCharacterEncoding()
    {
        return applicationProperties.getEncoding();
    }

    private HttpServletRequest getRequest()
    {
        HttpServletRequest request = null;
        try
        {
            request = ServletActionContext.getRequest();
        }
        catch (Exception ignored)
        {
        }
        return request;
    }

}
