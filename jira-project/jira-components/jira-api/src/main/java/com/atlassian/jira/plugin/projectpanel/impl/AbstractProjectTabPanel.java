package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Map;

/**
 * Abstract tab panel for browse project.
 */
@PublicSpi
public abstract class AbstractProjectTabPanel implements ProjectTabPanel
{
    protected ProjectTabPanelModuleDescriptor descriptor;
    final protected JiraAuthenticationContext authenticationContext;

    protected AbstractProjectTabPanel(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.authenticationContext = jiraAuthenticationContext;
    }

    /**
     * This constructor is for the benefit of plugins which may be expecting there to be a no-arg constructor for this
     * and sub classes.
     */
    protected AbstractProjectTabPanel()
    {
        this.authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }

    public void init(ProjectTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getHtml(final BrowseContext ctx)
    {
        return descriptor.getHtml("view", createVelocityParams(ctx));
    }

    /**
     * Creates new map of velocity parameters. By default this map contains the context params from
     * {@link BrowseContext}. Note that it does not explicitly add the default velocity params or the i18n bean,
     * since these are added in {@link com.atlassian.jira.plugin.JiraResourcedModuleDescriptor#getHtml(String, java.util.Map)}.
     *
     * Extenders of AbstractProjectTabPanel should override this method to specify precise parameters required for
     * their velocity template.
     *
     * @param ctx browse context
     * @return new velocity parameters map
     */
    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        return ctx.createParameterMap();
    }
}