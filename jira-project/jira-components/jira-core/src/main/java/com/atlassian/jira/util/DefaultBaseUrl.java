package com.atlassian.jira.util;

import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

/**
 * @since v5.2
 */
public class DefaultBaseUrl implements BaseUrl
{
    private final VelocityRequestContextFactory factory;

    public DefaultBaseUrl(VelocityRequestContextFactory factory)
    {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public String getBaseUrl()
    {
        return trimToEmpty(factory.getJiraVelocityRequestContext().getBaseUrl());
    }

    @Nonnull
    @Override
    public String getCanonicalBaseUrl()
    {
        return trimToEmpty(factory.getJiraVelocityRequestContext().getCanonicalBaseUrl());
    }

    @Nullable
    @Override
    public <I, O> O runWithStaticBaseUrl(@Nullable final I input, @Nonnull final Function<I, O> runnable)
    {
        return factory.runWithStaticBaseUrl(input, runnable);
    }
}
