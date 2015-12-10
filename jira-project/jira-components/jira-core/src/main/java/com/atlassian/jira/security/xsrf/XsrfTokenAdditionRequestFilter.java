package com.atlassian.jira.security.xsrf;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This request {@link javax.servlet.Filter} will set a XSRF token into the session IF there is a user AND they dont
 * already have a token.
 *
 * @since v4.0
 */
public class XsrfTokenAdditionRequestFilter implements Filter
{

    private static final String ALREADY_FILTERED = XsrfTokenAdditionRequestFilter.class.getName() + "_already_filtered";

    /**
     * Adds the XSRF token to the session IF there is a user AND they dont already have a token
     *
     * @param servletRequest  request
     * @param servletResponse response
     * @param filterChain     filter chain
     *
     * @throws java.io.IOException            if another filter in the filter chain throws it
     * @throws javax.servlet.ServletException if another filter in the filter chain throws it
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        // Only apply this filter once per httpServletRequest and not during setup since not enough of JIRA will be available
        final String jiraSetup = getJiraApplicationProperties().getString(APKeys.JIRA_SETUP);
        if (servletRequest.getAttribute(ALREADY_FILTERED) != null || jiraSetup == null)
        {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        else
        {
            servletRequest.setAttribute(ALREADY_FILTERED, Boolean.TRUE);
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        setXsrfToken(httpServletRequest);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    protected ApplicationProperties getJiraApplicationProperties()
    {
        return ComponentAccessor.getComponentOfType(ApplicationProperties.class);
    }

    public void init(final FilterConfig filterConfig) throws ServletException
    {
        // nothing to do
    }

    public void destroy()
    {
        // nothing to do
    }

    private void setXsrfToken(final HttpServletRequest httpServletRequest)
    {
        getXsrfTokenGenerator().generateToken(httpServletRequest);
    }

    XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
    }

}
