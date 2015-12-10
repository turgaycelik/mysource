package com.atlassian.jira.web.servlet.rpc;

import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet is used to load Axis and publish our soap service.
 */
public class LazyAxisDecoratorServlet extends HttpServlet
{
    private ServletConfig config;

    public void init(ServletConfig config)
    {
        this.config = config;
        getAxisProvider().init(config);
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
    {
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        final ApplicationProperties applicationProperties = getApplicationProperties();
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_RPC_ALLOW))
        {
            if (applicationProperties.getString(APKeys.JIRA_SETUP) != null)
            {
                final OpTimer timer = Instrumentation.pullTimer(InstrumentationName.SOAP_REQUESTS);
                try
                {
                    ActionContextKit.resetContext();
                    getAxisProvider().init(config);
                    getAxisProvider().getAxisServlet().service(servletRequest, servletResponse);
                }
                finally
                {
                    ActionContextKit.resetContext();
                    timer.end();
                }
            }
            else
            {
                httpServletResponse.sendError(503, "JIRA has not been setup. Remote API is not available.");
            }
        }
        else
        {
            httpServletResponse.sendError(503, "Remote API not activated for this JIRA installation.");
        }
    }

    protected AxisServletProvider getAxisProvider()
    {
        return ComponentAccessor.getComponent(AxisServletProvider.class);
    }

    private ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getComponent(ApplicationProperties.class);
    }

}
