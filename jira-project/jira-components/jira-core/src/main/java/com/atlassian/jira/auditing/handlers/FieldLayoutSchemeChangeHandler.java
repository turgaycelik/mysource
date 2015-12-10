package com.atlassian.jira.auditing.handlers;

import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.fields.layout.AbstractFieldLayoutSchemeEntityEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityUpdatedEvent;

/**
 * @since v6.2
 */
public interface FieldLayoutSchemeChangeHandler
{
    RecordRequest onFieldLayoutSchemeEntityEvent(AbstractFieldLayoutSchemeEntityEvent event);

    RecordRequest onFieldLayoutSchemeEntityUpdatedEvent(FieldLayoutSchemeEntityUpdatedEvent event);
}
