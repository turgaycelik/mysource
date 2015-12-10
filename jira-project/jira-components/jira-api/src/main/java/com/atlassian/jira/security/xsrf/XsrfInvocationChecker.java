package com.atlassian.jira.security.xsrf;

import com.atlassian.annotations.PublicApi;
import webwork.action.Action;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Checks that a web-request (either WebWork action or HttpServlet) has been invoked with the correct
 * XSRF token.
 *
 * @since v4.1.1
 */
@PublicApi
public interface XsrfInvocationChecker
{
    /**
     * This is the same name that Confluences uses in their webwork2 world so we are using the same name for synergy
     * reasons
     */
    String REQUIRE_SECURITY_TOKEN = "RequireSecurityToken";
    String X_ATLASSIAN_TOKEN = "X-Atlassian-Token";

    /**
     * Checks that the action about to be executed has been invoked within the correct XSRF parameters. This method
     * will only perform the check if the current "command" is annotated with {@link com.atlassian.jira.security.xsrf.RequiresXsrfCheck}.
     *
     * @param action      the {@link webwork.action.ActionSupport} in play. Cannot be null.
     * @param parameters  the parameters this has been called with. Cannot be null.
     * @return false if the action failed the XSRF check.
     */
    XsrfCheckResult checkActionInvocation(Action action, Map<String, ?> parameters);

    /**
     * Checks that the web request contains the correct XSRF parameters.
     *
     * @param httpServletRequest the {@link javax.servlet.http.HttpServletRequest} in play. Can't be null.
     * @return false if the request failed the XSRF check.
     */
    XsrfCheckResult checkWebRequestInvocation(HttpServletRequest httpServletRequest);
}
