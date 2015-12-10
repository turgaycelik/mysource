package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * A Generic implementation of a {@link com.atlassian.jira.plugin.versionpanel.VersionTabPanel}.  Dellegates to the
 * view resource for rendering..
 *
 * @deprecated Extend {@link AbstractProjectTabPanel} instead. Since v5.0.
 */
public class GenericVersionsProjectTabPanel extends GenericProjectTabPanel
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public GenericVersionsProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(jiraAuthenticationContext, fieldVisibilityManager);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public GenericVersionsProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this(jiraAuthenticationContext, ComponentAccessor.getComponent(FieldVisibilityManager.class));
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS, null);
    }


}
