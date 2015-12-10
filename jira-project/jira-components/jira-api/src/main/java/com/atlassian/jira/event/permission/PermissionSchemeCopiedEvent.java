package com.atlassian.jira.event.permission;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a permission scheme has been copied.
 *
 * @since v5.0
 */
public class PermissionSchemeCopiedEvent extends AbstractSchemeCopiedEvent
{
    @Internal
    public PermissionSchemeCopiedEvent(Scheme fromScheme, Scheme toScheme)
    {
        super(fromScheme, toScheme);
    }
}
