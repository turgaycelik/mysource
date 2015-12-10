package com.atlassian.jira.util.velocity;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Function;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class MockVelocityRequestContextFactory implements VelocityRequestContextFactory
{
    private final String staticBaseUrl;
    private String currentUrl;

    public MockVelocityRequestContextFactory(final String baseUrl)
    {
        this(baseUrl, null);
    }

    public MockVelocityRequestContextFactory(final String baseUrl, final String staticBaseUrl)
    {
        this.currentUrl = baseUrl;
        this.staticBaseUrl = staticBaseUrl;
    }

    @Override
    public VelocityRequestContext getJiraVelocityRequestContext()
    {
        return new SimpleVelocityRequestContext(currentUrl);
    }

    @Override
    public Map<String, Object> getDefaultVelocityParams(final Map<String, Object> startingParams, final JiraAuthenticationContext authenticationContext)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void cacheVelocityRequestContext(final VelocityRequestContext velocityRequestContext)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void clearVelocityRequestContext()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setVelocityRequestContext(final HttpServletRequest request)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setVelocityRequestContext(final String baseUrl, final HttpServletRequest request)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setVelocityRequestContext(final VelocityRequestContext velocityRequestContext)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nullable
    @Override
    public <I, O> O runWithStaticBaseUrl(@Nullable final I input, @Nonnull final Function<I, O> runnable)
    {
        String lastUrl = currentUrl;
        currentUrl = staticBaseUrl;
        try
        {
            return runnable.apply(input);
        }
        finally
        {
            currentUrl = lastUrl;
        }
    }
}
