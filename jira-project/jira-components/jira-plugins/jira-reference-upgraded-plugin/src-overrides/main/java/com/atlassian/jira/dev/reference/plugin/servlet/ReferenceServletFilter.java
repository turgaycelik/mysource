package com.atlassian.jira.dev.reference.plugin.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Reference servlet filter.
 *
 * @since v4.4
 */
public class ReferenceServletFilter implements Filter
{
    public static final String USER_INIT_PARAM = "reference.servlet.filter.USER";

    private String user;

    public void init(FilterConfig filterConfig) throws ServletException
    {
        user = filterConfig.getInitParameter(USER_INIT_PARAM);
        if (user == null)
        {
            user = "Servlet filter user";
        }

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        if (!HttpServletRequest.class.isInstance(servletRequest))
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        String userFromReq = servletRequest.getParameter("user");
        userFromReq = userFromReq != null ? userFromReq : "anonymous";
        String newUser = "UPGRADED " + user + " [REAL USER: " + userFromReq + "]";
        filterChain.doFilter(new ParameterOverridingRequestWrapper((HttpServletRequest) servletRequest, "user", newUser),
                servletResponse);
    }

    public void destroy()
    {
    }


    private static class ParameterOverridingRequestWrapper extends HttpServletRequestWrapper
    {

        private final String paramName;
        private final String paramValue;

        public ParameterOverridingRequestWrapper(HttpServletRequest request, String paramName, String paramValue)
        {
            super(request);
            this.paramName = paramName;
            this.paramValue = paramValue;
        }

        @Override
        public String getParameter(String name)
        {
            if (name.equals(paramName))
            {
                return paramValue;
            }
            return super.getParameter(name);
        }
    }
}
