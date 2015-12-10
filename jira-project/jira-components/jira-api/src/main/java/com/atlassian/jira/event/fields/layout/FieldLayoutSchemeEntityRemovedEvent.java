package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;

/**
 * @since v6.2
 */
public class FieldLayoutSchemeEntityRemovedEvent extends AbstractFieldLayoutSchemeEntityEvent
{
    public FieldLayoutSchemeEntityRemovedEvent(@Nonnull final FieldLayoutScheme scheme, final FieldLayoutSchemeEntity entity)
    {
        super(scheme, entity);
    }
}
