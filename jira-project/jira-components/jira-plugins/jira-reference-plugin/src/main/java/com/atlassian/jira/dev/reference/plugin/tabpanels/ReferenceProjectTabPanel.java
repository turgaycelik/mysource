package com.atlassian.jira.dev.reference.plugin.tabpanels;

import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * Represents a simple reference project tab panel.
 *
 * @since v4.3
 */
public class ReferenceProjectTabPanel extends AbstractProjectTabPanel
{
    public ReferenceProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(jiraAuthenticationContext);
    }

    @Override
    public boolean showPanel(BrowseContext ctx)
    {
        return true;
    }
}
