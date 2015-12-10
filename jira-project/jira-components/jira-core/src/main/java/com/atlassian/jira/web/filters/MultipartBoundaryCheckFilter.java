package com.atlassian.jira.web.filters;

import java.io.IOException;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This filter protects against CVE-2014-0050: Exploit with Boundaries, Loops without Boundaries
 *
 * @since v6.2
 */
public class MultipartBoundaryCheckFilter implements Filter
{
    private static final int CONTENT_TYPE_BOUNDARY_LENGTH_LIMIT = 70; // RFC1341
    private static final String MULTIPART = "multipart";
    private static final String FORM_DATA = "form-data";
    private static final String BOUNDARY = "boundary";

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException
    {
        final String contentType = request.getContentType();

        if (contentType != null && isMultiPartBoundary(contentType))
        {
            String boundary = getMultiPartBoundary(contentType);
            if (boundary == null || boundary.length() > CONTENT_TYPE_BOUNDARY_LENGTH_LIMIT)
            {
                throw new RuntimeException("Error parsing Content-Type header");
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isMultiPartBoundary(final String contentType)
    {
        if (contentType == null)
        {
            return false;
        }

        return contentType.contains(MULTIPART) && contentType.contains(FORM_DATA) && contentType.contains(BOUNDARY);
    }

    private String getMultiPartBoundary(final String contentType)
    {
        if (contentType.contains(BOUNDARY))
        {
            try
            {
                return new ContentType(contentType).getParameter(BOUNDARY);
            }
            catch (ParseException e)
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void destroy()
    {

    }
}
