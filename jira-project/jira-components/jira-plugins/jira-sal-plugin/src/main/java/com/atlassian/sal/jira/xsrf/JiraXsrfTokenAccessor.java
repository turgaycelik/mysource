package com.atlassian.sal.jira.xsrf;

import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Reads the token from a given http request and can add a new xsrf token to the cookie of a request.
 */
public class JiraXsrfTokenAccessor implements XsrfTokenAccessor
{
    private XsrfTokenGenerator xsrfTokenGenerator;

    public JiraXsrfTokenAccessor(XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.xsrfTokenGenerator = xsrfTokenGenerator;
    }

    @Override
    public String getXsrfToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, boolean create)
    {
        return xsrfTokenGenerator.generateToken(httpServletRequest, create);
    }
}
