package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.jira.project.version.Version;

/**
 * Event indicating if a version is created via REST (which means project admin in most cases using the RESTful table).
 * This will be used to establish a baseline to compare against creating versions inline.
 *
 * @since v6.3
 */
@EventName ("administration.projects.versions.version.created.rest")
public class VersionCreatedViaRestEvent extends AbstractVersionEvent
{
    public VersionCreatedViaRestEvent(@Nonnull final Version version)
    {
        super(version);
    }
}
