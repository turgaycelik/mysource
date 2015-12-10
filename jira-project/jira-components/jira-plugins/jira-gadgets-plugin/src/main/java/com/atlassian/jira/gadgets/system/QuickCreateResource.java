package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.rest.v1.model.errors.ErrorCollection.Builder;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to get ISsueType/project scheme info and to create an issue.
 *
 * @since v4.0
 */
@Path ("/quickcreate")
@Produces ({ MediaType.APPLICATION_JSON })
public class QuickCreateResource
{
    private final ConstantsManager constantsManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueService issueService;

    public QuickCreateResource(ConstantsManager constantsManager, PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext, IssueService issueService)
    {
        this.constantsManager = constantsManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.issueService = issueService;
    }

    /**
     * This method will attempt to create an issue with the issue type, project and summary only.  Any errors will
     * passed back.
     *
     * @param createIssueBean The values being passed in.
     * @return Simple issue information {@link com.atlassian.jira.gadgets.system.QuickCreateResource.IssueKeyBean} or a
     *         list erros for validation errors.
     */
    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Path ("/createIssue")
    public Response createIssue(final CreateIssueBean createIssueBean)
    {
        final User user = authenticationContext.getLoggedInUser();
        final String projectId = createIssueBean.getPid();
        final String issueTypeId = createIssueBean.getIssuetype();

        final IssueInputParameters issueInputParameters = getIssueInputParameters(projectId, issueTypeId, createIssueBean.getSummary(), user);
        try
        {
            // Just pass in a nothing field values holder since we do not want to show any error values to the user.
            IssueService.CreateValidationResult validationResult = issueService.validateCreate(user, issueInputParameters);

            if (!validationResult.isValid())
            {
                return Response.status(400).entity(Builder.newBuilder().addErrorCollection(validationResult.getErrorCollection()).build()).cacheControl(NO_CACHE).build();
            }

            final IssueService.IssueResult issueResult = issueService.create(user, validationResult);
            if (!issueResult.isValid())
            {
                return Response.status(400).cacheControl(NO_CACHE).build();
            }
            final Issue newIssue = issueResult.getIssue();

            final IssueKeyBean issueKeyBean = new IssueKeyBean(newIssue.getKey(), permissionManager.hasPermission(Permissions.BROWSE, newIssue, user));

            return Response.ok(issueKeyBean).cacheControl(NO_CACHE).build();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private IssueInputParameters getIssueInputParameters(String projectId, String issueTypeId, String summary, User reporter)
    {
        final IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(new Long(projectId));
        issueInputParameters.setIssueTypeId(issueTypeId);
        issueInputParameters.setSummary(summary);
        issueInputParameters.setReporterId(reporter.getName());
        issueInputParameters.setAssigneeId("-1");
        issueInputParameters.setPriorityId(constantsManager.getDefaultPriorityObject().getId());
        return issueInputParameters;
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class IssueKeyBean
    {
        @XmlElement
        private String key;
        @XmlElement
        private boolean canBrowse;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private IssueKeyBean()
        {}

        IssueKeyBean(String key, boolean canBrowse)
        {
            this.key = key;
            this.canBrowse = canBrowse;
        }
    }

    @XmlRootElement
    public static class CreateIssueBean
    {
        @XmlElement
        private String pid;
        @XmlElement
        private String issuetype;
        @XmlElement
        private String summary;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private CreateIssueBean()
        {}

        CreateIssueBean(String pid, String issuetype, String summary)
        {
            this.pid = pid;
            this.issuetype = issuetype;
            this.summary = summary;
        }

        public String getPid()
        {
            return pid;
        }

        public String getIssuetype()
        {
            return issuetype;
        }

        public String getSummary()
        {
            return summary;
        }
    }

    ///CLOVER:ON
}
