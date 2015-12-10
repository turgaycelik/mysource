package com.atlassian.jira.event.issue.security;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating an issue security scheme has been created.
 *
 * @since v5.0
 */
public class IssueSecuritySchemeCreatedEvent extends AbstractSchemeEvent
{
    public IssueSecuritySchemeCreatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
