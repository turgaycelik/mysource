package com.atlassian.jira.auditing.handlers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.AffectedProject;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.project.AbstractVersionEvent;
import com.atlassian.jira.event.project.VersionArchiveEvent;
import com.atlassian.jira.event.project.VersionMergeEvent;
import com.atlassian.jira.event.project.VersionReleaseEvent;
import com.atlassian.jira.event.project.VersionUnarchiveEvent;
import com.atlassian.jira.event.project.VersionUnreleaseEvent;
import com.atlassian.jira.event.project.VersionUpdatedEvent;
import com.atlassian.jira.project.version.Version;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

public class VersionEventHandlerImpl implements VersionEventHandler
{
    @Nonnull
    @Override
    public RecordRequest onVersionCreateEvent(@Nonnull AbstractVersionEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.created")
                .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()))
                .withChangedValues(computeChangedValues(null, event.getVersion()));
    }

    @Nonnull
    @Override
    public RecordRequest onVersionDeleteEvent(@Nonnull final AbstractVersionEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.deleted")
                .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()));
    }

    @Nonnull
    @Override
    public RecordRequest onVersionUnreleaseEvent(@Nonnull final VersionUnreleaseEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.unreleased")
                .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()));
    }

    @Nonnull
    @Override
    public RecordRequest onVersionUnarchiveEvent(@Nonnull final VersionUnarchiveEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.unarchived")
                .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()));
    }

    @Nonnull
    @Override
    public RecordRequest onVersionReleaseEvent(@Nonnull final VersionReleaseEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.released")
                .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()));
    }

    @Nonnull
    @Override
    public RecordRequest onVersionMergeEvent(@Nonnull final VersionMergeEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.merged")
                .forObject(AssociatedItem.Type.VERSION, event.getMergedVersion().getName(), event.getMergedVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()))
                .withChangedValues(computeChangedValues(event.getMergedVersion(), event.getVersion()));
    }

    @Nonnull
    @Override
    public Option<RecordRequest> onVersionUpdatedEvent(@Nonnull final VersionUpdatedEvent event)
    {
        final List<ChangedValue> values = computeChangedValues(event.getOriginalVersion(), event.getVersion());
        if (!values.isEmpty())
        {
            return Option.some(new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.updated")
                    .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                    .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()))
                    .withChangedValues(values));
        }
        return Option.none();
    }

    @Nonnull
    @Override
    public RecordRequest onVersionArchiveEvent(@Nonnull final VersionArchiveEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.version.archived")
                .forObject(AssociatedItem.Type.VERSION, event.getVersion().getName(), event.getVersionId())
                .withAssociatedItems(new AffectedProject(event.getVersion().getProjectObject()));
    }

    @Nullable
    String getReleaseDate(@Nullable Version version)
    {
        if (version == null || version.getReleaseDate() == null)
        {
            return null;
        }
        return new DateTime(version.getReleaseDate()).toString(new DateTimeFormatterBuilder().append(ISODateTimeFormat.date()).toFormatter());
    }

    @Nullable
    String getStartDate(@Nullable Version version)
    {
        if (version == null || version.getStartDate() == null)
        {
            return null;
        }
        return new DateTime(version.getStartDate()).toString(new DateTimeFormatterBuilder().append(ISODateTimeFormat.date()).toFormatter());
    }

    @Nonnull
    protected List<ChangedValue> computeChangedValues(@Nullable final Version version, @Nonnull final Version currentVersion)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();

        changedValues.addIfDifferent("common.words.name", version == null ? null : version.getName(), currentVersion.getName())
                .addIfDifferent("common.words.description", version == null ? null : version.getDescription(), currentVersion.getDescription())
                .addIfDifferent("version.startdate", getStartDate(version), getStartDate(currentVersion))
                .addIfDifferent("version.releasedate", getReleaseDate(version), getReleaseDate(currentVersion));

        return changedValues.build();
    }
}
