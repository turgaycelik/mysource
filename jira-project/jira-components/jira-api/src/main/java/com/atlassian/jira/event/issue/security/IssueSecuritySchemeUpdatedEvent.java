package com.atlassian.jira.event.issue.security;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating an issue security scheme has been updated.
 *
 * @since v5.0
 */
public class IssueSecuritySchemeUpdatedEvent extends AbstractSchemeUpdatedEvent
{
    /**
     * @deprecated Use {@link #IssueSecuritySchemeUpdatedEvent(com.atlassian.jira.scheme.Scheme, com.atlassian.jira.scheme.Scheme)}. Since v6.2
     */
    @Deprecated
    @Internal
    public IssueSecuritySchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme, null);
    }

    @Internal
    public IssueSecuritySchemeUpdatedEvent(Scheme scheme, Scheme originalScheme)
    {
        super(scheme, originalScheme);
    }
}
