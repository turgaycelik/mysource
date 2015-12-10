package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.event.scheme.AbstractSchemeDeletedEvent;

/**
 * @since v6.2
 */
public class FieldLayoutSchemeDeletedEvent extends AbstractSchemeDeletedEvent
{
    public FieldLayoutSchemeDeletedEvent(@Nonnull final Long id, @Nullable final String name)
    {
        super(id, name);
    }
}
