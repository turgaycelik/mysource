package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

import com.google.common.base.Objects;

/**
 * Event indicating a version has been merged into another
 *
 * @since v4.4
 */
public class VersionMergeEvent extends AbstractVersionEvent
{
    private final Version mergedVersion;

    @Internal
    public VersionMergeEvent(@Nonnull Version version, @Nonnull Version mergedVersion)
    {
        super(version);
        this.mergedVersion = mergedVersion;
    }

    /**
     * Get the ID of the version that was merged into this version. Note that the version for this ID will no longer
     * exist when this event is published.
     *
     * @return The ID of the version that was merged into this version
     */
    public long getMergedVersionId()
    {
        return mergedVersion.getId();
    }

    @Nonnull
    public Version getMergedVersion()
    {
        return mergedVersion;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        VersionMergeEvent that = (VersionMergeEvent) o;

        if (!Objects.equal(mergedVersion.getId(), that.mergedVersion.getId())) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (mergedVersion.getId() ^ (mergedVersion.getId() >>> 32));
        return result;
    }
}
