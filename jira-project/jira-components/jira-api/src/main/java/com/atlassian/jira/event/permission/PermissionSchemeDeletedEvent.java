package com.atlassian.jira.event.permission;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeDeletedEvent;

/**
 * Event indicating a permission scheme has been deleted.
 *
 * @since v5.0
 */
public class PermissionSchemeDeletedEvent extends AbstractSchemeDeletedEvent
{
    /**
     *
     * @deprecated Please use {@link #PermissionSchemeDeletedEvent(Long, String)}. Since v6.2
     */
    @Deprecated
    @Internal
    public PermissionSchemeDeletedEvent(@Nonnull Long id)
    {
        super(id, null);
    }

    @Internal
    public PermissionSchemeDeletedEvent(@Nonnull Long id, @Nonnull String name)
    {
        super(id, name);
    }
}
