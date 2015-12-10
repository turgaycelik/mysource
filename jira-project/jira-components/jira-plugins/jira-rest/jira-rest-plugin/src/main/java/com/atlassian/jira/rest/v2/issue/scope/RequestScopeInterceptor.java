package com.atlassian.jira.rest.v2.issue.scope;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;

import java.lang.reflect.InvocationTargetException;

/**
 * This interceptor can be used in plugins that define &lt;rest&gt; modules. In conjunction with the {@link
 * RequestScope}, plugins can have define request-scoped beans in Spring.
 *
 * @since v4.2
 */
public class RequestScopeInterceptor implements ResourceInterceptor
{
    /**
     * The scope that this interceptor uses.
     */
    private final RequestScope scope;

    /**
     * Creates a new RequestScopeInterceptor.
     *
     * @param scope a RequestScope
     */
    public RequestScopeInterceptor(RequestScope scope)
    {
        this.scope = scope;
    }

    /**
     * Uses the RequestScope to begin and destroy a Request. This has the side effect of setting and clearing the
     * "current" MethodInvocation.
     *
     * @param invocation a MethodInvocation containing information about the invocation
     */
    public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        RequestScope.Request request = scope.beginRequest(invocation);
        try
        {
            // run next interceptor
            invocation.invoke();
        }
        finally
        {
            request.destroy();
        }
    }
}
