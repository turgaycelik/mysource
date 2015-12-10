package com.atlassian.jira.web.pagebuilder;

import com.atlassian.webresource.api.UrlMode;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Abstract superclass for decorators rendered by JSPs. These decorators require 4 JSPs:
 * <ul>
 *     <li>One rendered before the decorated page's head</li>
 *     <li>One rendered after the decorated page's head</li>
 *     <li>One rendered before the decorated page's body</li>
 *     <li>One rendered after the decorated page's body</li>
 * </ul>
 * @since v6.1
 */
public abstract class AbstractJspDecorator implements Decorator, JspDecorator
{
    protected final WebResourceAssembler webResourceAssembler;
    protected final String headPrePath;
    protected final String headPostPath;
    protected final String bodyPrePath;
    protected final String bodyPostPath;

    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;

    protected AbstractJspDecorator(final WebResourceAssembler webResourceAssembler,
            final String headPrePath, final String headPostPath, final String bodyPrePath, final String bodyPostPath)
    {
        this.webResourceAssembler = webResourceAssembler;
        this.headPrePath = headPrePath;
        this.headPostPath = headPostPath;
        this.bodyPrePath = bodyPrePath;
        this.bodyPostPath = bodyPostPath;
    }

    @Override
    public void setContext(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
    {
        this.servletContext = servletContext;
        this.request = request;
        this.response = response;
    }

    @Override
    public void writePreHead(final Writer writer)
    {
        writeTemplate(headPrePath);
    }

    @Override
    public void writeOnFlush(final Writer writer)
    {
        webResourceAssembler.assembled().drainIncludedResources().writeHtmlTags(writer, UrlMode.RELATIVE);
    }

    @Override
    public void writePostHead(final Writer writer, final DecoratablePage.ParsedHead parsedHead)
    {
        try
        {
            JspDecoratorUtils.setParsedHead(parsedHead);
            writeTemplate(headPostPath);
        }
        finally
        {
            JspDecoratorUtils.clearParsedHead();
        }
    }

    @Override
    public void writePreBody(final Writer writer, final DecoratablePage.ParsedBody parsedBody)
    {
        try
        {
            JspDecoratorUtils.setParsedBody(parsedBody);
            writeTemplate(bodyPrePath);
        }
        finally
        {
            JspDecoratorUtils.clearParsedBody();
        }
    }

    @Override
    public void writePostBody(final Writer writer, final DecoratablePage.ParsedBody parsedBody)
    {
        try
        {
            JspDecoratorUtils.setParsedBody(parsedBody);
            writeTemplate(bodyPostPath);
        }
        finally
        {
            JspDecoratorUtils.clearParsedBody();
        }
    }

    private void writeTemplate(String path)
    {
        try
        {
            servletContext.getRequestDispatcher(path).include(request, response);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (ServletException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
