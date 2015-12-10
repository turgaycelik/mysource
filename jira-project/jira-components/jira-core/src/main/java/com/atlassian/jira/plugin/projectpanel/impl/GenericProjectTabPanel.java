package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Map;

/**
 * A generic implementation of a {@link com.atlassian.jira.plugin.projectpanel.ProjectTabPanel}. Delegates to the
 * view resource for rendering.
 *
 * @deprecated Please extend {@link AbstractProjectTabPanel} instead. Since v5.0.
 */
public class GenericProjectTabPanel extends AbstractProjectTabPanel
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public GenericProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(jiraAuthenticationContext);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    /**
     * @deprecated Please use {@link #GenericProjectTabPanel(com.atlassian.jira.security.JiraAuthenticationContext, com.atlassian.jira.web.FieldVisibilityManager)}
     * instead.
     */
    @SuppressWarnings ( { "JavaDoc" })
    @Deprecated
    public GenericProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(jiraAuthenticationContext);
        this.fieldVisibilityManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
    }

    public String getHtml(BrowseContext ctx)
    {
        final Map<String, Object> startingParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        startingParams.put("i18n", authenticationContext.getI18nHelper());
        startingParams.put("project", ctx.getProject());
        startingParams.put("fieldVisibility", fieldVisibilityManager);
        return descriptor.getHtml("view", startingParams);
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return !(fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS, null) &&
                fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.COMPONENTS, null));
    }
}
