package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been unreleased
 *
 * @since v4.4
 */
public class VersionUnreleaseEvent extends AbstractVersionEvent
{
    @Internal
    public VersionUnreleaseEvent(@Nonnull Version version)
    {
        super(version);
    }
}
