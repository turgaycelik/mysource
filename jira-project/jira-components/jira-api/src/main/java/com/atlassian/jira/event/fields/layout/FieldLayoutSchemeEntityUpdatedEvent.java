package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;

/**
 * @since v6.2
 */
public class FieldLayoutSchemeEntityUpdatedEvent extends AbstractFieldLayoutSchemeEntityEvent
{
    private final FieldLayoutSchemeEntityDetails originalEntity;

    public FieldLayoutSchemeEntityUpdatedEvent(@Nonnull final FieldLayoutScheme scheme,
            @Nonnull final FieldLayoutSchemeEntity originalEntity,
            @Nonnull final FieldLayoutSchemeEntity entity)
    {
        super(scheme, entity);
        this.originalEntity = createDetails(originalEntity);
    }

    @Nonnull
    public FieldLayoutSchemeEntityDetails getOriginalEntityDetails()
    {
        return originalEntity;
    }
}
