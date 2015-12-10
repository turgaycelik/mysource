package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.rest.v2.issue.IncludedFields;
import com.atlassian.jira.rest.v2.issue.IssueBean;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.action.issue.IssueSearchLimits;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.SelfExpandingExpander;
import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.singletonList;

/**
 * Resource for searches.
 *
 * @since v4.3
 */
@Path ("search")
@AnonymousAllowed
@Consumes ( { MediaType.APPLICATION_JSON })
@Produces ( { MediaType.APPLICATION_JSON })
public class SearchResource
{
    /**
     * The default number of issues that will be returned, if another number is not specified.
     */
    static final int DEFAULT_ISSUES_RETURNED = 50;

    /**
     * The service used for performing JQL searches.
     */
    private final SearchService searchService;

    /**
     * A JiraAuthenticationContext.
     */
    private final JiraAuthenticationContext jiraAuthenticationContext;

    /**
     * A IssueSearchLimits.
     */
    private final IssueSearchLimits searchLimits;

    /**
     * Factory of bean builders.
     */
    private final BeanBuilderFactory beanBuilderFactory;

    public SearchResource(SearchService searchService, JiraAuthenticationContext jiraAuthenticationContext,
            IssueSearchLimits searchLimits, BeanBuilderFactory beanBuilderFactory)
    {
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchLimits = searchLimits;
        this.beanBuilderFactory = beanBuilderFactory;
    }

    /**
     * Searches for issues using JQL.
     *
     * <p><b>Sorting</b>
     *  the <code>jql</code> parameter is a full <a href="http://confluence.atlassian.com/display/JIRA/Advanced+Searching">JQL</a>
     *  expression, and includes an <code>ORDER BY</code> clause.
     * </p>
     *
     * <p>
     *  The <code>fields</code> param (which can be specified multiple times) gives a comma-separated list of fields
     *  to include in the response. This can be used to retrieve a subset of fields.
     *  A particular field can be excluded by prefixing it with a minus.
     *  <p>
     *   By default, only navigable (<code>*navigable</code>) fields are returned in this search resource. Note: the default is different
     *   in the get-issue resource -- the default there all fields (<code>*all</code>).
     *  <ul>
     *      <li><code>*all</code> - include all fields</li>
     *      <li><code>*navigable</code> - include just navigable fields</li>
     *      <li><code>summary,comment</code> - include just the summary and comments</li>
     *      <li><code>-description</code> - include navigable fields except the description (the default is <code>*navigable</code> for search)</li>
     *      <li><code>*all,-comment</code> - include everything except comments</li>
     *  </ul>
     *
     * </p>
     * <p><b>GET vs POST:</b>
     *  If the JQL query is too large to be encoded as a query param you should instead
     *  POST to this resource.
     * </p>
     *
     * <p><b>Expanding Issues in the Search Result:</b>
     *  It is possible to expand the issues returned by directly specifying the expansion on the expand parameter passed
     *  in to this resources.
     * </p>
     * <p>
     *  For instance, to expand the &quot;changelog&quot; for all the issues on the search result, it is neccesary to
     *  specify &quot;changelog&quot; as one of the values to expand.
     * </p>
     * @param jql a JQL query string
     * @param startAt the index of the first issue to return (0-based)
     * @param maxResults the maximum number of issues to return (defaults to 50). The maximum allowable value is
     * dictated by the JIRA property 'jira.search.views.default.max'. If you specify a value that is higher than this
     * number, your search results will be truncated.
     * @param validateQuery whether to validate the JQL query
     * @param fields the list of fields to return for each issue. By default, all navigable fields are returned.
     * @param expand A comma-separated list of the parameters to expand.
     * @return a SearchResultsBean
     * @throws com.atlassian.jira.issue.search.SearchException if there is a problem performing the search
     *
     * @response.representation.200.qname
     *      searchResults
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the search results.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.SearchResultsBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem with the JQL query.
     */
    @GET
    public SearchResultsBean search(
            @QueryParam ("jql") String jql,
            @QueryParam ("startAt") Integer startAt,
            @QueryParam ("maxResults") Integer maxResults,
            @QueryParam ("validateQuery") @DefaultValue("true") Boolean validateQuery,
            @QueryParam ("fields") List<StringList> fields,
            @QueryParam ("expand") StringList expand)
            throws SearchException
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        SearchService.ParseResult parseResult = searchService.parseQuery(user, jql == null ? "" : jql);
        if (!parseResult.isValid())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(parseResult.getErrors().getErrorMessages()));
        }

        final MessageSet result = searchService.validateQuery(user, parseResult.getQuery());
        if ((validateQuery == null || validateQuery) && result.hasAnyErrors())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(result.getErrorMessages()).addErrorMessages(result.getWarningMessages()));
        }

        PagerFilter filter = createFilter(startAt, maxResults);
        SearchResults searchResult = searchService.search(user, parseResult.getQuery(), filter);

        IncludedFields includeFields = IncludedFields.includeNavigableByDefault(fields);

        return asResultsBean(searchResult, filter, includeFields, expand, result.getWarningMessages());
    }

    /**
     * Performs a search using JQL.
     *
     * @param searchRequest a JSON object containing the search request
     * @return a SearchResultsBean
     * @throws com.atlassian.jira.issue.search.SearchException if there is a problem performing the search
     *
     * @request.representation.qname
     *      searchRequest
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.search.SearchRequestBean#DOC_EXAMPLE}
     *
     * @response.representation.200.qname
     *      searchResults
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the search results.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.SearchResultsBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem with the JQL query.
     */
    @POST
    public SearchResultsBean searchUsingSearchRequest(SearchRequestBean searchRequest) throws SearchException
    {
        List<StringList> fields = searchRequest.fields != null ? singletonList(new StringList(searchRequest.fields)) : null;
        StringList expand = searchRequest.expand != null ? StringList.fromList(searchRequest.expand) : null;

        return search(searchRequest.jql, searchRequest.startAt, searchRequest.maxResults, searchRequest.validateQuery, fields, expand);
    }

    /**
     * Creates a SearchResultsBean suitable for returning to the client.
     *
     * @param results a SearchResults
     * @param filter a PagerFilter that was used for the search
     * @param fields a List&lt;StringList,&gt;
     * @param expand the parameters to expand
     * @param warningMessages
     * @return a SearchResultsBean
     */
    protected SearchResultsBean asResultsBean(SearchResults results, PagerFilter filter, IncludedFields fields, StringList expand, Set<String> warningMessages)
    {
        List<IssueBean> issues = newArrayList(transform(results.getIssues(), new IssueToIssueBean(fields, expand)));
        return new SearchResultsBean(
                results.getStart(),
                filter.getMax(),
                results.getTotal(),
                issues,
                warningMessages
        );
    }

    /**
     * Creates a new PagerFilter for the given search request. If the maxResults specified in the search request is
     * greater than the value returned by {@link com.atlassian.jira.web.action.issue.IssueSearchLimits#getMaxResults()},
     * the value returned by this method will be used.
     *
     * @param startAt an Integer containing the start index
     * @param maxResults an Integer containing the max results
     * @return a PagerFilter
     */
    protected PagerFilter createFilter(Integer startAt, Integer maxResults)
    {
        int searchMaxLimit = searchLimits.getMaxResults();
        return new PagerFilter(startAt != null ? max(0, startAt) : 0, maxResults != null ? (maxResults < 0 ? searchMaxLimit : min(searchMaxLimit, maxResults)) : DEFAULT_ISSUES_RETURNED);
    }

    /**
     * Function that converts an Issue to an IssueBean (short version).
     */
    private class IssueToIssueBean implements Function<Issue, IssueBean>
    {
        private final IncludedFields fields;
        private final ExpandParameter expand;
        private final String expandAsString;
        private final EntityExpanderResolver expandResolver = new SelfExpandingExpander.Resolver();
        private final EntityCrawler entityCrawler = new EntityCrawler();

        public IssueToIssueBean(IncludedFields fields, StringList expand)
        {
            this.fields = fields;
            this.expand = new DefaultExpandParameter(expand != null ? expand.asList() : Collections.<String>emptyList());
            this.expandAsString = expand != null ? expand.toQueryParam() : null;
        }

        public IssueBean apply(@Nullable Issue issue)
        {
            IssueBean bean = beanBuilderFactory.newIssueBeanBuilder(issue, fields).expand(expandAsString).build();

            // explicity crawl all over this motha, since atlassian-rest didn't take care of business.
            entityCrawler.crawl(bean, expand, expandResolver);
            return bean;
        }
    }
}
