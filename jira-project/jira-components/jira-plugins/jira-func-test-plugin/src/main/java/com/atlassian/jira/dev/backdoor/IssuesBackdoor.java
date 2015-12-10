package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter;

@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("/issues")
public class IssuesBackdoor
{

    private final SearchService searchService;
    private final UserManager userManager;
    private final IssueManager issueManager;

    public IssuesBackdoor(SearchService searchService, UserManager userManager, IssueManager issueManager)
    {
        this.searchService = searchService;
        this.userManager = userManager;
        this.issueManager = issueManager;
    }

    @GET
    @Path ("issueKeyForSummary")
    @Produces ({ MediaType.TEXT_PLAIN })
    public Response getIssueKeyForSummary(@QueryParam ("summary") String summary)
    {
        try
        {
            List<Issue> issues = searchService.search(userManager.getUser("admin"),
                    JqlQueryBuilder.newBuilder().where().summary(summary).buildQuery(), getUnlimitedFilter()).getIssues();
            return Response.ok(issues.get(0).getKey()).build();
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }

    }

    @GET
    @Path ("issueIdByKey")
    @Produces ({ MediaType.TEXT_PLAIN })
    public Response getIssueIdByKey(@QueryParam ("key") String key)
    {
        final Issue issue = issueManager.getIssueByCurrentKey(key);
        if (issue != null)
        {
            return Response.ok(issue.getId().toString()).build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path ("touch")
    public void touch(@QueryParam ("key") String key) throws GenericEntityException
    {
        final GenericValue issue = issueManager.getIssue(key);
        issue.set("updated", new Timestamp(System.currentTimeMillis()));
        issue.store();
    }
}
