package com.atlassian.jira.auditing.handlers;

import java.util.List;

import com.atlassian.jira.auditing.AffectedProject;
import com.atlassian.jira.auditing.AffectedScheme;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
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
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.util.NamedWithDescription;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 *
 * @since v6.2
 */
public class SchemeEventHandlerImpl implements SchemeEventHandler
{
    enum SchemeDetail
    {
        // why not to create those keys dynamically? so you can refer from properties file to their usage easily
        WORKFLOW(AuditingCategory.WORKFLOWS, "jira.auditing.workflow.scheme.created",
                "jira.auditing.workflow.scheme.updated",
                "jira.auditing.workflow.scheme.copied",
                "jira.auditing.workflow.scheme.added.to.project",
                "jira.auditing.workflow.scheme.removed.from.project"),

        PERMISSION(AuditingCategory.PERMISSIONS, "jira.auditing.permission.scheme.created",
                "jira.auditing.permission.scheme.updated",
                "jira.auditing.permission.scheme.copied",
                "jira.auditing.permission.scheme.added.to.project",
                "jira.auditing.permission.scheme.removed.from.project"),

        NOTIFICATION(AuditingCategory.NOTIFICATIONS, "jira.auditing.notification.scheme.created",
                NotificationChangeHandlerImpl.SCHEME_UPDATED_I18N_KEY,
                "jira.auditing.notification.scheme.copied",
                "jira.auditing.notification.scheme.added.to.project",
                "jira.auditing.notification.scheme.removed.from.project"),

        FIELDS(AuditingCategory.FIELDS, "jira.auditing.field.layout.scheme.created",
                "jira.auditing.field.layout.scheme.updated",
                "jira.auditing.field.layout.scheme.copied",
                "jira.auditing.field.layout.scheme.added.to.project",
                "jira.auditing.field.layout.scheme.removed.from.project"),

        ISSUE_SECURITY(AuditingCategory.PERMISSIONS, "jira.auditing.issue.security.scheme.created",
                "jira.auditing.issue.security.scheme.updated",
                "jira.auditing.issue.security.scheme.copied",
                "jira.auditing.issue.security.scheme.added.to.project",
                "jira.auditing.issue.security.scheme.removed.from.project");

        private final AuditingCategory category;
        private final String createdSummary;
        private final String updatedSummary;
        private final String copiedSummary;
        private final String addedToProject;
        private final String removedFromProject;

        SchemeDetail(final AuditingCategory category, final String createdSummary, final String updatedSummary, final String copiedSummary,
                final String addedToProject, final String removedFromProject)
        {
            this.category = category;
            this.createdSummary = createdSummary;
            this.updatedSummary = updatedSummary;
            this.copiedSummary = copiedSummary;
            this.addedToProject = addedToProject;
            this.removedFromProject = removedFromProject;
        }
    }

    private final ImmutableMap<String, SchemeDetail> schemeDetails;

    public SchemeEventHandlerImpl()
    {
        schemeDetails = ImmutableMap.<String, SchemeDetail>builder()
                .put(DefaultSchemeFactory.WORKFLOW_SCHEME, SchemeDetail.WORKFLOW)
                .put(DefaultSchemeFactory.PERMISSION_SCHEME, SchemeDetail.PERMISSION)
                .put(DefaultSchemeFactory.NOTIFICATION_SCHEME, SchemeDetail.NOTIFICATION)
                .put(DefaultSchemeFactory.ISSUE_SECURITY_SCHEME, SchemeDetail.ISSUE_SECURITY)
                .build();
    }

    public RecordRequest onSchemeCreatedEvent(final AbstractSchemeEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail scheme = schemeDetails.get(event.getScheme().getType());
        Preconditions.checkNotNull(scheme);

        return new RecordRequest(scheme.category, scheme.createdSummary)
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getId())
                .withChangedValues(computeChangedValues(event.getScheme()));
    }

    public RecordRequest onSchemeCopiedEvent(final AbstractSchemeCopiedEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        final SchemeDetail scheme = schemeDetails.get(event.getScheme().getType());
        Preconditions.checkNotNull(scheme);

        return new RecordRequest(scheme.category, scheme.copiedSummary)
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getId())
                .withAssociatedItems(new AffectedScheme(event.getFromScheme()))
                .withChangedValues(computeChangedValues(event.getScheme()));
    }

    public RecordRequest onSchemeCopiedEvent(final FieldLayoutSchemeCopiedEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        final SchemeDetail scheme = SchemeDetail.FIELDS;

        return new RecordRequest(scheme.category, scheme.copiedSummary)
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getScheme().getId())
                .withAssociatedItems(new AffectedScheme(event.getFromScheme()))
                .withChangedValues(computeChangedValues(event.getScheme()));
    }

    public RecordRequest onSchemeDeletedEvent(final PermissionSchemeDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.PERMISSIONS, "jira.auditing.permission.scheme.deleted")
                .forObject(AssociatedItem.Type.SCHEME, event.getName(), event.getId());
    }

    public RecordRequest onSchemeDeletedEvent(final WorkflowSchemeDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.WORKFLOWS, "jira.auditing.workflow.scheme.deleted")
                .forObject(AssociatedItem.Type.SCHEME, event.getName(), event.getId());
    }

    public RecordRequest onSchemeDeletedEvent(final NotificationSchemeDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.NOTIFICATIONS, "jira.auditing.notification.scheme.deleted")
                .forObject(AssociatedItem.Type.SCHEME, event.getName(), event.getId());
    }

    @Override
    public RecordRequest onSchemeCreatedEvent(final FieldLayoutSchemeCreatedEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail scheme = SchemeDetail.FIELDS;

        return new RecordRequest(scheme.category, scheme.createdSummary)
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getScheme().getId())
                .withChangedValues(computeChangedValues(event.getScheme()));
    }

    @Override
    public RecordRequest onSchemeUpdatedEvent(final FieldLayoutSchemeUpdatedEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail scheme = SchemeDetail.FIELDS;

        return new RecordRequest(scheme.category, scheme.updatedSummary)
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getScheme().getId())
                .withChangedValues(computeChangedValues(event.getOriginalScheme(), event.getScheme()));
    }

    @Override
    public RecordRequest onSchemeDeletedEvent(final FieldLayoutSchemeDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.FIELDS, "jira.auditing.field.layout.scheme.deleted")
                .forObject(AssociatedItem.Type.SCHEME, event.getName(), event.getId());
    }

    @Override
    public RecordRequest onSchemeAddedToProject(FieldLayoutSchemeAddedToProjectEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail details = SchemeDetail.FIELDS;

        return new RecordRequest(details.category, details.addedToProject)
                .forObject(new AffectedProject(event.getProject()))
                .withAssociatedItems(new AffectedScheme(event.getScheme()));
    }

    @Override
    public RecordRequest onSchemeRemovedFromProject(FieldLayoutSchemeRemovedFromProjectEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail details = SchemeDetail.FIELDS;

        return new RecordRequest(details.category, details.removedFromProject)
                .forObject(new AffectedProject(event.getProject()))
                .withAssociatedItems(new AffectedScheme(event.getScheme()));
    }

    public RecordRequest onSchemeUpdatedEvent(final AbstractSchemeUpdatedEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail scheme = schemeDetails.get(event.getScheme().getType());
        Preconditions.checkNotNull(scheme);

        return new RecordRequest(scheme.category, scheme.updatedSummary)
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getId())
                .withChangedValues(computeChangedValues(event.getOriginalScheme(), event.getScheme()));
    }

    @Override
    public RecordRequest onSchemeAddedToProject(AbstractSchemeAddedToProjectEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail details = schemeDetails.get(event.getScheme().getType());
        Preconditions.checkNotNull(details);

        return new RecordRequest(details.category, details.addedToProject)
                .forObject(new AffectedProject(event.getProject()))
                .withAssociatedItems(new AffectedScheme(event.getScheme()));
    }

    @Override
    public RecordRequest onSchemeRemovedFromProject(AbstractSchemeRemovedFromProjectEvent event)
    {
        Preconditions.checkNotNull(event.getScheme());
        SchemeDetail details = schemeDetails.get(event.getScheme().getType());
        Preconditions.checkNotNull(details);

        return new RecordRequest(details.category, details.removedFromProject)
                .forObject(new AffectedProject(event.getProject()))
                .withAssociatedItems(new AffectedScheme(event.getScheme()));
    }

    protected static List<ChangedValue> computeChangedValues(final NamedWithDescription currentScheme)
    {
        return computeChangedValues(null, currentScheme);
    }

    protected static List<ChangedValue> computeChangedValues(final NamedWithDescription originalScheme, final NamedWithDescription currentScheme)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();

        changedValues.addIfDifferent("common.words.name", originalScheme == null ? null : originalScheme.getName(), currentScheme.getName())
                .addIfDifferent("common.words.description", originalScheme == null ? null : originalScheme.getDescription(), currentScheme.getDescription());

        return changedValues.build();
    }
}
