package com.atlassian.jira.event.issue.field.config.manager;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

/**
 * Event indicating a field config scheme has been deleted.
 *
 * @since v5.1
 */
public class IssueTypeSchemeDeletedEvent extends AbstractFieldConfigSchemeEvent
{
    public IssueTypeSchemeDeletedEvent(FieldConfigScheme configScheme)
    {
        super(configScheme);
    }
}
