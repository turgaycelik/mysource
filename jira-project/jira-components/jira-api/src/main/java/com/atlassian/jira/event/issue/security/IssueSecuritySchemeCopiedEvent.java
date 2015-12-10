package com.atlassian.jira.event.issue.security;

import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating an issue security scheme has been copied.
 *
 * @since v5.0
 */
public class IssueSecuritySchemeCopiedEvent extends AbstractSchemeCopiedEvent
{
    public IssueSecuritySchemeCopiedEvent(Scheme fromScheme, Scheme toScheme)
    {
        super(fromScheme, toScheme);
    }
}
