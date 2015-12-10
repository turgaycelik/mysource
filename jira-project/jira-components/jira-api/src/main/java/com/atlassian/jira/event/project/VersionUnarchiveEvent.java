package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been unarchived
 *
 * @since v4.4
 */
public class VersionUnarchiveEvent extends AbstractVersionEvent
{
    @Internal
    public VersionUnarchiveEvent(@Nonnull Version version)
    {
        super(version);
    }
}
