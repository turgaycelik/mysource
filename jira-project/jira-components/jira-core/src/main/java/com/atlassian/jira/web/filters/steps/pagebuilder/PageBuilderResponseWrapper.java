package com.atlassian.jira.web.filters.steps.pagebuilder;

import com.atlassian.jira.web.pagebuilder.DecoratorListener;
import com.opensymphony.module.sitemesh.filter.Buffer;
import com.opensymphony.module.sitemesh.filter.HttpContentType;
import com.opensymphony.module.sitemesh.parser.HTMLPageParser;
import com.opensymphony.module.sitemesh.scalability.NoopScalabilitySupport;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Response wrapper that is aware of decoration. If decorating is turned on, the response is buffered so that it can
 * be parsed later.
 * @since 6.1
 */
class PageBuilderResponseWrapper extends HttpServletResponseWrapper implements DecoratorListener
{
    private Buffer buffer;
    private boolean contentHasBeenWritten;
    private boolean decoratorHasBeenSet;

    PageBuilderResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }

    @Override
    public void onDecoratorSet()
    {
        if (contentHasBeenWritten)
        {
            throw new IllegalStateException("Cannot set decorator after content type has been written");
        }
        decoratorHasBeenSet = true;
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        contentHasBeenWritten = true;
        // We can assume that the content type has been set before this method is called to get a writer for writing.
        // If the content type has been set and we are decorating, buffer will be initiated and we should return the
        // buffering writer. Otherwise we pass through to the underlying writer.
        if (null != buffer)
        {
            return buffer.getWriter();
        }
        else
        {
            return getResponse().getWriter();
        }
    }

    @Override
    public void setContentType(String type)
    {
        if (decoratorHasBeenSet && type != null)
        {
            HttpContentType httpContentType = new HttpContentType(type);
            buffer = new Buffer(new HTMLPageParser(), httpContentType.getEncoding(), new NoopScalabilitySupport());
        }
        super.setContentType(type);
    }

    Buffer getBuffer()
    {
        return buffer;
    }

    boolean isBuffering()
    {
        return null != buffer;
    }
}
