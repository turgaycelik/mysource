package com.atlassian.jira.web.dispatcher;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import webwork.config.util.ActionInfo;
import webwork.dispatcher.ActionResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * This can render an actions view via the Soy Template system and write the result directly to the {@link
 * javax.servlet.http.HttpServletResponse}
 *
 * @since v6.0
 */
class JiraSoyViewDispatcher
{
    private final ActionViewDataSupport actionViewDataSupport = new ActionViewDataSupport();

    public void dispatch(HttpServletResponse httpServletResponse, final ActionResult ar, ActionInfo.ViewInfo viewInfo)
            throws ServletException, IOException

    {
        try
        {
            SoyTemplateAddress soy = SoyTemplateAddress.address(viewInfo);
            soyRenderer().render(getAppendable(httpServletResponse), soy.getCompleteKey(), soy.getTemplateName(), getParameters(ar, viewInfo));
        }
        catch (SoyException e)
        {
            throw new ServletException(e);
        }
    }

    private Map<String, Object> getParameters(final ActionResult ar, final ActionInfo.ViewInfo viewInfo)
    {
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.putAll(actionViewDataSupport.getData(ar, viewInfo));

        return parameters;
    }

    @VisibleForTesting
    SoyTemplateRenderer soyRenderer()
    {
        SoyTemplateRendererProvider rendererProvider = ComponentAccessor.getComponent(SoyTemplateRendererProvider.class);
        return rendererProvider.getRenderer();
    }

    @VisibleForTesting
    Appendable getAppendable(final HttpServletResponse httpServletResponse) throws IOException
    {
        final PrintWriter writer = httpServletResponse.getWriter();
        return new Appendable()
        {
            @Override
            public Appendable append(final CharSequence csq) throws IOException
            {
                writer.append(csq);
                return this;
            }

            @Override
            public Appendable append(final CharSequence csq, final int start, final int end) throws IOException
            {
                writer.append(csq, start, end);
                return this;
            }

            @Override
            public Appendable append(final char c) throws IOException
            {
                writer.append(c);
                return this;
            }
        };
    }
}
