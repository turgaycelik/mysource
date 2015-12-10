package com.atlassian.jira.event.scheme;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.scheme.Scheme;

/**
 * Abstract event that captures the data relevant to a scheme being copied, e.g. permission schemes, notification schemes etc.
 *
 * @since v5.0
 */
@Internal
public class AbstractSchemeCopiedEvent extends AbstractSchemeEvent
{
    private Scheme fromScheme;

    public AbstractSchemeCopiedEvent(@Nonnull Scheme fromScheme, @Nonnull Scheme toScheme)
    {
        super(toScheme);
        this.fromScheme = fromScheme;
    }

    @Nonnull
    public Long getCopiedFromId()
    {
        return fromScheme.getId();
    }

    @Nonnull
    public Long getCopiedToId()
    {
        return getScheme().getId();
    }

    @Nonnull
    public Scheme getFromScheme()
    {
        return fromScheme;
    }
}
