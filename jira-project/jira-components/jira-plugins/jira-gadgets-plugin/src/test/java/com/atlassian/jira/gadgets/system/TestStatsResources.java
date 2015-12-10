package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.query.QueryImpl;
import org.easymock.classextension.EasyMock;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class TestStatsResources extends ResourceTest
{

    SearchService mockSearchService = createMock(SearchService.class);

    JiraAuthenticationContext mockAuthCtx = createMock(JiraAuthenticationContext.class);
    TwoDimensionalStatsMap map = createMock(TwoDimensionalStatsMap.class);
    StatisticTypesResource stats = createMock(StatisticTypesResource.class);
    ChartUtils utils = createMock(ChartUtils.class);
    SearchRequestService srs = createMock(SearchRequestService.class);
    PermissionManager permissionManager = createMock(PermissionManager.class);
    ProjectManager projectManager = createMock(ProjectManager.class);
    I18nHelper helper = createMock(I18nHelper.class);
    StatisticsMapper mapper = EasyMock.createMock(StatisticsMapper.class);
    StatisticAccessorBean accessor = createMock(StatisticAccessorBean.class);
    private SearchRequest request = new SearchRequest(new QueryImpl());
    StatsSearchUrlBuilder builder = createMock(StatsSearchUrlBuilder.class);

    @SuppressWarnings("unchecked")
    FieldValueToDisplayTransformer<StatsMarkup> mockTransformer = createMock(FieldValueToDisplayTransformer.class);

    public void testWithFilterSearch()
    {
        String type = "filter";
        assertCorrectForQueryType(type);
    }

    public void testWithProjectSearch()
    {
        String type = "project";
        assertCorrectForQueryType(type);
    }

    private void assertCorrectForQueryType(String type)
    {
        User user = new MockUser("fooGuy", "Foo Guy", "foo@bar.com");

        MockSearchQueryBackedResource resource = makeResource();

        expect(helper.getText("common.concepts.irrelevant.desc")).andReturn("The field is not present on some issues");
        expect(helper.getText("common.concepts.irrelevant")).andReturn("Irrelevant");
        replay(helper);
        expect(mockAuthCtx.getLoggedInUser()).andStubReturn(user);
        expect(mockAuthCtx.getI18nHelper()).andStubReturn(helper);
        replay(mockAuthCtx);

        expect(stats.getDisplayName("project")).andReturn("Project");

        Map map = new HashMap();
        MockGenericValue key1 = new MockGenericValue("proj1");
        key1.set("name", "proj1");
        MockGenericValue key2 = new MockGenericValue("proj2");
        key2.set("name", "proj2");
        map.put(key1, 1);
        map.put(key2, 2);
        StatisticMapWrapper mapWrapper = new StatisticMapWrapper(map, 4, 1);
        try
        {
            expect(accessor.getWrapper(mapper, StatisticAccessorBean.OrderBy.NATURAL, StatisticAccessorBean.Direction.ASC)).andReturn(mapWrapper);
            replay(accessor);
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }

        expect(mockSearchService.getQueryString(user, request.getQuery())).andReturn("urlForRequest");

        replay(mockSearchService);

        resource.setSearchRequest(request);
        resource.setExpectedQueryString(type + "-111");

        FilterStatisticsValuesGenerator generator = EasyMock.createMock(FilterStatisticsValuesGenerator.class);

        EasyMock.expect(generator.getStatsMapper("project")).andReturn(mapper);

        EasyMock.replay(generator, stats, utils);

        if (type.equals("filter"))
        {
            expect(srs.getFilter(new JiraServiceContextImpl(user, new SimpleErrorCollection()), 111L)).andReturn(new SearchRequest(new QueryImpl(), new MockApplicationUser("foo"), "theFilter", "theFilterDesc"));
            replay(srs);
        }
        else
        {
            Project projMock = createMock(Project.class);
            expect(projMock.getName()).andReturn("theProject");
            expect(projectManager.getProjectObj(111L)).andReturn(projMock);
            replay(projectManager, projMock);
        }

        resource.setGenerator(generator);
        resource.setBuilder(builder);

        expect(builder.getSearchUrlForHeaderCell(key1, mapper, request)).andReturn("lolmg");
        expect(builder.getSearchUrlForHeaderCell(key2, mapper, request)).andReturn("lolmg2");
        replay(builder);

        expect(mockTransformer.transformFromProject( "project", key1, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg")).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg'>proj1</a>"));
        expect(mockTransformer.transformFromProject("project", key2, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg2")).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg2'>proj2</a>"));
        replay(mockTransformer);

        Response response = resource.getData(type + "-111", "project", true, "ascending", "natural");
        StatsResource.Results results = (StatsResource.Results) response.getEntity();

        if (type.equals("filter"))
        {
            assertEquals("theFilter", results.filterOrProjectName);
        }
        else
        {
            assertEquals("theProject", results.filterOrProjectName);
        }
        assertEquals("Project", results.statTypeDescription);
        assertEquals("http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hideurlForRequest", results.filterOrProjectLink);

        assertThat(results.rows, hasItems(
                new StatsResource.StatsRow("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg'>proj1</a>", 1, 25, null, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg"),
                new StatsResource.StatsRow("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg2'>proj2</a>", 2, 50, null, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?reset=true&mode=hidelolmg2"),
                new StatsResource.StatsRow("<span title=\"The field is not present on some issues\">Irrelevant</span>", 1, 25, null, null)
        ));
    }

    private MockSearchQueryBackedResource makeResource()
    {
        VelocityRequestContextFactory velocity = EasyMock.createMock(VelocityRequestContextFactory.class);
        VelocityRequestContext velocityRequestContext = EasyMock.createMock(VelocityRequestContext.class);
        expect(velocity.getJiraVelocityRequestContext()).andStubReturn(velocityRequestContext);
        expect(velocityRequestContext.getCanonicalBaseUrl()).andStubReturn("http://localhost:8090/JIRA");
        replay(velocity, velocityRequestContext);

        return new MockSearchQueryBackedResource(utils, mockAuthCtx, mockSearchService, srs, permissionManager, stats, projectManager, velocity, mockTransformer);
    }

    public void testFailingValidation()
    {
        MockSearchQueryBackedResource resource = makeResource();
        expect(stats.getDisplayName("foo")).andStubReturn("foo");
        replay(stats);
        List<ValidationError> errors = Arrays.asList();
        resource.addErrorsToReturn(new ValidationError("filterId", "invalid filter"));
        resource.setExpectedQueryString("foo");

        Response stats1 = resource.validate("foo", "foo");
        assertEquals(400, stats1.getStatus());
    }

    public void testPassingValidation()
    {
        MockSearchQueryBackedResource resource = makeResource();
        expect(stats.getDisplayName("foo")).andStubReturn("foo");
        replay(stats);
        List<ValidationError> errors = Arrays.asList();
        resource.setExpectedQueryString("filter-foo");

        Response actualResponse = resource.validate("filter-foo", "foo");
        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualResponse);
    }

    class MockSearchQueryBackedResource extends StatsResource
    {
        private String expectedQueryString;
        private Collection<ValidationError> errorsToReturn = new ArrayList<ValidationError>();
        private SearchRequest searchRequest;
        private StatsSearchUrlBuilder builder;

        public MockSearchQueryBackedResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext,
                                             final SearchService searchService, final SearchRequestService searchRequestService,
                                             final PermissionManager permissionManager, final StatisticTypesResource statisticTypesResource,
                                             final ProjectManager projectManager, final VelocityRequestContextFactory velocityRequestContextFactory,
                                             final FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer)
        {
            super(chartUtils, authenticationContext, searchService, searchRequestService, permissionManager, statisticTypesResource, projectManager, velocityRequestContextFactory, fieldValueToDisplayTransformer);
        }

        protected StatsSearchUrlBuilder getHeadingUrlBuilder()
        {
            return builder;
        }

        protected StatisticAccessorBean getStatisticsAcessorBean(SearchRequest searchRequest)
        {
            return accessor;
        }

        public void setBuilder(StatsSearchUrlBuilder builder)
        {
            this.builder = builder;
        }

        public void setSearchRequest(SearchRequest searchRequest)
        {
            this.searchRequest = searchRequest;
        }

        public void setExpectedQueryString(String anExpectedQueryString)
        {
            this.expectedQueryString = anExpectedQueryString;
        }

        public void addErrorsToReturn(ValidationError... errorsToAdd)
        {
            if (errorsToAdd != null)
            {
                errorsToReturn.addAll(asList(errorsToAdd));
            }
        }

        protected StatsSearchUrlBuilder getStatsSearchUrlBuilder()
        {
            return builder;
        }

        @Override
        protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
        {
            assertEquals(expectedQueryString, queryString);
            assertNotNull(errors);
            assertNotNull(params);
            errors.addAll(errorsToReturn);
            return searchRequest;
        }
    }
}
