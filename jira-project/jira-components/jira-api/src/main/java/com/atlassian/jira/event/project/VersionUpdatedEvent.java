package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * @since v6.3
 */
public class VersionUpdatedEvent extends AbstractVersionEvent
{
    private Version originalVersion;

    @Internal
    public VersionUpdatedEvent(@Nonnull final Version version, @Nonnull final Version originalVersion)
    {
        super(version);
        this.originalVersion = originalVersion;
    }

    @Nonnull
    public Version getOriginalVersion()
    {
        return originalVersion;
    }
}
