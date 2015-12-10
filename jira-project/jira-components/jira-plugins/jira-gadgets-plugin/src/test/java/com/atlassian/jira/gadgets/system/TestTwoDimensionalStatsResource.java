package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

public class TestTwoDimensionalStatsResource extends ResourceTest
{
    StatisticsMapper xMapper = EasyMock.createMock(StatisticsMapper.class);
    StatisticsMapper yMapper = EasyMock.createMock(StatisticsMapper.class);
    StubSearchRequest request = new StubSearchRequest();

    I18nHelper bean = createMock(I18nHelper.class);
    SearchService mockSearchService = EasyMock.createMock(SearchService.class);

    JiraAuthenticationContext mockAuthCtx = EasyMock.createMock(JiraAuthenticationContext.class);
    TwoDimensionalStatsMap map = EasyMock.createMock(TwoDimensionalStatsMap.class);
    StatisticTypesResource stats = EasyMock.createMock(StatisticTypesResource.class);
    ChartUtils utils = EasyMock.createMock(ChartUtils.class);

    @SuppressWarnings("unchecked")
    FieldValueToDisplayTransformer<StatsMarkup> mockTransformer = createMock(FieldValueToDisplayTransformer.class);

    private class StubSearchRequest extends SearchRequest
    {
        private Long id;

        public Long getId()
        {
            return id;
        }

        public void setId(final Long id)
        {
            this.id = id;
        }
    }

    public void testEmptyResult()
    {
        List<Object> list = Collections.emptyList();

        EasyMock.expect(mockSearchService.getQueryString(null, request.getQuery())).andReturn("www.lol.com");
        EasyMock.replay(mockSearchService);

        final I18nHelper mockI18n = EasyMock.createMock(I18nHelper.class);

        EasyMock.replay(mockI18n);

        EasyMock.expect(mockAuthCtx.getI18nHelper()).andReturn(mockI18n).anyTimes();
        EasyMock.replay(mockAuthCtx);

        TwoDimensionalStatsResource.TwoDimensionalProperties dimensionalProperties = buildTwoDimensionalProperties(list, list, false, "assignees", null, null, "Assignee", "Assignee");
        assertTrue(dimensionalProperties.getFilter().isEmpty());
        assertEquals(0, dimensionalProperties.getTotalRows());

    }

    public void testOneResult()
    {
        request.setId(1337L);
        User user = new MockUser("fooGuy", "Foo Guy", "foo@bar.com");

        List<User> objects = Arrays.asList(user);
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.xaxis")).andReturn("T:");
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.yaxis")).andReturn("Total Unique Issues");
        replay(i18nHelper);
        expect(mockAuthCtx.getLoggedInUser()).andStubReturn(user);
        expect(mockAuthCtx.getI18nHelper()).andStubReturn(i18nHelper);
        replay(mockAuthCtx);
        expect(mockSearchService.getQueryString(user, request.getQuery())).andStubReturn("theFilterUrl");
        replay(mockSearchService);
        expect(map.getCoordinate(user, user)).andReturn(3);

        StatsSearchUrlBuilder builder = createMock(StatsSearchUrlBuilder.class);

        expect(builder.getSearchUrlForHeaderCell(user, xMapper, request)).andStubReturn("&urlForUser");
        expect(builder.getSearchUrlForHeaderCell(user, yMapper, request)).andStubReturn("&urlForUser");
        expect(builder.getSearchUrlForCell(user, user, map, request)).andStubReturn("&urlForUser&urlForUser");

        replay(builder);

        expect(mockTransformer.transformFromAssignee("assignees", user, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>")).times(2);
        replay(mockTransformer);

        TwoDimensionalStatsResource.TwoDimensionalProperties properties = buildTwoDimensionalProperties(objects, objects, false, "assignees", "assignees", builder, "Assignee", "Assignee");
        assertEquals("http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&requestId=1337", properties.getFilter().getFilterUrl());
        assertEquals(false, properties.isShowTotals());
        List<TwoDimensionalStatsResource.Cell> firstRowCells = properties.getFirstRow().getCells();
        assertEquals(1, firstRowCells.size());
        TwoDimensionalStatsResource.Cell firstRowCell = firstRowCells.get(0);
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>", firstRowCell.getMarkup());
        assertEquals(1, properties.getRows().size());
        TwoDimensionalStatsResource.Row nextRow = properties.getRows().get(0);
        List<TwoDimensionalStatsResource.Cell> nextRowCells = nextRow.getCells();
        assertFalse(properties.getFilter().isEmpty());
        assertEquals(2, nextRowCells.size());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>", nextRowCells.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser&urlForUser'>3</a>", nextRowCells.get(1).getMarkup());
        assertEquals(1, properties.getTotalRows());
    }

    public void testOneResultWithIrrelevants()
    {
        request.setId(1337L);
        User user = new MockUser("fooGuy", "Foo Guy", "foo@bar.com");

        List<User> objects = Arrays.asList(user);
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        expect(i18nHelper.getText("common.concepts.irrelevant.desc")).andReturn("The field is not present on some issues").anyTimes();
        expect(i18nHelper.getText("common.concepts.irrelevant")).andReturn("Irrelevant").anyTimes();
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.xaxis")).andReturn("T:");
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.yaxis")).andReturn("Total Unique Issues");
        replay(i18nHelper);
        expect(mockAuthCtx.getLoggedInUser()).andStubReturn(user);
        expect(mockAuthCtx.getI18nHelper()).andStubReturn(i18nHelper);
        replay(mockAuthCtx);
        expect(mockSearchService.getQueryString(user, request.getQuery())).andStubReturn("theFilterUrl");
        replay(mockSearchService);
        expect(map.getCoordinate(user, user)).andReturn(3);

        StatsSearchUrlBuilder builder = createMock(StatsSearchUrlBuilder.class);

        expect(builder.getSearchUrlForHeaderCell(user, xMapper, request)).andStubReturn("&urlForUser");
        expect(builder.getSearchUrlForHeaderCell(user, yMapper, request)).andStubReturn("&urlForUser");
        expect(builder.getSearchUrlForCell(user, user, map, request)).andStubReturn("&urlForUser&urlForUser");

        replay(builder);

        expect(mockTransformer.transformFromAssignee("assignees", user, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>")).times(2);
        replay(mockTransformer);

        TwoDimensionalStatsResource.TwoDimensionalProperties properties = buildTwoDimensionalPropertiesWithIrrelevant(objects, objects, false, "assignees", "assignees", builder, "Assignee", "Assignee");
        assertEquals("http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&requestId=1337", properties.getFilter().getFilterUrl());
        assertEquals(false, properties.isShowTotals());
        List<TwoDimensionalStatsResource.Cell> firstRowCells = properties.getFirstRow().getCells();
        assertEquals(2, firstRowCells.size());
        TwoDimensionalStatsResource.Cell firstRowCell = firstRowCells.get(0);
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>", firstRowCell.getMarkup());
        assertEquals("<span title=\"The field is not present on some issues\">Irrelevant</span>", firstRowCells.get(1).getMarkup());
        assertEquals(2, properties.getRows().size());
        TwoDimensionalStatsResource.Row nextRow = properties.getRows().get(0);
        List<TwoDimensionalStatsResource.Cell> nextRowCells = nextRow.getCells();
        assertFalse(properties.getFilter().isEmpty());
        assertEquals(3, nextRowCells.size());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>", nextRowCells.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser&urlForUser'>3</a>", nextRowCells.get(1).getMarkup());
        assertEquals("1", nextRowCells.get(2).getMarkup());
        assertEquals(2, properties.getTotalRows());
    }

    public void testThrowingException()
    {
        MockSearchQueryBackedResource resource = makeResource();

        resource.addErrorsToReturn(new ValidationError("filterId", "invalid filter"));
        resource.setExpectedQueryString("filter-foo");

        Response stats1 = resource.getStats("filter-foo", "foo", "foo", "foo", "foo", true, "0");
        assertEquals(400, stats1.getStatus());
    }

    public void testFailingValidation()
    {
        MockSearchQueryBackedResource resource = makeResource();
        expect(stats.getDisplayName("foo")).andStubReturn("foo");
        replay(stats);
        resource.addErrorsToReturn(new ValidationError("filterId", "invalid filter"));
        resource.setExpectedQueryString("filter-23");

        Response stats1 = resource.validate("23", "foo", "foo", "10");
        assertEquals(400, stats1.getStatus());
    }

    public void testPassingValidation()
    {
        MockSearchQueryBackedResource resource = makeResource();
        expect(stats.getDisplayName("foo")).andStubReturn("foo");
        replay(stats);
        resource.setExpectedQueryString("filter-23");

        Response actualResponse = resource.validate("23", "foo", "foo", "10");
        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actualResponse);
    }

    private MockSearchQueryBackedResource makeResource()
    {
        PermissionManager mockPermissionManager = EasyMock.createMock(PermissionManager.class);
        SearchProvider mockSearchProvider = EasyMock.createMock(SearchProvider.class);

        VelocityRequestContextFactory velocity = EasyMock.createMock(VelocityRequestContextFactory.class);

        VelocityRequestContext velocityRequestContext = EasyMock.createMock(VelocityRequestContext.class);

        expect(velocity.getJiraVelocityRequestContext()).andStubReturn(velocityRequestContext);

        expect(velocityRequestContext.getCanonicalBaseUrl()).andStubReturn("http://localhost:8090/JIRA");

        replay(velocity, velocityRequestContext);

        return new MockSearchQueryBackedResource(utils, mockAuthCtx, mockSearchService, mockPermissionManager, stats, mockSearchProvider, velocity, mockTransformer);
    }

    private MockSearchQueryBackedResourceWithIrrelevantData makeResourceWithIrrelevantData()
    {
        PermissionManager mockPermissionManager = EasyMock.createMock(PermissionManager.class);
        SearchProvider mockSearchProvider = EasyMock.createMock(SearchProvider.class);

        VelocityRequestContextFactory velocity = EasyMock.createMock(VelocityRequestContextFactory.class);

        VelocityRequestContext velocityRequestContext = EasyMock.createMock(VelocityRequestContext.class);

        expect(velocity.getJiraVelocityRequestContext()).andStubReturn(velocityRequestContext);

        expect(velocityRequestContext.getCanonicalBaseUrl()).andStubReturn("http://localhost:8090/JIRA");

        replay(velocity, velocityRequestContext);

        return new MockSearchQueryBackedResourceWithIrrelevantData(utils, mockAuthCtx, mockSearchService, mockPermissionManager, stats, mockSearchProvider, velocity, mockTransformer);
    }


    public void testOneResultWithShowTotals()
    {

        request.setId(1337L);

        User user = new MockUser("fooGuy", "Foo Guy", "foo@bar.com");

        I18nHelper i18nHelper = createMock(I18nHelper.class);
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.xaxis")).andReturn("T:");
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.yaxis")).andReturn("Total Unique Issues");
        replay(i18nHelper);

        List<User> objects = Arrays.asList(user);
        expect(mockAuthCtx.getLoggedInUser()).andStubReturn(user);
        expect(mockAuthCtx.getI18nHelper()).andStubReturn(i18nHelper);
        replay(mockAuthCtx);
        expect(mockSearchService.getQueryString(user, request.getQuery())).andStubReturn("theFilterUrl");
        replay(mockSearchService);

        StatsSearchUrlBuilder builder = createMock(StatsSearchUrlBuilder.class);

        expect(builder.getSearchUrlForHeaderCell(user, xMapper, request)).andStubReturn("&urlForUser");
        expect(builder.getSearchUrlForHeaderCell(user, yMapper, request)).andStubReturn("&urlForUser");
        expect(builder.getSearchUrlForCell(user, user, map, request)).andStubReturn("&urlForUser&urlForUser");
        replay(builder);

        expect(map.getCoordinate(user, user)).andReturn(3);
        expect(map.getYAxisUniqueTotal(user)).andReturn(3);
        expect(map.getXAxisUniqueTotal(user)).andReturn(3);
        expect(map.getUniqueTotal()).andReturn(3L);

        expect(mockTransformer.transformFromAssignee("assignees", user, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>")).times(2);
        replay(mockTransformer);
        

        TwoDimensionalStatsResource.TwoDimensionalProperties properties = buildTwoDimensionalProperties(objects, objects, true, "assignees", "assignees", builder, "Assignee", "Assignee");

        assertEquals("http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&requestId=1337", properties.getFilter().getFilterUrl());
        assertEquals(true, properties.isShowTotals());
        List<TwoDimensionalStatsResource.Cell> firstRowCells = properties.getFirstRow().getCells();
        assertEquals(2, firstRowCells.size());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>", firstRowCells.get(0).getMarkup());
        assertEquals("T:", firstRowCells.get(1).getMarkup());
        assertEquals(2, properties.getRows().size());
        TwoDimensionalStatsResource.Row nextRow = properties.getRows().get(0);
        List<TwoDimensionalStatsResource.Cell> nextRowCells = nextRow.getCells();
        assertFalse(properties.getFilter().isEmpty());
        assertEquals(3, nextRowCells.size());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>Foo Guy</a>", nextRowCells.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser&urlForUser'>3</a>", nextRowCells.get(1).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>3</a>", nextRowCells.get(2).getMarkup());
        TwoDimensionalStatsResource.Row totalsRow = properties.getRows().get(1);
        List<TwoDimensionalStatsResource.Cell> totalRowsCell = totalsRow.getCells();
        assertEquals(3, totalRowsCell.size());
        assertEquals("Total Unique Issues:", totalRowsCell.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser'>3</a>", totalRowsCell.get(1).getMarkup());
        assertEquals("3", totalRowsCell.get(2).getMarkup());
        assertEquals(1, properties.getTotalRows());
    }

    public void testTwoResultsWithShowTotals()
    {
        /* Test data :

                        Assignee
                        fooGuy      fooGuy2
            project1    1           2       3
            project2    3           4       7
            total       4           6       10
         */

        request.setId(1337L);

        User userOne = new MockUser("fooGuy", "Foo Guy", "foo@bar.com");
        User userTwo = new MockUser("fooGuy2", "Foo Guy2", "foo@bar.com");

        GenericValue projectOne = createMock(GenericValue.class);
        GenericValue projectTwo = createMock(GenericValue.class);

        expect(projectOne.getString("name")).andReturn("Project1");
        expect(projectTwo.getString("name")).andReturn("Project2");

        replay(projectOne, projectTwo);

        List xObjects = Arrays.asList(userOne, userTwo);
        List yObjects = Arrays.asList(projectOne, projectTwo);

        I18nHelper i18nHelper = createMock(I18nHelper.class);
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.xaxis")).andReturn("T:");
        expect(i18nHelper.getText("gadget.twodimensionalfilterstats.total.yaxis")).andReturn("Total Unique Issues");
        replay(i18nHelper);

        replay(xMapper, yMapper);
        expect(mockAuthCtx.getLoggedInUser()).andStubReturn(userOne);
        expect(mockAuthCtx.getI18nHelper()).andStubReturn(i18nHelper);
        replay(mockAuthCtx);
        //First should be filter url, others should be urls for header, then objects
        expect(mockSearchService.getQueryString(userOne, request.getQuery())).andStubReturn("theFilterUrl");
        replay(mockSearchService);

        StatsSearchUrlBuilder builder = createMock(StatsSearchUrlBuilder.class);

        expect(builder.getSearchUrlForHeaderCell(userOne, xMapper, request)).andStubReturn("&urlForUser1");
        expect(builder.getSearchUrlForHeaderCell(userOne, yMapper, request)).andStubReturn("&urlForUser1");
        expect(builder.getSearchUrlForHeaderCell(userTwo, xMapper, request)).andStubReturn("&urlForUser2");
        expect(builder.getSearchUrlForHeaderCell(userTwo, yMapper, request)).andStubReturn("&urlForUser2");
        expect(builder.getSearchUrlForHeaderCell(projectOne, yMapper, request)).andStubReturn("&urlForProject1");
        expect(builder.getSearchUrlForHeaderCell(projectTwo, yMapper, request)).andStubReturn("&urlForProject2");
        expect(builder.getSearchUrlForHeaderCell(projectOne, xMapper, request)).andStubReturn("&urlForProject1");
        expect(builder.getSearchUrlForHeaderCell(projectTwo, xMapper, request)).andStubReturn("&urlForProject2");

        expect(builder.getSearchUrlForCell(userOne, projectOne, map, request)).andStubReturn("&urlForUser1InProject1");
        expect(builder.getSearchUrlForCell(userTwo, projectOne, map, request)).andStubReturn("&urlForUser2InProject1");
        expect(builder.getSearchUrlForCell(userOne, projectTwo, map, request)).andStubReturn("&urlForUser1InProject2");
        expect(builder.getSearchUrlForCell(userTwo, projectTwo, map, request)).andStubReturn("&urlForUser2InProject2");

        replay(builder);
        expect(map.getCoordinate(userOne, projectOne)).andReturn(1);
        expect(map.getCoordinate(userTwo, projectOne)).andReturn(2);
        expect(map.getCoordinate(userOne, projectTwo)).andReturn(3);
        expect(map.getCoordinate(userTwo, projectTwo)).andReturn(4);

        expect(map.getYAxisUniqueTotal(projectOne)).andReturn(3);
        expect(map.getYAxisUniqueTotal(projectTwo)).andReturn(7);
        expect(map.getXAxisUniqueTotal(userOne)).andReturn(4);
        expect(map.getXAxisUniqueTotal(userTwo)).andReturn(6);

        expect(map.getUniqueTotal()).andReturn(10L);

        expect(mockTransformer.transformFromAssignee("assignees", userOne, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser1" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser1'>Foo Guy</a>")).times(1);
        expect(mockTransformer.transformFromAssignee("assignees", userTwo, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser2" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser2'>Foo Guy2</a>")).times(1);
        expect(mockTransformer.transformFromProject("project", projectOne, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject1" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject1'>Project1</a>")).times(1);
        expect(mockTransformer.transformFromProject("project", projectTwo, "http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject2" )).andReturn(new StatsMarkup("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject2'>Project2</a>")).times(1);
        replay(mockTransformer);

        TwoDimensionalStatsResource.TwoDimensionalProperties properties = buildTwoDimensionalProperties(xObjects, yObjects, true, "assignees", "project", builder, "Assignee", "Project");

        assertEquals("http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&requestId=1337", properties.getFilter().getFilterUrl());
        assertEquals(true, properties.isShowTotals());
        List<TwoDimensionalStatsResource.Cell> firstRowCells = properties.getFirstRow().getCells();
        assertEquals(3, firstRowCells.size());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser1'>Foo Guy</a>", firstRowCells.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser2'>Foo Guy2</a>", firstRowCells.get(1).getMarkup());
        assertEquals("T:", firstRowCells.get(2).getMarkup());
        assertEquals(3, properties.getRows().size());
        TwoDimensionalStatsResource.Row nextRow = properties.getRows().get(0);
        List<TwoDimensionalStatsResource.Cell> nextRowCells = nextRow.getCells();
        assertFalse(properties.getFilter().isEmpty());
        assertEquals(4, nextRowCells.size());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject1'>Project1</a>", nextRowCells.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser1InProject1'>1</a>", nextRowCells.get(1).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser2InProject1'>2</a>", nextRowCells.get(2).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject1'>3</a>", nextRowCells.get(3).getMarkup());

        List<TwoDimensionalStatsResource.Cell> thirdRowCells = properties.getRows().get(1).getCells();
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject2'>Project2</a>", thirdRowCells.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser1InProject2'>3</a>", thirdRowCells.get(1).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser2InProject2'>4</a>", thirdRowCells.get(2).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForProject2'>7</a>", thirdRowCells.get(3).getMarkup());

        TwoDimensionalStatsResource.Row totalsRow = properties.getRows().get(2);
        List<TwoDimensionalStatsResource.Cell> totalRowsCell = totalsRow.getCells();
        assertEquals(4, totalRowsCell.size());
        assertEquals("Total Unique Issues:", totalRowsCell.get(0).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser1'>4</a>", totalRowsCell.get(1).getMarkup());
        assertEquals("<a href='http://localhost:8090/JIRA/secure/IssueNavigator.jspa?mode=hide&reset=true&urlForUser2'>6</a>", totalRowsCell.get(2).getMarkup());
        assertEquals("10", totalRowsCell.get(3).getMarkup());
        assertEquals(2, properties.getTotalRows());
    }

    private TwoDimensionalStatsResource.TwoDimensionalProperties buildTwoDimensionalProperties(List<?> xObjects, List<?> yObjects, boolean showTotals, String xStatsType, String yStatsType, StatsSearchUrlBuilder builder, String xDisplayName, String yDisplayName)
    {
        MockSearchQueryBackedResource resource = makeResource();

        resource.setBuilder(builder);

        resource.setExpectedQueryString("filter-1000");

        FilterStatisticsValuesGenerator generator = EasyMock.createMock(FilterStatisticsValuesGenerator.class);

        EasyMock.expect(generator.getStatsMapper(xStatsType)).andReturn(xMapper);
        EasyMock.expect(generator.getStatsMapper(yStatsType)).andReturn(yMapper);

        EasyMock.expect(stats.getDisplayName(xStatsType)).andStubReturn(xDisplayName);
        EasyMock.expect(stats.getDisplayName(yStatsType)).andStubReturn(yDisplayName);

        EasyMock.replay(generator, stats, utils);

        resource.setGenerator(generator);
        resource.setSearchRequest(request);
        resource.setXAxis(xObjects);
        resource.setYAxis(yObjects);

        return resource.buildProperties(request, "ascending", "natural", 0, xStatsType, showTotals, yStatsType);
    }


    private TwoDimensionalStatsResource.TwoDimensionalProperties buildTwoDimensionalPropertiesWithIrrelevant(List<?> xObjects, List<?> yObjects, boolean showTotals, String xStatsType, String yStatsType, StatsSearchUrlBuilder builder, String xDisplayName, String yDisplayName)
    {
        MockSearchQueryBackedResourceWithIrrelevantData resource = makeResourceWithIrrelevantData();

        resource.setBuilder(builder);

        resource.setExpectedQueryString("filter-1000");

        FilterStatisticsValuesGenerator generator = EasyMock.createMock(FilterStatisticsValuesGenerator.class);

        EasyMock.expect(generator.getStatsMapper(xStatsType)).andReturn(xMapper);
        EasyMock.expect(generator.getStatsMapper(yStatsType)).andReturn(yMapper);

        EasyMock.expect(stats.getDisplayName(xStatsType)).andStubReturn(xDisplayName);
        EasyMock.expect(stats.getDisplayName(yStatsType)).andStubReturn(yDisplayName);

        EasyMock.replay(generator, stats, utils);

        resource.setGenerator(generator);
        resource.setSearchRequest(request);
        resource.setXAxis(xObjects);
        resource.setYAxis(yObjects);

        return resource.buildProperties(request, "ascending", "natural", 0, xStatsType, showTotals, yStatsType);
    }

    class MockSearchQueryBackedResource extends TwoDimensionalStatsResource
    {
        protected String expectedQueryString;
        protected Collection<ValidationError> errorsToReturn = new ArrayList<ValidationError>();
        protected SearchRequest searchRequest;
        protected List<?> xAxis;
        protected List<?> yAxis;
        protected StatsSearchUrlBuilder builder;

        public MockSearchQueryBackedResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext,
                                             final SearchService searchService, final PermissionManager permissionManager,
                                             StatisticTypesResource statisticTypesResource, SearchProvider searchProvider,
                                             VelocityRequestContextFactory velocityRequestContextFactory, FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer)
        {
            super(chartUtils, authenticationContext, searchService, permissionManager, statisticTypesResource, searchProvider, velocityRequestContextFactory, null, fieldValueToDisplayTransformer, null, null, null, null);
        }

        public void setBuilder(StatsSearchUrlBuilder builder)
        {
            this.builder = builder;
        }

        public void setSearchRequest(SearchRequest searchRequest)
        {
            this.searchRequest = searchRequest;
        }

        public void setXAxis(List<?> xAxis)
        {
            this.xAxis = xAxis;
        }

        public void setYAxis(List<?> yAxis)
        {
            this.yAxis = yAxis;
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

        protected TwoDimensionalStatsMap getAndPopulateTwoDimensionalStatsMap(StatisticsMapper xAxisMapper, StatisticsMapper yAxisMapper, SearchRequest searchRequest)
        {
            EasyMock.expect(map.getXAxis()).andReturn(xAxis);
            EasyMock.expect(map.getYAxis("natural", "ascending")).andReturn(yAxis);
            EasyMock.expect(map.getxAxisMapper()).andStubReturn(xAxisMapper);
            EasyMock.expect(map.getyAxisMapper()).andStubReturn(yAxisMapper);
            EasyMock.expect(map.hasIrrelevantYData()).andStubReturn(false);
            EasyMock.expect(map.hasIrrelevantXData()).andStubReturn(false);
            EasyMock.expect(map.getBothIrrelevant()).andStubReturn(0);
            EasyMock.replay(map);
            return map;
        }
    }

    class MockSearchQueryBackedResourceWithIrrelevantData extends MockSearchQueryBackedResource
    {

        public MockSearchQueryBackedResourceWithIrrelevantData(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final SearchService searchService, final PermissionManager permissionManager, StatisticTypesResource statisticTypesResource, SearchProvider searchProvider, VelocityRequestContextFactory velocityRequestContextFactory, FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer)
        {
            super(chartUtils, authenticationContext, searchService, permissionManager, statisticTypesResource, searchProvider, velocityRequestContextFactory, fieldValueToDisplayTransformer);
        }

        @Override
        protected TwoDimensionalStatsMap getAndPopulateTwoDimensionalStatsMap(final StatisticsMapper xAxisMapper, final StatisticsMapper yAxisMapper, final SearchRequest searchRequest)
        {
            EasyMock.expect(map.getXAxis()).andReturn(xAxis);
            EasyMock.expect(map.getYAxis("natural", "ascending")).andReturn(yAxis);
            EasyMock.expect(map.getxAxisMapper()).andStubReturn(xAxisMapper);
            EasyMock.expect(map.getyAxisMapper()).andStubReturn(yAxisMapper);
            EasyMock.expect(map.hasIrrelevantYData()).andStubReturn(true);
            EasyMock.expect(map.hasIrrelevantXData()).andStubReturn(true);

            for (Object xAxi : xAxis)
            {
                EasyMock.expect(map.getYAxisIrrelevantTotal(xAxi)).andStubReturn(1);
            }
            for (Object yAxi : yAxis)
            {
                EasyMock.expect(map.getXAxisIrrelevantTotal(yAxi)).andStubReturn(1);
            }

            EasyMock.expect(map.getBothIrrelevant()).andStubReturn(2);
            EasyMock.replay(map);
            return map;
        }
    }

}
