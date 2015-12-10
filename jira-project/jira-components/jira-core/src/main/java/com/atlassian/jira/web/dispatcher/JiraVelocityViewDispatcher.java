package com.atlassian.jira.web.dispatcher;

import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.ServletContextProvider;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.VelocityException;
import webwork.config.util.ActionInfo;
import webwork.dispatcher.ActionResult;
import webwork.view.velocity.VelocityHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.template.TemplateSources.file;
import static com.atlassian.jira.web.dispatcher.JiraWebworkViewDispatcher.isFromCore;

/**
 * This can render an actions view via the Velocity Template system and write the result directly to the {@link
 * javax.servlet.http.HttpServletResponse}
 *
 * @since v6.0
 */
class JiraVelocityViewDispatcher
{
    private final ActionViewDataSupport actionViewDataSupport = new ActionViewDataSupport();

    public void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, final ActionResult actionResult, ActionInfo.ViewInfo viewInfo)
            throws ServletException, IOException

    {

        String templatePath = buildTemplatePath(actionResult, viewInfo);

        final PrintWriter writer = httpServletResponse.getWriter();
        try
        {
            final VelocityContext context = buildActionVelocityContext(httpServletRequest, httpServletResponse, actionResult, viewInfo);
            getTemplatingEngine().render(file(templatePath)).applying(context).asHtml(writer);
        }
        catch (VelocityException e)
        {
            errorHandling(templatePath, writer, e);
        }

    }

    private String buildTemplatePath(final ActionResult actionResult, final ActionInfo.ViewInfo viewInfo)
    {
        String pluginSource = viewInfo.getActionInfo().getSource();
        //
        // JRADEV-13927 - we need to put pluginKey:moduleKey/templateName into the mix so that the Velocity template loader code
        // knows what plugin this code came from.  Unless its core of course.
        //
        String pluginPrefix = isFromCore(pluginSource) ? "" : pluginSource + "/";
        return pluginPrefix + actionResult.getView().toString();
    }

    private void errorHandling(final String templatePath, final PrintWriter writer, final VelocityException e)
    {
        writer.write("Exception rendering velocity file " + TextUtils.htmlEncode(templatePath));
        writer.write("<br><pre>");
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        writer.write(TextUtils.htmlEncode(stringWriter.toString()));
        writer.write("</pre>");
    }

    private VelocityContext buildActionVelocityContext(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final ActionResult actionResult, final ActionInfo.ViewInfo viewInfo)
    {
        ActionContextKit.setContext(httpServletRequest, httpServletResponse, ServletContextProvider.getServletContext());

        final Map<String, Object> velocityParams = getDefaultVelocityParams();
        velocityParams.put("i18n", getAuthenticationContext().getI18nHelper());

        velocityParams.putAll(actionViewDataSupport.getData(actionResult, viewInfo));

        return (VelocityContext) VelocityHelper.getContextWithoutInit(httpServletRequest, httpServletResponse, velocityParams);
    }

    @VisibleForTesting
    Map<String, Object> getDefaultVelocityParams()
    {
        return JiraVelocityUtils.getDefaultVelocityParams(getAuthenticationContext());
    }

    @VisibleForTesting
    JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return getComponent(VelocityTemplatingEngine.class);
    }
}
