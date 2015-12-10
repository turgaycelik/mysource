package com.atlassian.jira.web.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.module.sitemesh.HTMLPage;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.util.OutputConverter;
import org.apache.velocity.exception.VelocityException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.template.TemplateSources.file;

/**
 * Servlet that renders velocity decorators.  Shouldn't be requested directly, but rather should be invoked by a request
 * dispatcher by sitemesh.  Usually, it would have
 */
public class VelocityDecoratorServlet extends HttpServlet
{
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        Page page = (Page) request.getAttribute(RequestConstants.PAGE);
        if (page != null)
        {
            applyDecoratorUsingVelocity(request, page, response);
        }
        else
        {
            String servletPath = (String) request.getAttribute("javax.servlet.include.servlet_path");
            if (servletPath == null)
                servletPath = request.getServletPath();
            throw new ServletException("No sitemesh page to decorate. This servlet should not be invoked directly. " +
                "The path invoked was " + servletPath);
        }
    }

    private void applyDecoratorUsingVelocity(HttpServletRequest request, Page page, HttpServletResponse response) throws
        IOException
    {
        String servletPath = (String) request.getAttribute("javax.servlet.include.servlet_path");
        if (servletPath == null)
        {
            servletPath = request.getServletPath();
        }

        if (servletPath != null && servletPath.startsWith("/"))
        {
            servletPath = servletPath.substring(1); // trim leading slash
        }

        Map<String, Object> velocityParams = getVelocityParams(request, page, response);

        final PrintWriter writer = response.getWriter();
        try
        {
            response.setContentType(getApplicationProperties().getContentType());
            final String body = getTemplatingEngine().render(file(servletPath)).applying(velocityParams).asHtml();
            writer.write(body);
        }
        catch (VelocityException e)
        {
            writer.write("Exception rendering velocity file " + servletPath);
            writer.write("<br><pre>");
            e.printStackTrace(writer);
            writer.write("</pre>");
        }

        request.removeAttribute(RequestConstants.PAGE);
    }

    private Map<String, Object> getVelocityParams(HttpServletRequest request, Page page, HttpServletResponse response)
        throws IOException
    {
        Map<String, Object> velocityParams = getDefaultVelocityParams();

        velocityParams.put("page", page);
        velocityParams.put("title", page.getTitle());

        StringWriter bodyBuffer = new StringWriter();
        page.writeBody(OutputConverter.getWriter(bodyBuffer));
        velocityParams.put("body", bodyBuffer);

        if (page instanceof HTMLPage)
        {
            HTMLPage htmlPage = (HTMLPage) page;
            StringWriter buffer = new StringWriter();
            htmlPage.writeHead(OutputConverter.getWriter(buffer));
            velocityParams.put("head", buffer.toString());
        }

        // This allows the templates to include JSPs, using $dispatcher.include()
        velocityParams.put("dispatcher", new BufferingRequestDispatcher(request, response));

        velocityParams.put("i18n", getJiraAuthenticationContext().getI18nHelper());
        velocityParams.put("req", request);
        return velocityParams;
    }

    @VisibleForTesting
    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    @VisibleForTesting
    JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    @VisibleForTesting
    Map<String, Object> getDefaultVelocityParams()
    {
        return JiraVelocityUtils.getDefaultVelocityParams(getJiraAuthenticationContext());
    }

    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return getComponent(VelocityTemplatingEngine.class);
    }
}
