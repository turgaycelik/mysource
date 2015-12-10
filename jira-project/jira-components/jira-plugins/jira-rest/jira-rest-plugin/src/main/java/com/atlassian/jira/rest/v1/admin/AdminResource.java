package com.atlassian.jira.rest.v1.admin;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Parent Resource for administration resources.
 *
 * @since v4.0
 */
@Path("admin")
@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@CorsAllowed
public class AdminResource
{

    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final OptionSetManager optionSetManager;
    private final UserProjectHistoryManager projectHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;

    public AdminResource(PermissionManager permissionManager, JiraAuthenticationContext authenticationContext,
                         IssueTypeSchemeManager issueTypeSchemeManager, OptionSetManager optionSetManager,
                         UserProjectHistoryManager projectHistoryManager, ApplicationProperties applicationProperties, VelocityRequestContextFactory requestContextFactory)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.optionSetManager = optionSetManager;
        this.projectHistoryManager = projectHistoryManager;
        this.applicationProperties = applicationProperties;
        this.requestContextFactory = requestContextFactory;
    }

    @Path("issuetypeschemes")
    public IssueTypeSchemeResource getIssueTypeSchemeResource()
    {
        return new IssueTypeSchemeResource(permissionManager, authenticationContext, issueTypeSchemeManager, optionSetManager, projectHistoryManager, applicationProperties, requestContextFactory);
    }
}
