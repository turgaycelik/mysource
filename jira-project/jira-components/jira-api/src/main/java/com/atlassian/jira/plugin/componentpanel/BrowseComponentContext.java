package com.atlassian.jira.plugin.componentpanel;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.project.browse.BrowseContext;

/**
 * Maintains the context for the Component tabs.
 */
public interface BrowseComponentContext extends BrowseContext
{
    /**
     * Retrieve the component for this context
     * @return A {@link ProjectComponent}
     */
    ProjectComponent getComponent();
}
