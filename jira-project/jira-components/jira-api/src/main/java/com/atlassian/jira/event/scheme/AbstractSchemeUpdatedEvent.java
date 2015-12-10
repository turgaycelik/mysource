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
public class AbstractSchemeUpdatedEvent extends AbstractSchemeEvent
{
    private Scheme originalScheme;

    public AbstractSchemeUpdatedEvent(@Nullable Scheme scheme, @Nullable Scheme originalScheme)
    {
        super(scheme);
        this.originalScheme = originalScheme;
    }

    @Nullable
    public Scheme getOriginalScheme()
    {
        return originalScheme;
    }
}
