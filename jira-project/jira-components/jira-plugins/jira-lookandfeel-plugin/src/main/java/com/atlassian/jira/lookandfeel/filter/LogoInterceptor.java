package com.atlassian.jira.lookandfeel.filter;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.plugin.servlet.ResourceDownloadUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The entire purpose of this filter is to redirect all Logo requests to the logo uploaded by sysadmins.
 */
public class LogoInterceptor implements Filter
{
    private FilterConfig config;
    private JiraHome jiraHome;
    private final String JIRA_LOGO="jira-logo-scaled.png";


    public void init(final FilterConfig filterConfig) throws ServletException
    {
        this.config = filterConfig;
        this.jiraHome = ComponentAccessor.getComponent(JiraHome.class);
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException
    {
        if(request instanceof HttpServletRequest) {
            final HttpServletResponse res = (HttpServletResponse )response;
            final HttpServletRequest req = (HttpServletRequest)request;
            String requestURL = req.getRequestURL().toString();
            if(requestURL.endsWith(JIRA_LOGO)) {
                ImageDownloader downloader = new ImageDownloader();
                downloader.doDownload(req, res, config.getServletContext(), jiraHome.getHomePath()+"/logos/"+JIRA_LOGO, true);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void destroy() { }
}
