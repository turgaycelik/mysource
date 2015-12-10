package com.atlassian.jira.bc.project.version.remotelink;

import javax.annotation.Nonnull;

import com.atlassian.jira.entity.remotelink.RemoteEntityLinkImpl;
import com.atlassian.jira.project.version.Version;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.1
 */
public class RemoteVersionLinkImpl extends RemoteEntityLinkImpl<Version> implements RemoteVersionLink
{
    private final Version version;

    public RemoteVersionLinkImpl(@Nonnull Version version, @Nonnull String globalId, @Nonnull String json)
    {
        super(globalId, json);
        this.version = notNull("version", version);
    }

    @Override
    public Version getEntity()
    {
        return version;
    }

    @Nonnull
    @Override
    public Long getEntityId()
    {
        return version.getId();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final RemoteVersionLinkImpl that = (RemoteVersionLinkImpl) o;
        return version.equals(that.version) &&
                globalId.equals(that.globalId) &&
                jsonRef.getJson().equals(that.jsonRef.getJson());
    }

    @Override
    public int hashCode()
    {
        int hash = version.hashCode();
        hash = 31 * hash + globalId.hashCode();
        hash = 31 * hash + jsonRef.getJson().hashCode();
        return hash;
    }

    @Override
    public String toString()
    {
        return "RemoteVersionLinkImpl[version=" + version +
                ",globalId=" + globalId +
                ",json=" + jsonRef.getJson().length() +
                ']';
    }
}
