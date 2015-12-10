package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.Query;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * An abstract class used for common search request handling for search request backed resources.
 *
 * @since v4.0
 */
public abstract class SearchQueryBackedResource extends AbstractResource
{
    protected static final String QUERY_STRING = "projectOrFilterId";
    static final String PROJECT = "project";
    private static final String SEARCH_REQUEST = "searchRequest";
    protected final ChartUtils chartUtils;
    protected final JiraAuthenticationContext authenticationContext;
    protected final PermissionManager permissionManager;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    protected final SearchService searchService;
    private static final String FILTER_PREFIX = "filter-";
    private static final String PROJECT_PREFIX = "project-";
    private static final String JQL_PREFIX = "jql-";

    public SearchQueryBackedResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext,
            final SearchService searchService, final PermissionManager permissionManager,
            final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.chartUtils = chartUtils;
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.permissionManager = permissionManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * Method used for retrieving/building a SearchRequest and validating params.
     *
     * @param queryString a String containing a search criteria.  Starts with "filter-" for a search request, "project-"
     *                    for a project or "jql-" for a jql equest.
     * @param errors      a collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError} object
     *                    containing all validation message keys.
     * @param params      a map to populate with with appropriate entities.
     * @return a {@link com.atlassian.jira.issue.search.SearchRequest} that will restrict the search to the given
     *         criteria
     */
    protected SearchRequest getSearchRequestAndValidate(String queryString, Collection<ValidationError> errors, Map<String, Object> params)
    {
        SearchRequest searchRequest;
        if (StringUtils.isNotEmpty(queryString))
        {
            params.put(QUERY_STRING, queryString);
            searchRequest = chartUtils.retrieveOrMakeSearchRequest(queryString, params);
            validateParams(errors, params);
            if (!errors.isEmpty())
            {
                searchRequest = null;
            }
        }
        else
        {
            errors.add(new ValidationError(QUERY_STRING, "gadget.common.required.query"));
            searchRequest = null;
        }
        return searchRequest;
    }

    private void validateParams(Collection<ValidationError> errors, Map<String, Object> params)
    {
        final String queryString = (String) params.get(QUERY_STRING);
        if (queryString.startsWith(FILTER_PREFIX))
        {
            if (params.get(SEARCH_REQUEST) == null)
            {
                errors.add(new ValidationError(QUERY_STRING, "gadget.common.invalid.filter"));
            }
        }
        else if (queryString.startsWith(PROJECT_PREFIX))
        {
            if (params.get(PROJECT) == null)
            {
                errors.add(new ValidationError(QUERY_STRING, "gadget.common.invalid.project"));
            }
            else
            {
                if (!permissionManager.hasPermission(Permissions.BROWSE, (Project) params.get(PROJECT), authenticationContext.getUser()))
                {
                    errors.add(new ValidationError(QUERY_STRING, "gadget.common.invalid.project"));
                }
            }
        }
        else if (queryString.startsWith(JQL_PREFIX))
        {
            if (params.get(SEARCH_REQUEST) == null)
            {
                errors.add(new ValidationError(QUERY_STRING, "gadget.common.invalid.jql"));
            }
        }
        else
        {
            errors.add(new ValidationError(QUERY_STRING, "gadget.common.invalid.projectOrFilterId"));
        }
    }

    /**
     * Get the name to display for the given query.
     *
     * @param params The params created during chart generation process.
     * @return For a project, get the project name.  For a saved filter, get the name.  For a unsaved search, return the
     *         anonymous key.
     */
    protected String getFilterTitle(final Map<String, Object> params)
    {
        if (params.containsKey(PROJECT))
        {
            return ((Project) params.get(PROJECT)).getName();
        }
        else if (params.containsKey(SEARCH_REQUEST))
        {
            return ((SearchRequest) params.get(SEARCH_REQUEST)).getName();
        }
        else
        {
            return "gadget.common.anonymous.filter";
        }
    }

    /**
     * Get the url to send people to for this search.
     *
     * @param params The params created during chart generation process.
     * @return For a project or filter, send them to the issue navigator. For an unsaved search, return empty string.
     */
    protected String getFilterUrl(final Map<String, Object> params)
    {
        if (params.containsKey(PROJECT))
        {
            final Project project = (Project) params.get(PROJECT);
            final Query query = JqlQueryBuilder.newBuilder().where().project().eq(project.getKey()).buildQuery();
            return "/secure/IssueNavigator.jspa?reset=true&mode=hide" + searchService.getQueryString(ApplicationUsers.toDirectoryUser(authenticationContext.getUser()), query);
        }
        else if (params.containsKey(SEARCH_REQUEST))
        {
            final SearchRequest request = (SearchRequest) params.get(SEARCH_REQUEST);
            if (request != null && request.isLoaded())
            {
                return "/secure/IssueNavigator.jspa?mode=hide&requestId=" + request.getId();
            }
            else
            {
                return "/secure/IssueNavigator.jspa?reset=true&mode=hide" + searchService.getQueryString(ApplicationUsers.toDirectoryUser(authenticationContext.getUser()), (request == null) ? new QueryImpl() : request.getQuery());
            }
        }
        else
        {
            return "";
        }
    }

    String createIndexingUnavailableMessage()
    {
        final String msg1 = authenticationContext.getI18nHelper().getText("gadget.common.indexing");
        String msg2;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser()))
        {
            String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.configure",
                    "<a href=\"" + baseUrl + "/secure/admin/jira/IndexAdmin.jspa\">", "</a>");
        }
        else
        {
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.admin");
        }
        return msg1 + " " + msg2;
    }
}
