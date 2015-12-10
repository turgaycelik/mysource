package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;

/**
 * @since v6.2
 */
public class AbstractFieldLayoutEvent
{
    private final FieldLayoutSchemeDetails scheme;

    public AbstractFieldLayoutEvent(@Nonnull final FieldLayoutScheme scheme)
    {
        this.scheme = createFieldLayoutSchemeDetails(scheme);
    }

    protected FieldLayoutSchemeDetails createFieldLayoutSchemeDetails(final FieldLayoutScheme scheme)
    {
        return new FieldLayoutSchemeDetails()
        {
            @Override
            public Long getId()
            {
                return scheme.getId();
            }

            @Override
            public String getDescription()
            {
                return scheme.getDescription();
            }

            @Override
            public String getName()
            {
                return scheme.getName();
            }
        };
    }

    @Nonnull
    public FieldLayoutSchemeDetails getScheme()
    {
        return scheme;
    }
}
