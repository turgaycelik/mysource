package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.SecurityLevelJsonBean;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 5.0
 */
@Path ("securitylevel")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueSecurityLevelResource
{
    private IssueSecurityLevelManager issueSecurityLevelManager;
    private JiraBaseUrls jiraBaseUrls;
    private I18nHelper i18n;

    private IssueSecurityLevelResource()
    {
        // this constructor used by tooling
    }

    public IssueSecurityLevelResource(IssueSecurityLevelManager issueSecurityLevelManager, JiraBaseUrls jiraBaseUrls, I18nHelper i18n)
    {
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.i18n = i18n;
    }

    /**
     * Returns a full representation of the security level that has the given id.
     *
     * @param issueSecurityLevelId a String containing an issue security level id
     * @return a full representation of the issue security level with the given id
     *
     * @response.representation.200.qname
     *      issuesecuritylevel
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the issue type exists and is visible by the calling user.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.SecurityLevelBeanExample#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the issue type does not exist, or is not visible to the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getIssuesecuritylevel(@PathParam ("id") final String issueSecurityLevelId)
    {
        try
        {
            IssueSecurityLevel issueSecurityLevel = issueSecurityLevelManager.getSecurityLevel(Long.parseLong(issueSecurityLevelId));
            if (issueSecurityLevel == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("createissue.error.security.level.not.found", issueSecurityLevelId)));
            }

            Response.ResponseBuilder rb = Response.ok(SecurityLevelJsonBean.shortBean(issueSecurityLevel, jiraBaseUrls));

            return rb.cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("createissue.error.security.level.not.found", issueSecurityLevelId)));
        }
    }
}
