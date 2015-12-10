package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been deleted
 *
 * @since v4.4
 */
public class VersionDeleteEvent extends AbstractVersionEvent
{
    @Internal
    public VersionDeleteEvent(@Nonnull Version version)
    {
        super(version);
    }
}
