package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Parent Resource for all pickers in the gadget plugin.
 *
 * @since v4.0
 */
@Path ("pickers")
@Produces ({ MediaType.APPLICATION_JSON })
@AnonymousAllowed
public class PickerResource
{
    //JRA-19918 searches for 4.0 don't work well
    private static final String DELIMS = "-_/\\,+=&^%$#*@!~`'\":;<> ";

    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestManager searchRequestManager;

    public PickerResource(PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, SearchRequestManager searchRequestManager)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.searchRequestManager = searchRequestManager;
    }

    @Path ("projectsAndFilters")
    @GET
    public Response searchForProjectsAndCategories(@QueryParam ("query") @DefaultValue ("") String query)
    {

        if (query.startsWith("*") || query.startsWith("?"))
        {
            final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder(new ValidationError("quickfind", "gadget.common.invalid.projectOrFilterId.query.prefix")).build();

            return Response.status(400).entity(errorCollection).cacheControl(CacheControl.NO_CACHE).build();
        }
        return Response.ok(getProjectsAndCategories(query)).cacheControl(CacheControl.NO_CACHE).build();
    }

    public ProjectsAndFiltersWrapper getProjectsAndCategories(String query)
    {
        final ProjectPickerWrapper projects = getProjects(query);

        final FilterPickerWrapper filters = getFilters(query);

        return new ProjectsAndFiltersWrapper(projects.projects, filters.filters);
    }

    @Path ("projects")
    @GET
    public Response searchForProjects(@QueryParam ("query") @DefaultValue ("") String query)
    {
        if (query.startsWith("*") || query.startsWith("?"))
        {
            final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder(new ValidationError("quickfind", "gadget.common.invalid.projectOrFilterId.query.prefix")).build();

            return Response.status(400).entity(errorCollection).cacheControl(CacheControl.NO_CACHE).build();
        }
        return Response.ok(getProjects(query)).cacheControl(CacheControl.NO_CACHE).build();
    }

    public ProjectPickerWrapper getProjects(String query)
    {
        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, authenticationContext.getLoggedInUser());
        final ProjectPickerWrapper result = new ProjectPickerWrapper();

        for (Project project : projects)
        {
            if (projectMatches(project, query))
            {
                result.addProject(new ProjectPickerBean(project.getId().toString(), formatProject(project, query), project.getName(), project.getKey()));
            }
        }
        return result;
    }

    @Path ("filters")
    @GET
    public Response searchForFilters(@QueryParam ("query") @DefaultValue ("") String query)
    {
        if (query.startsWith("*") || query.startsWith("?"))
        {
            final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder(new ValidationError("quickfind", "gadget.common.invalid.projectOrFilterId.query.prefix")).build();

            return Response.status(400).entity(errorCollection).cacheControl(CacheControl.NO_CACHE).build();
        }

        return Response.ok(getFilters(query)).cacheControl(CacheControl.NO_CACHE).build();
    }

    public FilterPickerWrapper getFilters(String query)
    {
        final StringBuilder newQueryBuilder = new StringBuilder();
        final StringTokenizer tokenizer = new StringTokenizer(query, DELIMS);
        while (tokenizer.hasMoreElements())
        {
            final String token = tokenizer.nextToken();
            if (StringUtils.isNotBlank(token))
            {
                 //JRA-19918 Too many results being returned
                newQueryBuilder.append("+").append(token).append(" ");
            }
        }

        final String newQuery = newQueryBuilder.toString().trim();

        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder();
        builder.setName(StringUtils.isBlank(query) ? null : newQuery);
        builder.setDescription(StringUtils.isBlank(query) ? null : newQuery);

        // we are using OR searching at the moment. This may change in the future
        // As we are using wildcards, set mode to wildcard
        builder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.WILDCARD);
        // what are we sorting on
        builder.setSortColumn(SharedEntityColumn.NAME, true);

        builder.setEntitySearchContext(SharedEntitySearchContext.USE);

        final SharedEntitySearchResult<SearchRequest> searchResults = searchRequestManager.search(builder.toSearchParameters(), authenticationContext.getUser(), 0, 10);

        final List<SearchRequest> list = searchResults.getResults();

        final FilterPickerWrapper filterPickerWrapper = new FilterPickerWrapper();

        for (SearchRequest searchRequest : list)
        {
            filterPickerWrapper.addProject(new FilterPickerBean(searchRequest.getId().toString(), searchRequest.getName(),
                    formatField(searchRequest.getName(), query), formatField(searchRequest.getDescription(), query)));
        }

        return filterPickerWrapper;
    }

    private boolean projectMatches(Project project, String query)
    {
        query = query.toLowerCase().trim();
        final String projectName = project.getName().toLowerCase();
        final String projectKey = project.getKey().toLowerCase();

        if (projectName.startsWith(query) || projectKey.startsWith(query))
        {
            return true;
        }

        final StringTokenizer tokenizer = new StringTokenizer(project.getName().toLowerCase(), DELIMS);
        while (tokenizer.hasMoreElements())
        {
            final String projPart = tokenizer.nextToken();
            if (projPart.startsWith(query))
            {
                return true;
            }
        }
        return false;
    }

    private String formatProject(Project project, String query)
    {
        final String projectName = formatField(project.getName(), query);
        final String projectKey = formatField(project.getKey(), query);

        final StringBuilder sb = new StringBuilder();
        sb.append(projectName);
        sb.append("&nbsp;(");
        sb.append(projectKey);
        sb.append(")");
        return sb.toString();
    }

    private String formatField(String field, String query)
    {

        final DelimeterInserter delimeterInserter = new DelimeterInserter("<strong>", "</strong>");
        delimeterInserter.setConsideredWhitespace(DELIMS);

        final StringTokenizer tokenizer = new StringTokenizer(query, DELIMS);
        final List<String> terms = new ArrayList<String>();

        while (tokenizer.hasMoreElements())
        {
            final String projPart = tokenizer.nextToken();
            if (StringUtils.isNotBlank(projPart))
            {
                terms.add(projPart);
            }
        }

        return delimeterInserter.insert(TextUtils.htmlEncode(field), terms.toArray(new String[terms.size()]));
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class ProjectsAndFiltersWrapper
    {
        @XmlElement
        private List<ProjectPickerBean> projects;
        @XmlElement
        private List<FilterPickerBean> filters;

        public ProjectsAndFiltersWrapper()
        {
        }

        public ProjectsAndFiltersWrapper(List<ProjectPickerBean> projects, List<FilterPickerBean> filters)
        {
            this.projects = projects;
            this.filters = filters;
        }
    }

    @XmlRootElement
    public static class ProjectPickerWrapper
    {
        @XmlElement
        private List<ProjectPickerBean> projects;

        private ProjectPickerWrapper()
        {
        }

        private ProjectPickerWrapper(List<ProjectPickerBean> projects)
        {
            this.projects = projects;
        }

        public void addProject(ProjectPickerBean project)
        {
            if (projects == null)
            {
                projects = new ArrayList<ProjectPickerBean>();
            }
            projects.add(project);
        }
    }

    @XmlRootElement
    public static class ProjectPickerBean
    {
        @XmlElement
        private String html;
        @XmlElement
        private String name;
        @XmlElement
        private String key;
        @XmlElement
        private String id;

        private ProjectPickerBean()
        {
        }

        private ProjectPickerBean(String id, String html, String name, String key)
        {
            this.html = html;
            this.name = name;
            this.key = key;
            this.id = id;
        }
    }

    @XmlRootElement
    public static class FilterPickerWrapper
    {
        @XmlElement
        private List<FilterPickerBean> filters;

        private FilterPickerWrapper()
        {
        }

        private FilterPickerWrapper(List<FilterPickerBean> filters)
        {
            this.filters = filters;
        }

        public void addProject(FilterPickerBean filter)
        {
            if (filters == null)
            {
                filters = new ArrayList<FilterPickerBean>();
            }
            filters.add(filter);
        }
    }

    @XmlRootElement
    public static class FilterPickerBean
    {
        @XmlElement
        private String id;
        @XmlElement
        private String name;
        @XmlElement
        private String nameHtml;
        @XmlElement
        private String descHtml;

        private FilterPickerBean()
        {
        }

        public FilterPickerBean(String id, String name, String nameHtml, String descHtml)
        {
            this.id = id;
            this.name = name;
            this.nameHtml = nameHtml;
            this.descHtml = descHtml;
        }
    }
    ///CLOVER:ON
}
