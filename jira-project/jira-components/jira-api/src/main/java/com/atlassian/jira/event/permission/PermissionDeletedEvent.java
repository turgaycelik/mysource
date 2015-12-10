package com.atlassian.jira.event.permission;

import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Event indicating a permission entity has been removed from a permission scheme.
 *
 * @since v5.0
 */
public class PermissionDeletedEvent extends AbstractSchemeEntityEvent
{
    @Internal
    public PermissionDeletedEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        super(schemeId, schemeEntity);
    }

    @Nullable
    public Long getId()
    {
        return getSchemeEntityId();
    }
}
