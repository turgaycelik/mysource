package com.atlassian.jira.event.issue.field.config.manager;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

/**
 * Event indicating a field config scheme has been created.
 *
 * @since v5.1
 */
public class IssueTypeSchemeCreatedEvent extends AbstractFieldConfigSchemeEvent
{
    public IssueTypeSchemeCreatedEvent(FieldConfigScheme configScheme)
    {
        super(configScheme);
    }
}
