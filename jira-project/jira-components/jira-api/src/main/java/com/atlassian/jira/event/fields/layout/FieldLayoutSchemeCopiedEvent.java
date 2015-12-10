package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;

/**
 * @since v6.2
 */
public class FieldLayoutSchemeCopiedEvent extends AbstractFieldLayoutEvent
{
    private final FieldLayoutSchemeDetails originalScheme;

    public FieldLayoutSchemeCopiedEvent(@Nonnull FieldLayoutScheme fromScheme, @Nonnull FieldLayoutScheme toScheme)
    {
        super(toScheme);
        this.originalScheme = createFieldLayoutSchemeDetails(fromScheme);
    }

    @Nonnull
    public FieldLayoutSchemeDetails getFromScheme()
    {
        return originalScheme;
    }
}
