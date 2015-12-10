package com.atlassian.jira.util.velocity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.http.JiraUrl;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * Return an instance of {@link VelocityRequestContext}, depending on whether we are called from a web or non-web context.
 * <p/>
 * The original intention of this class is to get around bugs such as JRA-11038, where velocity fragments are called from both
 * web and non-web contexts.  Originally we tried to proxy HttpServletRequest, but it makes more sense to have a specific interface
 * {@link VelocityRequestContext}.
 */
public class DefaultVelocityRequestContextFactory implements VelocityRequestContextFactory
{
    private final ApplicationProperties applicationProperties;

    public DefaultVelocityRequestContextFactory(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    /**
     * @deprecated Please use {@link #DefaultVelocityRequestContextFactory(com.atlassian.jira.config.properties.ApplicationProperties)} instead
     */
    public DefaultVelocityRequestContextFactory()
    {
        this(ComponentAccessor.getApplicationProperties());
    }

    public VelocityRequestContext getJiraVelocityRequestContext()
    {
        VelocityRequestContext cachedRequestContext = getCachedContext();
        if (cachedRequestContext != null)
        {
            return cachedRequestContext;
        }
        else
        {
            return createStaticContext();
        }
    }

    @Override
    public Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, JiraAuthenticationContext authenticationContext)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
    }

    /**
     * Called from a servlet filter.  Passes the {@link javax.servlet.http.HttpServletRequest#getContextPath()} along as the baseUrl.
     *
     * @param request The HttpServletRequest used to construct the {@link com.atlassian.jira.util.velocity.RequestContextParameterHolder}
     * @deprecated Use {@link VelocityRequestContextFactory#setVelocityRequestContext(javax.servlet.http.HttpServletRequest)} instead. Since v5.0.
     */
    public static void cacheVelocityRequestContext(HttpServletRequest request)
    {
        cacheVelocityRequestContext(request.getContextPath(), request);
    }

    @Override
    public void setVelocityRequestContext(HttpServletRequest request)
    {
        setVelocityRequestContext(request.getContextPath(), request);
    }

    /**
     * Should be called from a servlet filter before the request gets a chance to run
     *
     * @param baseUrl Should pass in {@link javax.servlet.http.HttpServletRequest#getContextPath()}
     * @param request The HttpServletRequest used to construct the {@link com.atlassian.jira.util.velocity.RequestContextParameterHolder}
     * @deprecated Use {@link VelocityRequestContextFactory#setVelocityRequestContext(String, javax.servlet.http.HttpServletRequest)} instead. Since v5.0.
     */
    public static void cacheVelocityRequestContext(String baseUrl, HttpServletRequest request)
    {
        RequestContextParameterHolder requestContextParameterHolder = null;
        String canonicalBaseURL = baseUrl;
        VelocityRequestSession session = null;
        if (request != null)
        {
            requestContextParameterHolder = new RequestContextParameterHolderImpl(request);
            canonicalBaseURL = JiraUrl.constructBaseUrl(request);
            session = new HttpSessionBackedVelocityRequestSession(request);
        }
        final VelocityRequestContext velocityRequestContext = new SimpleVelocityRequestContext(baseUrl, canonicalBaseURL, requestContextParameterHolder, session);
        JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.VELOCITY_REQUEST_CONTEXT, velocityRequestContext);
    }

    @Override
    public void setVelocityRequestContext(String baseUrl, HttpServletRequest request)
    {
        cacheVelocityRequestContext(baseUrl, request);
    }

    /**
     * @deprecated Use {@link VelocityRequestContextFactory#setVelocityRequestContext(VelocityRequestContext)} instead. Since v5.0.
     */
    public void cacheVelocityRequestContext(final VelocityRequestContext velocityRequestContext)
    {
        setVelocityRequestContext(velocityRequestContext);
    }

    @Override
    public void setVelocityRequestContext(final VelocityRequestContext velocityRequestContext)
    {
        getRequestCache().put(RequestCacheKeys.VELOCITY_REQUEST_CONTEXT, velocityRequestContext);
    }

    @Override
    public <I, O> O runWithStaticBaseUrl(final I input, @Nonnull final Function<I, O> runnable)
    {
        final VelocityRequestContext cachedContext = getCachedContext();
        setVelocityRequestContext(createStaticContext());
        try
        {
            return runnable.apply(input);
        }
        finally
        {
            if (cachedContext == null)
            {
                clearVelocityRequestContext();
            }
            else
            {
                setVelocityRequestContext(cachedContext);
            }
        }
    }

    public void clearVelocityRequestContext()
    {
        getRequestCache().remove(RequestCacheKeys.VELOCITY_REQUEST_CONTEXT);
    }

    @VisibleForTesting
    Map<String, Object> getRequestCache()
    {
        return JiraAuthenticationContextImpl.getRequestCache();
    }

    private VelocityRequestContext createStaticContext()
    {
        return new SimpleVelocityRequestContext(applicationProperties.getString(APKeys.JIRA_BASEURL));
    }

    private VelocityRequestContext getCachedContext()
    {
        return (VelocityRequestContext) getRequestCache().get(RequestCacheKeys.VELOCITY_REQUEST_CONTEXT);
    }
}
