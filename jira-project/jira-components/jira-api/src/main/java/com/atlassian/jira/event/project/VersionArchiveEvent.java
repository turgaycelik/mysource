package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been archived
 *
 * @since v4.4
 */
public class VersionArchiveEvent extends AbstractVersionEvent
{
    @Internal
    public VersionArchiveEvent(@Nonnull Version version)
    {
        super(version);
    }
}
