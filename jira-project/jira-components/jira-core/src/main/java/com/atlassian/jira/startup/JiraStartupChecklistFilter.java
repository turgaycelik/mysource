package com.atlassian.jira.startup;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;
import com.atlassian.jira.web.util.MetalResourcesManager;

import com.google.common.collect.ImmutableMap;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * This filter is used to ensure that JIRA has started up correctly.
 * <p/>
 * If not it always shows an error page, effectively locking out users from JIRA.
 * <p/>
 * This is the FIRST filter that is run within JIRA.
 *
 * @since v4.0
 */
public class JiraStartupChecklistFilter implements Filter
{
    private static final String SUFFIX = "JiraLockedError";
    private static final String LOCKED_TEMPLATE_NAME = "templates/jira/appconsistency/jiralocked.vm";
    private static final String NOERRORS_TEMPLATE_NAME = "templates/jira/appconsistency/no-errors-detected.vm";

    private VelocityEngine velocityEngine;

    public void init(final FilterConfig filterConfig) throws ServletException
    {
        // Nothing
    }

    public void destroy()
    {
        // Do nothing
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        final String contextPath = httpServletRequest.getContextPath();

        if (JiraStartupChecklist.startupOK())
        {
            // Situation Normal
            if (httpServletRequest.getRequestURI().endsWith(SUFFIX))
            {
                try
                {
                    final Map<String, ?> parameters = ImmutableMap.of(
                            "contextPath", contextPath,
                            "resourcesHtml", MetalResourcesManager.getMetalResources(contextPath)
                    );
                    httpServletResponse.setContentType("text/html; charset=UTF-8");
                    getVelocityEngine().mergeTemplate(NOERRORS_TEMPLATE_NAME, "UTF-8", new VelocityContext(parameters), httpServletResponse.getWriter());
                }
                catch (Exception e)
                {
                    throw new ServletException("Exception occurred while rendering template '" + NOERRORS_TEMPLATE_NAME + "'.", e);
                }
            }
            else
            {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        }
        else
        {
            // Lock JIRA
            if (httpServletRequest.getRequestURI().endsWith(SUFFIX))
            {
                final ImmutableMap<String, ?> parameters = ImmutableMap.of(
                        "failedStartupCheck", JiraStartupChecklist.getFailedStartupCheck(),
                        "contextPath", contextPath,
                        "resourcesHtml", MetalResourcesManager.getMetalResources(contextPath)
                );
                try
                {
                    httpServletResponse.setContentType("text/html; charset=UTF-8");
                    httpServletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    // Not using the VelocityManager as we do not want to 'touch' the rest of JIRA, as the database has
                    // been locked. So go straight to Velocity.
                    getVelocityEngine().mergeTemplate(LOCKED_TEMPLATE_NAME, "UTF-8", new VelocityContext(parameters), httpServletResponse.getWriter());
                }
                catch (Exception e)
                {
                    throw new ServletException("Exception occurred while rendering template '" + LOCKED_TEMPLATE_NAME + "'.", e);
                }
            }
            else
            {
                httpServletResponse.sendRedirect(contextPath + "/" + SUFFIX);
            }
        }
    }

    @ClusterSafe ("Program Artefact")
    private synchronized VelocityEngine getVelocityEngine()
    {
        if (velocityEngine == null)
        {
            velocityEngine = new VelocityEngineFactory.Default().getEngine();
        }

        return velocityEngine;
    }
}
