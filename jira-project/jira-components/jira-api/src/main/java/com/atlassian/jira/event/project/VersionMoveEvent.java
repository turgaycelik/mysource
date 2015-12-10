package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been moved
 *
 * @since v4.4
 */
public class VersionMoveEvent extends AbstractVersionEvent
{
    @Internal
    public VersionMoveEvent(@Nonnull Version version)
    {
        super(version);
    }
}
