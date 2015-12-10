package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating a version has been created
 *
 * @since v4.4
 */
public class VersionCreateEvent extends AbstractVersionEvent
{
    @Internal
    public VersionCreateEvent(@Nonnull Version version)
    {
        super(version);
    }
}
