package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been released
 *
 * @since v4.4
 */
public class VersionReleaseEvent extends AbstractVersionEvent
{
    @Internal
    public VersionReleaseEvent(@Nonnull Version version)
    {
        super(version);
    }
}
