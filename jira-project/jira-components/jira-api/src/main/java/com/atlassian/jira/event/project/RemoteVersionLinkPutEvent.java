package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Event indicating that a remote version link has been created or updated.
 *
 * @since v6.1.1
 */
public class RemoteVersionLinkPutEvent extends AbstractRemoteVersionLinkEvent
{
    @Internal
    public RemoteVersionLinkPutEvent(final Version version, @Nonnull final String globalId)
    {
        super(version, notNull("globalId", globalId));
    }
}
