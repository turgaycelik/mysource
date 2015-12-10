package com.atlassian.jira.web.pagebuilder;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.opensymphony.module.sitemesh.RequestConstants;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of PageBuilder and PageBuilderSpi interfaces
 * @since v6.1
 */
public class DefaultPageBuilder implements PageBuilder, PageBuilderSpi
{
    private final ApplicationProperties applicationProperties;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final DecoratorListener decoratorListener;
    private final ServletContext servletContext;
    private final FeatureManager featureManager;

    private boolean firstByteSent = false;
    private boolean preHeadSent = false;
    private boolean finished = false;
    private Decorator decorator;

    public DefaultPageBuilder(final ApplicationProperties applicationProperties,
            final HttpServletRequest request, final HttpServletResponse response,
            final DecoratorListener decoratorListener, final ServletContext servletContext,
            FeatureManager featureManager)
    {
        this.applicationProperties = applicationProperties;
        this.request = request;
        this.response = response;
        this.decoratorListener = decoratorListener;
        this.servletContext = servletContext;
        this.featureManager = featureManager;
    }

    @Override
    public void setDecorator(Decorator decorator)
    {
        if (firstByteSent)
        {
            throw new IllegalStateException("Cannot set decorator after first byte has been sent");
        }
        this.decorator = decorator;
        this.decoratorListener.onDecoratorSet();
        if (decorator instanceof JspDecorator)
        {
            ((JspDecorator) decorator).setContext(servletContext, request, response);
        }
        // This disables sitemesh decoration
        this.request.setAttribute(RequestConstants.DISABLE_BUFFER_AND_DECORATION, true);
    }

    @Override
    public void flush()
    {
        if (null == this.decorator)
        {
            throw new IllegalStateException("Attempting to flush before setting decorator");
        }
        firstByteSent = true;
        try
        {
            if (!preHeadSent)
            {
                preHeadSent = true;
                // TODO: applicationProperties lookup will need to call an SPI when PageBuilder becomes x-product
                response.setContentType(applicationProperties.getContentType());
                response.setCharacterEncoding(applicationProperties.getEncoding());
                if (featureManager.isEnabled("com.atlassian.plugins.SEND_HEAD_EARLY.nginx-proxy-disable"))
                {
                    // Disable nginx proxy buffering (http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_buffering)
                    // This is done because in OnDemand, the nginx http proxy module in OnDemand will buffer content
                    // before it's passed to the gzip module. We don't want this, we want it to be buffered in gzip
                    // and flushed when we flush here.
                    response.addHeader("X-Accel-Buffering", "no");
                }
                decorator.writePreHead(response.getWriter());
            }
            decorator.writeOnFlush(response.getWriter());

            response.getWriter().flush();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void finish(DecoratablePage page)
    {
        if (finished)
        {
            throw new IllegalStateException("Attempting to call PageBuilder.finish() multiple times");
        }
        finished = true;
        try
        {
            if (null == this.decorator)
            {
                page.writeHead(response.getWriter());
                page.writeBody(response.getWriter());
            }
            else
            {
                flush();
                page.writeHead(response.getWriter());
                decorator.writePostHead(response.getWriter(), new DefaultParsedHead(page));
                decorator.writePreBody(response.getWriter(), new DefaultParsedBodyTag(page));
                page.writeBody(response.getWriter());
                decorator.writePostBody(response.getWriter(), new DefaultParsedBodyTag(page));
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private class DefaultParsedHead implements DecoratablePage.ParsedHead
    {
        private final DecoratablePage page;

        private DefaultParsedHead(final DecoratablePage page)
        {
            this.page = page;
        }

        @Override
        public String getTitle()
        {
            return page.getTitle();
        }

        @Override
        public String getMetaProperty(final String key)
        {
            return page.getMetaProperty(key);
        }
    }

    private class DefaultParsedBodyTag implements DecoratablePage.ParsedBody
    {
        private final DecoratablePage page;

        private DefaultParsedBodyTag(final DecoratablePage page)
        {
            this.page =  page;
        }

        @Override
        public String getBodyTagProperty(final String key)
        {
            return page.getBodyTagProperty(key);
        }

        @Override
        public String getPageProperty(final String key)
        {
            return page.getPageProperty(key);
        }
    }
}
