package com.atlassian.jira.issue.fields.event;

import javax.annotation.Nonnull;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.jira.event.project.AbstractVersionEvent;
import com.atlassian.jira.project.version.Version;

/**
 * Event to throw when an affects version is created inline
 */
@EventName ("administration.projects.versions.version.affected.created.inline")
public class AffectedVersionCreatedInlineEvent extends AbstractVersionEvent
{
    public AffectedVersionCreatedInlineEvent(@Nonnull final Version version)
    {
        super(version);
    }
}
