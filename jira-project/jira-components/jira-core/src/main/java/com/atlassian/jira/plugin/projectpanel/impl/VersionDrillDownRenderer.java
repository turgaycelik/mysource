package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;

import java.util.Collection;

/**
 * Renderer to render a collection of versions.
 *
 * @since v6.2
 */
public interface VersionDrillDownRenderer
{
    String getHtml(BrowseContext ctx, String uniqueKey, Collection<Version> versions);

    String getNavigatorUrl(Project project, Version version, ProjectComponent component);
}
