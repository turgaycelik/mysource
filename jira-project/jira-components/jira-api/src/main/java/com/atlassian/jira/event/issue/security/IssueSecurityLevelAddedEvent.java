package com.atlassian.jira.event.issue.security;

import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Event indicating a security level has been added to an issue security scheme.
 *
 * @since v5.0
 */
public class IssueSecurityLevelAddedEvent extends AbstractSchemeEntityEvent
{
    public IssueSecurityLevelAddedEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        super(schemeId, schemeEntity);
    }
}
