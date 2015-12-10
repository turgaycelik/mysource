package com.atlassian.jira.plugin.webfragment;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.annotations.VisibleForTesting;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Decorator for {@link CacheableContextProvider} instances that stores the result of a {@link #getContextMap(java.util.Map)}
 * invocation in a request attribute. The uniqueness of the cache key is given by {@link CacheableContextProvider#getUniqueContextKey(java.util.Map)},
 * since it is possible for multiple {@link #getContextMap(java.util.Map)} invocations to be made in the same request with
 * different contexts.
 *
 * @since v4.4
 */
public class CacheableContextProviderDecorator implements ContextProvider
{
    @VisibleForTesting
    static final String REQUEST_ATTRIBUTE_PREFIX = "com.atlassian.jira.request.scoped.context.provider:";

    private final CacheableContextProvider contextProvider;

    public CacheableContextProviderDecorator(final CacheableContextProvider contextProvider)
    {
        this.contextProvider = contextProvider;
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final String key = REQUEST_ATTRIBUTE_PREFIX + contextProvider.getClass().getName() + ":" +
                contextProvider.getUniqueContextKey(context);

        final HttpServletRequest request = getRequest(context);
        if (request != null)
        {
            Map<String, Object> generatedContext = (Map<String, Object>) request.getAttribute(key);
            if (generatedContext == null)
            {
                generatedContext = initContextMap(context);
                request.setAttribute(key, generatedContext);
            }
            return new HashMap<String, Object>(generatedContext);
        }

        return initContextMap(context);
    }


    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        contextProvider.init(params);
    }

    /**
     * Generate the context that should be cached.
     *
     * @param context the context passed into the getContextMap method.
     * @return the context to store in the session.
     */
    private Map<String, Object> initContextMap(Map<String, Object> context)
    {
        return contextProvider.getContextMap(context);
    }

    /**
     * Method that retrieves the HttpServletRequest by the numerous methods that JIRA uses. It tries to get it from: the
     * context passed in (jirahelper or request), then from ExecutingHttpRequest, and then from the ActionContext
     *
     * @param context the context passed into the getContextMap method.
     * @return the current request.
     */
    protected HttpServletRequest getRequest(Map<String, Object> context)
    {
        HttpServletRequest request;
        JiraHelper jiraHelper = (JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER);
        if (jiraHelper != null)
        {
            request = jiraHelper.getRequest();
            if (request != null)
            {
                return request;
            }
        }
        final Object o = context.get("request");
        if (o != null && o instanceof HttpServletRequest)
        {
            request = (HttpServletRequest) o;
            return request;
        }

        request = ExecutingHttpRequest.get();
        if (request != null)
        {
            return request;
        }

        return ActionContext.getRequest();

    }
}
