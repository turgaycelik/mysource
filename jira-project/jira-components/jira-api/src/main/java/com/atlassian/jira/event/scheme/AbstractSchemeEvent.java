package com.atlassian.jira.event.scheme;

import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.scheme.Scheme;

/**
 * Abstract event that captures the data relevant to scheme events, e.g. permission schemes, notification schemes etc.
 *
 * @since v5.0
 */
@Internal
public class AbstractSchemeEvent
{
    private Scheme scheme;

    public AbstractSchemeEvent(@Nullable Scheme scheme)
    {
        this.scheme = scheme;
    }

    @Nullable
    public Long getId()
    {
        return scheme != null ? scheme.getId() : null;
    }

    @Nullable
    public Scheme getScheme()
    {
        return scheme;
    }
}
