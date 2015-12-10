package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.customfields.impl.ImportIdLinkCFType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for looking up issues based on their bugzilla ID.
 *
 * @since v4.0
 */
@Path ("/bugzilla")
@AnonymousAllowed
public class BugzillaIdSearchResource extends AbstractResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;
    private VelocityRequestContextFactory velocityRequestContextFactory;

    public BugzillaIdSearchResource(final SearchService searchService, final JiraAuthenticationContext authenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.searchService = searchService;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @GET
    @Path ("/search")
    public Response searchBugzillaIssue(@QueryParam ("bugId") String idParam)
            throws SearchException
    {
        String url = "";
        if (idParam != null)
        {
            String issueKey = findMovedIssueJql(idParam, authenticationContext.getLoggedInUser());
            if (issueKey != null)
            {
                url = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl() + "/browse/" + issueKey;
            }
        }
        return Response.ok(url).cacheControl(NO_CACHE).build();
    }

    @GET
    @Path ("/validate")
    public Response validate(@QueryParam ("bugzillaUrl") String baseUrl)
    {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        if (StringUtils.isBlank(baseUrl) || baseUrl.startsWith("javascript:"))
        {
            errors.add(new ValidationError("bugzillaUrl", "gadget.bugzilla.invalid.url"));
        }
        if (errors.isEmpty())
        {
            try
            {
                new URL(baseUrl);
            }
            catch (MalformedURLException e)
            {
                errors.add(new ValidationError("bugzillaUrl", "gadget.bugzilla.invalid.url"));
            }
        }
        return createValidationResponse(errors);
    }

    String findMovedIssueJql(final String bugzillaKey, final User user) throws SearchException
    {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        queryBuilder.where().addClause(new TerminalClauseImpl(ImportIdLinkCFType.BUGZILLA_ID_CF_NAME, Operator.EQUALS, bugzillaKey));
        Query query = queryBuilder.buildQuery();

        final SearchResults results = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
        if (results.getTotal() < 1)
        {
            return null;
        }
        return results.getIssues().get(0).getKey();
    }
}
