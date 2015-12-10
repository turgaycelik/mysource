package com.atlassian.jira.auditing.handlers;

import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeAddedToProjectEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeCopiedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeCreatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeDeletedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeUpdatedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeDeletedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeDeletedEvent;

/**
 *
 * @since v6.2
 */
public interface SchemeEventHandler
{
    RecordRequest onSchemeCreatedEvent(AbstractSchemeEvent event);

    RecordRequest onSchemeDeletedEvent(PermissionSchemeDeletedEvent event);

    RecordRequest onSchemeDeletedEvent(WorkflowSchemeDeletedEvent event);

    RecordRequest onSchemeUpdatedEvent(AbstractSchemeUpdatedEvent event);

    RecordRequest onSchemeAddedToProject(AbstractSchemeAddedToProjectEvent event);

    RecordRequest onSchemeAddedToProject(FieldLayoutSchemeAddedToProjectEvent event);

    RecordRequest onSchemeRemovedFromProject(AbstractSchemeRemovedFromProjectEvent event);

    RecordRequest onSchemeRemovedFromProject(FieldLayoutSchemeRemovedFromProjectEvent event);

    RecordRequest onSchemeCopiedEvent(AbstractSchemeCopiedEvent event);

    RecordRequest onSchemeDeletedEvent(NotificationSchemeDeletedEvent event);

    RecordRequest onSchemeCreatedEvent(FieldLayoutSchemeCreatedEvent event);

    RecordRequest onSchemeUpdatedEvent(FieldLayoutSchemeUpdatedEvent event);

    RecordRequest onSchemeDeletedEvent(FieldLayoutSchemeDeletedEvent event);

    RecordRequest onSchemeCopiedEvent(FieldLayoutSchemeCopiedEvent event);
}
