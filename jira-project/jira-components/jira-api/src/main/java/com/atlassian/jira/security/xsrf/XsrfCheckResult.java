package com.atlassian.jira.security.xsrf;

import com.atlassian.annotations.PublicApi;

/**
 * The result of the XSRF checks
 */
@PublicApi
public interface XsrfCheckResult
{
    /**
     * @return true if the invocation check was required to be performed
     */
    boolean isRequired();
    
    /**
     * @return true if the invocation check passed
     */
    boolean isValid();

    /**
     * @return true if there was an authenticated user in the original request
     */
    boolean isGeneratedForAuthenticatedUser();
}
