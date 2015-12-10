package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.QueryOptimizer;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint for statistics gadgets Project/Filter Stats and 2D Stats.
 */
@Path ("/stats")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class StatsResource extends SearchQueryBackedResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestService searchRequestService;
    private final StatisticTypesResource statisticTypesResource;
    private static final String STAT_TYPE = "statType";
    private final ProjectManager projectManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private FilterStatisticsValuesGenerator generator;
    private final FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer;

    public StatsResource(
            final ChartUtils chartUtils,
            final JiraAuthenticationContext authenticationContext,
            final SearchService searchService,
            final SearchRequestService searchRequestService,
            final PermissionManager permissionManager,
            final StatisticTypesResource statisticTypesResource,
            final ProjectManager projectManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.authenticationContext = authenticationContext;
        this.searchRequestService = searchRequestService;
        this.statisticTypesResource = statisticTypesResource;
        this.projectManager = projectManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.fieldValueToDisplayTransformer = fieldValueToDisplayTransformer;
    }

    /**
     * Check that the queryString referes to a valid search criteria and that the statType refers to a known statistic
     * type.
     */
    @GET
    @Path ("validate")
    public Response validate(@QueryParam (SearchQueryBackedResource.QUERY_STRING) final String queryString, @QueryParam (STAT_TYPE) final String statType)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        Map<String, Object> params = new HashMap<String, Object>();
        getSearchRequestAndValidate(queryString, errors, params);
        validateStatType(errors, statType, STAT_TYPE);
        return createValidationResponse(errors);
    }

    private FilterStatisticsValuesGenerator getGenerator()
    {
        if (generator == null)
        {
            generator = new FilterStatisticsValuesGenerator();
        }
        return generator;
    }

    public void setGenerator(FilterStatisticsValuesGenerator generator)
    {
        this.generator = generator;
    }

    /**
     * Return a Response containing a Results bean for the given query.
     */
    @GET
    @Path ("generate")
    public Response getData(
            @QueryParam (SearchQueryBackedResource.QUERY_STRING) final String queryString,
            @QueryParam (STAT_TYPE) @DefaultValue ("assignees") final String statType,
            @QueryParam ("includeResolvedIssues") @DefaultValue ("false") final boolean includeResolvedIssues,
            @QueryParam ("sortDirection") @DefaultValue ("asc") final String sortDirection,
            @QueryParam ("sortBy") @DefaultValue ("natural") final String sortBy
    )
    {
        final Map<String, Object> params = new HashMap<String, Object>();

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        final SearchRequest originalSearchRequest = getSearchRequestAndValidate(queryString, errors, params);
        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }
        SearchRequest searchRequest = originalSearchRequest;
        if (!includeResolvedIssues)
        {
            searchRequest = new SearchRequest(JqlQueryBuilder.newBuilder(originalSearchRequest.getQuery()).where().defaultAnd().unresolved().buildQuery());
        }
        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        final StatisticsMapper mapper = getGenerator().getStatsMapper(statType);
        final StatisticAccessorBean statsBean = getStatisticsAcessorBean(searchRequest);
        try
        {
            final StatisticMapWrapper<Object, Integer> data = statsBean.getWrapper(
                    mapper,
                    StatisticAccessorBean.OrderBy.get(sortBy),
                    StatisticAccessorBean.Direction.get(sortDirection));
            final List<StatsRow> rows = new ArrayList<StatsRow>();
            int total = 0;
            for (Integer n : data.values())
            {
                total += n;
            }

            total += data.getIrrelevantCount();

            if (total > 0)
            {
                for (Map.Entry<Object, Integer> entry : data.entrySet())
                {
                    final Object key = entry.getKey();
                    // TODO fix redundancies in URLs built here. should use a builder for each url rather than concat.
                    final String searchUrlForHeader = getSearchUrlForHeaderCell(key, mapper, searchRequest);
                    final String searchUrl = makeUrlForQuery(searchUrlForHeader);
                    StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform(statType, key, searchUrl, fieldValueToDisplayTransformer);
                    final StatsRow r = new StatsRow(
                            statsMarkup,
                            entry.getValue(),
                            data.getPercentage(key),
                            searchUrl
                    );
                    rows.add(r);
                }
                if (data.getIrrelevantCount() > 0)
                {
                    // Add a row for the irrelevant data
                    final StatsRow r = new StatsRow(
                            new StatsMarkup("<span title=\"" + authenticationContext.getI18nHelper().getText("common.concepts.irrelevant.desc") + "\">" + authenticationContext.getI18nHelper().getText("common.concepts.irrelevant") + "</span>"),
                            data.getIrrelevantCount(),
                            data.getIrrelevantPercentage());
                    rows.add(r);
                }
            }
            final Query query = searchRequest.getQuery();
            QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();
            String queryUrl = urlPrefix() + searchService.getQueryString(authenticationContext.getLoggedInUser(), optimizer.optimizeQuery(query));
            //JRA-19177: If the original searchrequest was for a saved filter, and we're not modifying the query by adding an resolution = unresolved clause, then link
            //to the issuenavigator using the searchrequest id.
            if(originalSearchRequest.getId() != null && includeResolvedIssues)
            {
                queryUrl = getBaseUrl() + "/secure/IssueNavigator.jspa?mode=hide&requestId=" + originalSearchRequest.getId();
            }

            return Response.ok(new Results(rows, getQueryDescription(queryString), statisticTypesResource.getDisplayName(statType), queryUrl, total)).cacheControl(NO_CACHE).build();
        }
        catch (SearchUnavailableException e)
        {
            if (!e.isIndexingEnabled())
            {
                return createIndexingUnavailableResponse(createIndexingUnavailableMessage());
            }
            else
            {
                throw e;
            }
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected StatisticAccessorBean getStatisticsAcessorBean(SearchRequest searchRequest)
    {
        return new StatisticAccessorBean(authenticationContext.getLoggedInUser(), searchRequest);
    }

    private String getQueryDescription(final String projectOrFilterId)
    {
        // TODO pull this up into common resources (does it exist already?)
        if (projectOrFilterId.startsWith("filter-"))
        {
            final Long filterId = new Long(projectOrFilterId.substring(7));

            final SearchRequest sr = searchRequestService.getFilter(
                    new JiraServiceContextImpl(authenticationContext.getLoggedInUser(), new SimpleErrorCollection()), filterId);
            if (sr != null)
            {
                return sr.getName();
            }
            else
            {
                throw new IllegalArgumentException("Unknown filter " + filterId);
            }
        }
        else if (projectOrFilterId.startsWith("project-"))
        {
            final Long projectId = new Long(projectOrFilterId.substring(8));
            final Project project = projectManager.getProjectObj(projectId);
            if (project != null)
            {
                return project.getName();
            }
            else
            {
                throw new IllegalArgumentException("Unknown project " + projectId);
            }
        }
        else
        {
            return ""; // return empty description if we cannot find one (eg. when jql string is used rather than project or filter)
        }
    }

    private String getSearchUrlForHeaderCell(final Object axisObject, final StatisticsMapper axisMapper, final SearchRequest searchRequest)
    {
        return getHeadingUrlBuilder().getSearchUrlForHeaderCell(axisObject, axisMapper, searchRequest);
    }

    protected StatsSearchUrlBuilder getHeadingUrlBuilder()
    {
        return new DefaultStatsSearchUrlBuilder(searchService, authenticationContext);
    }

    private String makeUrlForQuery(final String s)
    {
        return urlPrefix() + s;
    }

    private String urlPrefix()
    {
        return getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true&mode=hide";
    }

    private String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }

    private String validateStatType(final Collection<ValidationError> errors, final String statType, String statTypeName)
    {
        final String displayName = statisticTypesResource.getDisplayName(statType);

        if (StringUtils.isBlank(displayName))
        {
            errors.add(new ValidationError(statTypeName, "gadget.common.invalid.stat.type", statType));
        }

        return displayName;
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class StatsRow
    {
        @XmlElement
        String html;
        @XmlElement
        int count;
        @XmlElement
        int percentage;
        @XmlElement
        List<String> classes;
        @XmlElement
        String url;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private StatsRow()
        {
        }

        public StatsRow(String html, int count, int percentage, List<String> classes)
        {
            this(html, count, percentage, classes, null);
        }

        public StatsRow(StatsMarkup markup, int count, int percentage)
        {
            this(markup, count, percentage, null);
        }

        public StatsRow(StatsMarkup markup, int count, int percentage, String url)
        {
            this(markup.getHtml(), count, percentage, markup.getClasses(), url);
        }

        public StatsRow(String html, int count, int percentage, List<String> classes, String url)
        {
            this.classes = classes;
            this.percentage = percentage;
            this.count = count;
            this.html = html;
            this.url = url;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            StatsRow statsRow = (StatsRow) o;

            if (count != statsRow.count) { return false; }
            if (percentage != statsRow.percentage) { return false; }
            if (classes != null ? !classes.equals(statsRow.classes) : statsRow.classes != null) { return false; }
            if (html != null ? !html.equals(statsRow.html) : statsRow.html != null) { return false; }
            if (url != null ? !url.equals(statsRow.url) : statsRow.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = html != null ? html.hashCode() : 0;
            result = 31 * result + count;
            result = 31 * result + percentage;
            result = 31 * result + (classes != null ? classes.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            return result;
        }
    }

    @XmlRootElement
    public static class Results
    {
        @XmlElement
        List<StatsRow> rows;
        @XmlElement
        String filterOrProjectName;
        @XmlElement
        String statTypeDescription;
        @XmlElement
        String filterOrProjectLink;
        @XmlElement
        int totalIssueCount;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Results()
        {
        }

        public Results(List<StatsRow> rows, String filterOrProjectName, String statTypeDescription, String filterOrProjectLink, int totalIssueCount)
        {
            this.rows = rows;
            this.filterOrProjectName = filterOrProjectName;
            this.statTypeDescription = statTypeDescription;
            this.filterOrProjectLink = filterOrProjectLink;
            this.totalIssueCount = totalIssueCount;
        }
    }
    ///CLOVER:ON
}
