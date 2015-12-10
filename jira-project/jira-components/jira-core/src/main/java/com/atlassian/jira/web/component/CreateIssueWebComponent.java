package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Map;

/**
 * This component is responsible for rendering the create issue widget for browse project page.
 *
 * @since v4.0
 */
public class CreateIssueWebComponent extends AbstractWebComponent
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    public CreateIssueWebComponent(final VelocityTemplatingEngine templatingEngine, final ApplicationProperties applicationProperties, final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext)
    {
        super(templatingEngine, applicationProperties);
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    public String getHtml(Project project)
    {
        final User user = authenticationContext.getLoggedInUser();
        if (!show(project, user))
        {
            return "";
        }

        // Include the JavaScript that we require
        WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:jira-global");

        final Map<String, Object> velocityParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);

        // TODO: Fix the path to Velocity once created
        return getHtml("templates/jira/multipicker/pickertable.vm", velocityParams);
    }

    /**
     * Returns true is user has permission to create issues for given project. If the project passed in is null, this
     * method will return false.
     *
     * @param project project
     * @param user user, can be null
     * @return true is user has permission to create issues for given project
     */
    boolean show(Project project, User user)
    {
        return project != null && permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
    }

}
