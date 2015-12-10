/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.encoding.AbstractEncodingFilter;
import org.apache.commons.lang.StringUtils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import static com.atlassian.jira.web.filters.InitParamSupport.optional;

/**
 * This filter sets fixed request and response encoding, supplied either as constructor parameter (for programatic usage),
 * or as an init parameter.
 */
public class FixedEncodingFilter extends AbstractEncodingFilter
{
    public static enum InitParams
    {
        ENCODING(optional("jira.encoding.filter.encoding")),
        CONTENT_TYPE(optional("jira.encoding.filter.contentType"));

        private final InitParamSupport support;

        private InitParams(InitParamSupport support)
        {
            this.support = support;
        }

        public String key()
        {
            return support.key();
        }

        String get(FilterConfig config)
        {
            return support.get(config);
        }
    }

    private String encoding;
    private String contentType;

    public FixedEncodingFilter()
    {
        this(null);
    }

    public FixedEncodingFilter(final String encoding)
    {
        this(encoding, null);
    }

    public FixedEncodingFilter(final String encoding, final String contentType)
    {
        this.encoding = encoding;
        this.contentType = contentType;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        setEncodingIfNecessary(filterConfig);
        validateEncoding();
        setContentTypeIfNecessary(filterConfig);
    }

    private void setEncodingIfNecessary(final FilterConfig filterConfig)
    {
        if (!encodingSet())
        {
            encoding = InitParams.ENCODING.get(filterConfig);
        }
    }

    private void setContentTypeIfNecessary(final FilterConfig filterConfig)
    {
        if (!contentTypeSet())
        {
            setContentType(filterConfig);
        }
    }

    private boolean encodingSet()
    {
        return StringUtils.isNotEmpty(encoding);
    }

    private boolean contentTypeSet()
    {
        return StringUtils.isNotEmpty(contentType);
    }

    private void validateEncoding()
    {
        if (!encodingSet())
        {
            throw new IllegalStateException("Encoding not provided");
        }
        checkEncodingValue();
    }

    private void checkEncodingValue()
    {
        try
        {
            Charset.forName(encoding);
        }
        catch (UnsupportedCharsetException e)
        {
            throw new IllegalStateException("Charset unsupported", e);
        }
        catch (IllegalCharsetNameException e)
        {
            throw new IllegalStateException("No such charset", e);
        }
    }


    private void setContentType(final FilterConfig filterConfig)
    {
        contentType = InitParams.CONTENT_TYPE.get(filterConfig);
        if (!contentTypeSet())
        {
            contentType = defaultContentType();
        }
    }

    private String defaultContentType()
    {
        return String.format("text/html; charset=%s", encoding);
    }


    protected String getEncoding()
    {
        return encoding;
    }

    protected String getContentType()
    {
        return contentType;
    }
}
