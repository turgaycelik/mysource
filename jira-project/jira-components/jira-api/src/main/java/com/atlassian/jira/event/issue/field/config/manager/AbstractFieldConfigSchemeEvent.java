package com.atlassian.jira.event.issue.field.config.manager;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

/**
 * Abstract event that captures the data relevant to field config scheme events.
 *
 * @since v5.1
 */
public class AbstractFieldConfigSchemeEvent
{
    private Long id;

    public AbstractFieldConfigSchemeEvent(FieldConfigScheme configScheme)
    {
        if (null != configScheme)
        {
            this.id = configScheme.getId();
        }
    }

    public Long getId()
    {
        return id;
    }
}
