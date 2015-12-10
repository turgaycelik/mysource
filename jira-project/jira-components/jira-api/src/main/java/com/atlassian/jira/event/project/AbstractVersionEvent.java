package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.version.Version;

import com.google.common.base.Objects;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract event for versions
 *
 * @since v4.4
 */
public class AbstractVersionEvent
{
    private final Version version;

    public AbstractVersionEvent(long versionId)
    {
        this.version = notNull("Version should exist", ComponentAccessor.getVersionManager().getVersion(versionId));
    }

    public AbstractVersionEvent(@Nonnull Version version)
    {
        this.version = version;
    }

    /**
     * Get the ID of the version this event occurred on
     *
     * @return The ID of the version
     */
    public long getVersionId()
    {
        return version.getId();
    }

    @Nonnull
    public Version getVersion()
    {
        return version;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AbstractVersionEvent that = (AbstractVersionEvent) o;

        if (!Objects.equal(version.getId(), that.version.getId())) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (version.getId() ^ (version.getId() >>> 32));
    }
}
