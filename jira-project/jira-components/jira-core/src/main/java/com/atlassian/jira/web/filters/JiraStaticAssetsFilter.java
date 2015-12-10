package com.atlassian.jira.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This filter is used to serve static filed through default {@link RequestDispatcher}.</p>
 * <p>It means that <b>all</b> requests which contains this filter will not be passed further along the chain. </p>
 * <p>{@link JiraStaticAssetsFilter} was introduced during JRADEV-21366 to serve static files regardless the state of JIRA stack underneath.</p>
 *
 * @since 6.1
 */
public class JiraStaticAssetsFilter implements Filter
{

    private static final Logger log = LoggerFactory.getLogger(JiraStaticAssetsFilter.class);
    private RequestDispatcher defaultDispatcher;

    public void init(FilterConfig config) throws ServletException
    {
        defaultDispatcher = config.getServletContext().getNamedDispatcher("default");
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        log.debug("Serving path '{}' as a regular file", request.getRequestURI());
        defaultDispatcher.forward(req, resp);
    }


    public void destroy()
    {
    }

}
