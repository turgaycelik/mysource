package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;

/**
 * @since v6.2
 */
public class FieldLayoutSchemeCreatedEvent extends AbstractFieldLayoutEvent
{
    public FieldLayoutSchemeCreatedEvent(@Nonnull FieldLayoutScheme scheme)
    {
        super(scheme);
    }
}
