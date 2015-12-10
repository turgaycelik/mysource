package com.atlassian.jira.issue.fields.event;

import javax.annotation.Nonnull;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.jira.event.project.AbstractVersionEvent;
import com.atlassian.jira.project.version.Version;

/**
 * Event to throw when a fix version is created inline
 */
@EventName ("administration.projects.versions.version.fixfor.created.inline")
public class FixVersionCreatedInline extends AbstractVersionEvent
{
    public FixVersionCreatedInline(@Nonnull final Version version)
    {
        super(version);
    }
}
