package com.atlassian.jira.plugin.componentpanel.impl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.10
 */
@PublicSpi
public class GenericTabPanel implements ComponentTabPanel
{
    protected ComponentTabPanelModuleDescriptor descriptor;
    protected final ProjectManager projectManager;
    protected final JiraAuthenticationContext authenticationContext;
    private final FieldVisibilityManager fieldVisibilityManager;

    /**
     * Deprecated constructor.
     *
     * @param projectManager the ProjectManager
     * @param authenticationContext the JiraAuthenticationContext
     *
     * @deprecated Use {@link #GenericTabPanel(com.atlassian.jira.project.ProjectManager, com.atlassian.jira.security.JiraAuthenticationContext, com.atlassian.jira.web.FieldVisibilityManager)} instead. Since v4.4.
     */
    public GenericTabPanel(final ProjectManager projectManager, final JiraAuthenticationContext authenticationContext)
    {
        this(projectManager, authenticationContext, ComponentAccessor.getComponent(FieldVisibilityManager.class));
    }

    public GenericTabPanel(final ProjectManager projectManager, final JiraAuthenticationContext authenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.projectManager = projectManager;
        this.authenticationContext = authenticationContext;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public void init(ComponentTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getHtml(BrowseComponentContext context)
    {
        final Map<String, Object> startingParams = createVelocityParams(context);
        startingParams.put("fieldVisibility", fieldVisibilityManager);

        return descriptor.getHtml("view", startingParams);
    }

    public boolean showPanel(BrowseComponentContext context)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(context.getComponent().getProjectId(), IssueFieldConstants.COMPONENTS);
    }

    protected Map<String, Object> createVelocityParams(BrowseComponentContext context)
    {
        final Map<String, Object> startingParams = new HashMap<String, Object>();
        startingParams.put("project", context.getProject());
        startingParams.put("component", context.getComponent());
        startingParams.put("componentContext", context);
        return startingParams;
    }

}
