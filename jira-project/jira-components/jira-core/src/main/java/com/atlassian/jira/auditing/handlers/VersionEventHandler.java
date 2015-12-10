package com.atlassian.jira.auditing.handlers;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.project.AbstractVersionEvent;
import com.atlassian.jira.event.project.VersionArchiveEvent;
import com.atlassian.jira.event.project.VersionMergeEvent;
import com.atlassian.jira.event.project.VersionReleaseEvent;
import com.atlassian.jira.event.project.VersionUnarchiveEvent;
import com.atlassian.jira.event.project.VersionUnreleaseEvent;
import com.atlassian.jira.event.project.VersionUpdatedEvent;

public interface VersionEventHandler
{
    @Nonnull
    RecordRequest onVersionCreateEvent(@Nonnull AbstractVersionEvent event);

    @Nonnull
    RecordRequest onVersionDeleteEvent(@Nonnull AbstractVersionEvent event);

    @Nonnull
    RecordRequest onVersionUnreleaseEvent(@Nonnull VersionUnreleaseEvent event);

    @Nonnull
    RecordRequest onVersionUnarchiveEvent(@Nonnull VersionUnarchiveEvent event);

    @Nonnull
    RecordRequest onVersionReleaseEvent(@Nonnull VersionReleaseEvent event);

    @Nonnull
    RecordRequest onVersionMergeEvent(@Nonnull VersionMergeEvent event);

    @Nonnull
    RecordRequest onVersionArchiveEvent(@Nonnull VersionArchiveEvent event);

    @Nonnull
    Option<RecordRequest> onVersionUpdatedEvent(@Nonnull VersionUpdatedEvent event);
}
