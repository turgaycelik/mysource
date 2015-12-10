package com.atlassian.jira.event.scheme;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.Named;

/**
 * Abstract event that captures the data relevant to scheme deleted events, e.g. permission scheme deleted, notification scheme deleted etc.
 *
 * @since v6.2
 */
@Internal
public class AbstractSchemeDeletedEvent implements Named
{
    private Long id;
    private String name;

    public AbstractSchemeDeletedEvent(@Nonnull Long id, @Nullable String name)
    {
        this.id = id;
        this.name = name;
    }

    public Long getId()
    {
        return id;
    }

    @Nullable
    public String getName()
    {
        return name;
    }
}
