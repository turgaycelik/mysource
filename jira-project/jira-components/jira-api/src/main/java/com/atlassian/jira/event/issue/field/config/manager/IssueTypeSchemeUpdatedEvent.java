package com.atlassian.jira.event.issue.field.config.manager;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

/**
 * Event indicating a field config scheme has been updated.
 *
 * @since v5.1
 */
public class IssueTypeSchemeUpdatedEvent extends AbstractFieldConfigSchemeEvent
{
    public IssueTypeSchemeUpdatedEvent(FieldConfigScheme configScheme)
    {
        super(configScheme);
    }
}
