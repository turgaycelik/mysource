package com.atlassian.jira.plugin.versionpanel.impl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.query.clause.Clause;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.10
 */
@PublicSpi
public class GenericTabPanel implements VersionTabPanel
{
    private VersionTabPanelModuleDescriptor descriptor;
    protected final JiraAuthenticationContext authenticationContext;
    protected final SearchProvider searchProvider;
    private final FieldVisibilityManager fieldVisibilityManager;

    public GenericTabPanel(final JiraAuthenticationContext authenticationContext, final SearchProvider searchProvider,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.authenticationContext = authenticationContext;
        this.searchProvider = searchProvider;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    /**
     * @deprecated Use {@link #GenericTabPanel(com.atlassian.jira.security.JiraAuthenticationContext, com.atlassian.jira.issue.search.SearchProvider, com.atlassian.jira.web.FieldVisibilityManager)}
     * instead. Since 4.4.
     */
    @Deprecated
    public GenericTabPanel(final JiraAuthenticationContext authenticationContext, final SearchProvider searchProvider)
    {
        this(authenticationContext, searchProvider, ComponentAccessor.getComponent(FieldVisibilityManager.class));
    }


    public void init(VersionTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getHtml(BrowseVersionContext context)
    {
        final Map<String, Object> startingParams = createVelocityParams(context);
        return descriptor.getHtml("view", startingParams);
    }

    protected StatisticAccessorBean createStatisticAccessorBean(BrowseVersionContext context, boolean limitToUnresolved)
    {
        Clause searchClause = JqlQueryBuilder.newClauseBuilder().fixVersion().eq(context.getVersion().getId()).buildClause();
        return new StatisticAccessorBean(authenticationContext.getLoggedInUser(), context.getProject().getId(), searchClause, limitToUnresolved);
    }

    protected Map<String, Object> createVelocityParams(BrowseVersionContext context)
    {
        final Map<String, Object> startingParams = new HashMap<String, Object>();

        startingParams.put("project", context.getProject());
        startingParams.put("versionContext", context);

        return startingParams;
    }

    /**
     * Returns true if fix for version field is visible, false otherwise.
     *
     * @param context version context
     * @return true if fix for version field is visible, false otherwise
     */
    public boolean showPanel(BrowseVersionContext context)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(context.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS);
    }
}
