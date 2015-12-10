/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.opensymphony.module.sitemesh.filter.PageFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SitemeshExcludePathFilter extends AbstractHttpFilter
{
    PageFilter filter = new PageFilter();
    private static final String ATTACHMENT_PATH = "/secure/attachment";
    private static final String THUMBNAIL_PATH = "/secure/thumbnail";
    private static final String APPLET_PATH = "/secure/applet";

    public void init(FilterConfig filterConfig)
    {
        filter.init(filterConfig);
    }

    public void doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {        
        if (servletRequest != null && (ATTACHMENT_PATH.equals(servletRequest.getServletPath()) || THUMBNAIL_PATH.equals(servletRequest.getServletPath())) ||
                (servletRequest != null && servletRequest.getServletPath() != null && servletRequest.getServletPath().startsWith(APPLET_PATH)))
        {
            //we don't want attachments 'sitemesh'ed'
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else
        {
            filter.doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    public void destroy()
    {
        filter.destroy();
    }
}
