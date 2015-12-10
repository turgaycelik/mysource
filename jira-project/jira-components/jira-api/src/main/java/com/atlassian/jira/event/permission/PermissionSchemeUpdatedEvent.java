package com.atlassian.jira.event.permission;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a permission scheme has been updated.
 *
 * @since v5.0
 */
public class PermissionSchemeUpdatedEvent extends AbstractSchemeUpdatedEvent
{
    /**
     * @deprecated Use {@link #PermissionSchemeUpdatedEvent(com.atlassian.jira.scheme.Scheme, com.atlassian.jira.scheme.Scheme)}. Since v6.2
     */
    @Deprecated
    @Internal
    public PermissionSchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme, null);
    }

    @Internal
    public PermissionSchemeUpdatedEvent(Scheme scheme, Scheme originalScheme)
    {
        super(scheme, originalScheme);
    }
}
