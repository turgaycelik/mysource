package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;

/**
 * @since v6.2
 */
public class FieldLayoutSchemeUpdatedEvent extends AbstractFieldLayoutEvent
{
    private final FieldLayoutSchemeDetails originalScheme;

    public FieldLayoutSchemeUpdatedEvent(@Nonnull FieldLayoutScheme scheme, @Nonnull FieldLayoutScheme originalScheme)
    {
        super(scheme);
        this.originalScheme = createFieldLayoutSchemeDetails(originalScheme);
    }

    @Nonnull
    public FieldLayoutSchemeDetails getOriginalScheme()
    {
        return originalScheme;
    }
}
