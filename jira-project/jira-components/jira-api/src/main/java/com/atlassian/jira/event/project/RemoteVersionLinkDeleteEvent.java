package com.atlassian.jira.event.project;

import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating that one or more remote version links have been deleted.
 * Note that the {@link #getGlobalId() global ID} may be {@code null}, indicating
 * that all remote links for that version were deleted.
 *
 * @since v6.1.1
 */
public class RemoteVersionLinkDeleteEvent extends AbstractRemoteVersionLinkEvent
{
    @Internal
    public RemoteVersionLinkDeleteEvent(final Version version, @Nullable final String globalId)
    {
        super(version, globalId);
    }
}
