package com.atlassian.jira.web.filters.steps.pagebuilder;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterCallContextImpl;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.atlassian.jira.web.pagebuilder.DecoratablePage;
import com.atlassian.jira.web.pagebuilder.PageBuilderServiceSpi;
import com.opensymphony.module.sitemesh.HTMLPage;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.sitemesh.Content;
import com.opensymphony.sitemesh.compatability.HTMLPage2Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Filter Step for wrapping decoration using a page builder. Uses sitemesh to buffer and parse the decorated page
 * before decoration.
 * @since v6.1
 */
public class PageBuilderStep implements FilterStep
{
    private static final String FILTER_LOG_MSG = "PageBuilderFilter[%s].filter decorated servlet path [%s] ";
    private static final Logger log = LoggerFactory.getLogger(PageBuilderStep.class);

    private PageBuilderResponseWrapper responseWrapper;
    private HttpServletResponse wrappedResponse;

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext ctx)
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format(FILTER_LOG_MSG, ctx.getFilterConfig().getFilterName(),
                    ctx.getHttpServletRequest().getServletPath()));
        }

        // The sitemesh code below assumes that we will always be rendering content-type text/html. This assumption
        // is valid as long as this filter is only mapped to to urls that will always return text/html.
        // If we weren't always rendering text/html we could use Sitemesh's PageParserSelector, however it's unclear
        // how to proceed if we encountered a content-type that's not supported by PageParserSelector.shouldParsePage()
        responseWrapper = new PageBuilderResponseWrapper(ctx.getHttpServletResponse());
        wrappedResponse = ctx.getHttpServletResponse();

        getPageBuilderServiceSpi().initForRequest(ctx.getHttpServletRequest(),
                ctx.getHttpServletResponse(), responseWrapper, ctx.getFilterConfig().getServletContext());

        return new FilterCallContextImpl(ctx.getHttpServletRequest(), responseWrapper, ctx.getFilterChain(),
                ctx.getFilterConfig());
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext ctx)
    {
        try
        {
            if (responseWrapper.isBuffering())
            {
                Page page = responseWrapper.getBuffer().parse();
                renderDecoratablePage(new HTMLPage2Content((HTMLPage) page));
            }
            else
            {
                // do nothing - data was written directly through to the response
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Error parsing decorated page '" + ctx.getHttpServletRequest().getServletPath() + "'", ex);
        }
        finally
        {
            getPageBuilderServiceSpi().clearForRequest();
        }
        return new FilterCallContextImpl(ctx.getHttpServletRequest(), wrappedResponse, ctx.getFilterChain(),
                ctx.getFilterConfig());
    }

    private void renderDecoratablePage(final Content content)
    {
        getPageBuilderServiceSpi().getSpi().finish(new DecoratablePage()
        {
            @Override
            public String getTitle()
            {
                return content.getTitle();
            }

            @Override
            public String getMetaProperty(final String key)
            {
                return content.getProperty("meta." + key);
            }

            @Override
            public String getBodyTagProperty(String key)
            {
                return content.getProperty("body." + key);
            }

            @Override
            public String getPageProperty(String key)
            {
                return content.getProperty("page." + key);
            }

            @Override
            public void writeHead(final Writer writer)
            {
                try
                {
                    content.writeHead(writer);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void writeBody(final Writer writer)
            {
                try
                {
                    content.writeBody(writer);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private PageBuilderServiceSpi getPageBuilderServiceSpi()
    {
        return ComponentAccessor.getComponentOfType(PageBuilderServiceSpi.class);
    }
}
