package com.atlassian.jira.dev.reference.plugin.tabpanels;

import com.atlassian.jira.plugin.projectpanel.impl.GenericProjectTabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Map;

/**
 * Represents a simple reference project tab panel.
 *
 * @since v4.3
 */
public class ReferenceProjectTabPanel extends GenericProjectTabPanel
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public ReferenceProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(jiraAuthenticationContext, fieldVisibilityManager);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    public String getHtml(BrowseContext ctx)
    {
        final Map<String, Object> startingParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        startingParams.put("i18n", new I18nBean(ctx.getUser()));
        startingParams.put("project", ctx.getProject());
        startingParams.put("fieldVisibility", fieldVisibilityManager);
        startingParams.put("reloaded", "yes");
        return descriptor.getHtml("view", startingParams);
    }

    @Override
    public boolean showPanel(BrowseContext ctx)
    {
        return true;
    }
}
