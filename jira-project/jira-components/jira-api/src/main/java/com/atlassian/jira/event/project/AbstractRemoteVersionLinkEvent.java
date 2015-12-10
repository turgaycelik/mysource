package com.atlassian.jira.event.project;

import com.atlassian.jira.project.version.Version;

import com.google.common.base.Objects;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract event for remote version links.
 *
 * @since v6.1.1
 */
public abstract class AbstractRemoteVersionLinkEvent extends AbstractVersionEvent
{
    private final String globalId;

    protected AbstractRemoteVersionLinkEvent(final Version version, final String globalId)
    {
        super(notNull("version", version));
        this.globalId = globalId;
    }

    public String getGlobalId()
    {
        return globalId;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final AbstractRemoteVersionLinkEvent that = (AbstractRemoteVersionLinkEvent)o;
        return getVersionId() == that.getVersionId() && Objects.equal(globalId, that.globalId);
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + (globalId != null ? globalId.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[versionId=" + getVersionId() + ",globalId=" + globalId + ']';
    }
}
