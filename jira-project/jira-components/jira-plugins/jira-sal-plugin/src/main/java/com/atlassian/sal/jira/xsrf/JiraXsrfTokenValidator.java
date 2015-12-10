package com.atlassian.sal.jira.xsrf;

import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Validates if a request contains a valid xsrf token.
 */
public class JiraXsrfTokenValidator implements XsrfTokenValidator
{
    private XsrfInvocationChecker xsrfInvocationChecker;

    public JiraXsrfTokenValidator(XsrfInvocationChecker xsrfInvocationChecker)
    {
        this.xsrfInvocationChecker = xsrfInvocationChecker;
    }

    @Override
    public boolean validateFormEncodedToken(HttpServletRequest request)
    {
        XsrfCheckResult xsrfCheckResult = xsrfInvocationChecker.checkWebRequestInvocation(request);
        return xsrfCheckResult.isValid();
    }

    @Override
    public String getXsrfParameterName()
    {
        return XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY;
    }
}
