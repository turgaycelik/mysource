package com.atlassian.jira.plugin.versionpanel;

import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;

/**
 * Maintains the current context for browsing a version.
 *
 * @since v4.0
 */
public interface BrowseVersionContext extends BrowseContext
{

    /**
     * Returns version
     *
     * @return version
     */
    Version getVersion();

}
