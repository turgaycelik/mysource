package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;
import org.easymock.IAnswer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;

/**
 * Tests the parent of all search query-backed resources
 *
 * @since v4.0
 */
public class TestSearchQueryBackedResource extends ResourceTest
{
    private ChartUtils mockChartUtils;
    private JiraAuthenticationContext mockAuthCtx;
    private SearchService mockSearchService;
    private PermissionManager mockPermissionManager;
    private MockApplicationUser mockUser;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockChartUtils = mock(ChartUtils.class);
        mockAuthCtx = mock(JiraAuthenticationContext.class);
        mockSearchService = mock(SearchService.class);
        mockPermissionManager = mock(PermissionManager.class);

        mockUser = new MockApplicationUser("user");
        expect(mockAuthCtx.getUser()).andReturn(mockUser).anyTimes();
        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    private SearchQueryBackedResource createInstance()
    {
        return new SearchQueryBackedResource(mockChartUtils, mockAuthCtx,
                mockSearchService, mockPermissionManager, null)
        {
        };
    }

    public final void testGetSearchRequestAndValidate_filter()
    {
        String query = "filter-100";
        String expectedQuery = "filter-100";
        _testGetSearchRequestAndValidate_ok(query, expectedQuery, new Object());
    }

    public final void testGetSearchRequestAndValidate_project()
    {
        String query = "project-100";
        String expectedQuery = "project-100";
        Project p100 = stubProject(100L, "HOMOSAP", "homosapien");
        _testGetSearchRequestAndValidate_ok(query, expectedQuery, p100);
    }

    private void _testGetSearchRequestAndValidate_ok(final String query,
            final String expectedQuery, final Object value)
    {
        ChartUtilsMakeSearchRequestAnswer chartUtilsAnswer = new ChartUtilsMakeSearchRequestAnswer(value);
        expect(mockChartUtils.retrieveOrMakeSearchRequest(eq(expectedQuery), isA(Map.class))).
                andAnswer(chartUtilsAnswer);
        if (value instanceof Project)
        {
            expect(mockPermissionManager.hasPermission(Permissions.BROWSE, (Project) value, mockUser)).andReturn(true);
        }

        replayAll();

        SearchQueryBackedResource instance = createInstance();

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchRequest actual = instance.getSearchRequestAndValidate(query, errors, params);

        assertSame(chartUtilsAnswer.getSearchRequest(), actual);
        assertTrue(params.containsKey(chartUtilsAnswer.getKey()));
        assertTrue(errors.isEmpty());

        verifyAll();
    }

    public void testGetSearchRequestAndValidate_bad_filter()
    {
        String query = "filter-100";
        ValidationError expectedErrors = new ValidationError("projectOrFilterId", "gadget.common.invalid.filter");
        _testGetSearchRequestAndValidate_error(query, "bad", new Object(), expectedErrors);
    }

    public void testGetSearchRequestAndValidate_bad_project()
    {
        String query = "project-100";
        ValidationError expectedErrors = new ValidationError("projectOrFilterId", "gadget.common.invalid.project");
        _testGetSearchRequestAndValidate_error(query, "bad", new Object(), expectedErrors);
    }

    public void testGetSearchRequestAndValidate_secret_project()
    {
        String query = "project-100";
        Project p100 = stubProject(100L, "HOMOSAP", "homosapien");
        ValidationError expectedErrors = new ValidationError("projectOrFilterId", "gadget.common.invalid.project");
        _testGetSearchRequestAndValidate_error(query, "project", p100, expectedErrors);
    }

    public void testGetSearchRequestAndValidate_bad_jql()
    {
        String query = "jql-100";
        ValidationError expectedErrors = new ValidationError("projectOrFilterId", "gadget.common.invalid.jql");
        _testGetSearchRequestAndValidate_error(query, "bad", new Object(), expectedErrors);
    }

    public void testGetSearchRequestAndValidate_bad_query()
    {
        String query = "bad-100";
        ValidationError expectedErrors = new ValidationError("projectOrFilterId", "gadget.common.invalid.projectOrFilterId");
        _testGetSearchRequestAndValidate_error(query, "bad", new Object(), expectedErrors);
    }

    public void testGetSearchRequestAndValidate_null_query()
    {
        _testGetSearchRequestAndValidate_error(null, "bad", new Object(),
                new ValidationError("projectOrFilterId", "gadget.common.required.query"));
    }

    public void testGetSearchRequestAndValidate_empty_query()
    {
        _testGetSearchRequestAndValidate_error("", "bad", new Object(),
                new ValidationError("projectOrFilterId", "gadget.common.required.query"));
    }

    private void _testGetSearchRequestAndValidate_error(final String query,
            final String key, final Object value, final ValidationError... expectedErrors)
    {
        if (StringUtils.isNotEmpty(query))
        {
            expect(mockChartUtils.retrieveOrMakeSearchRequest(eq(query), isA(Map.class))).
                    andAnswer(new ChartUtilsMakeSearchRequestAnswer(key, value));
            if (value instanceof Project)
            {
                expect(mockPermissionManager.hasPermission(Permissions.BROWSE, (Project) value, mockUser)).andReturn(false);
            }
        }

        replayAll();

        SearchQueryBackedResource instance = createInstance();

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        Map<String, Object> params = new HashMap<String, Object>();
        SearchRequest actual = instance.getSearchRequestAndValidate(query, errors, params);

        assertNull(actual);
        assertEquals(CollectionBuilder.newBuilder(expectedErrors).asList(), errors);

        verifyAll();
    }

    public final void testGetFilterTitle_project()
    {
        Project project = stubProject(100L, "pkey", "pname");
        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("project", project).toHashMap();

        replayAll();

        String actual = createInstance().getFilterTitle(params);
        assertEquals("pname", actual);

        verifyAll();
    }

    public final void testGetFilterTitle_searchRequest()
    {
        SearchRequest r = new SearchRequest();
        r.setName("blah");
        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("searchRequest", r).toHashMap();

        String actual = createInstance().getFilterTitle(params);
        assertEquals("blah", actual);
    }

    public final void testGetFilterTitle_unsaved_search()
    {
        String actual = createInstance().getFilterTitle(new HashMap<String, Object>());
        assertEquals("gadget.common.anonymous.filter", actual);
    }

    public final void testGetFilterUrl_project()
    {
        Project project = stubProject(100L, "pkey", "pname");
        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("project", project).toHashMap();
        expect(mockSearchService.getQueryString(mockUser.getDirectoryUser(), new QueryImpl(new TerminalClauseImpl("project", Operator.EQUALS, "pkey")))).andReturn("&jqlQuery=blah");

        replayAll();

        String actual = createInstance().getFilterUrl(params);
        assertEquals("/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=blah", actual);

        verifyAll();
    }

    public final void testGetFilterUrl_loadedSearchRequest()
    {
        Query mockQuery = mock(Query.class);
        SearchRequest r = new SearchRequest(mockQuery, new MockApplicationUser("owner"), "name", "desc", 123L, 50L);

        replayAll();

        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("searchRequest", r).toHashMap();

        String actual = createInstance().getFilterUrl(params);
        assertEquals("/secure/IssueNavigator.jspa?mode=hide&requestId=123", actual);

        verifyAll();
    }

    public final void testGetFilterUrl_notLoadedSearchRequest()
    {
        Query mockQuery = mock(Query.class);
        SearchRequest r = new SearchRequest(mockQuery);
        expect(mockSearchService.getQueryString(mockUser.getDirectoryUser(), mockQuery)).andReturn("&jqlQuery=blah");

        replayAll();

        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("searchRequest", r).toHashMap();

        String actual = createInstance().getFilterUrl(params);
        assertEquals("/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=blah", actual);

        verifyAll();
    }

    public final void testGetFilterUrl_nullSearchRequest()
    {
        expect(mockSearchService.getQueryString(eq(mockUser.getDirectoryUser()), isA(Query.class))).andReturn("&jqlQuery=foo");

        replayAll();

        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("searchRequest", null).toHashMap();

        String actual = createInstance().getFilterUrl(params);
        assertEquals("/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=foo", actual);

        verifyAll();
    }

    public final void testGetFilterUrl_noSearchRequest()
    {
        String actual = createInstance().getFilterUrl(new HashMap<String, Object>());
        assertEquals("", actual);
    }

    private Project stubProject(final long pid, final String projKey, final String projName)
    {
        Project project = mock(Project.class);
        expect(project.getId()).andReturn(pid).anyTimes();
        expect(project.getKey()).andReturn(projKey).anyTimes();
        expect(project.getName()).andReturn(projName).anyTimes();
        return project;
    }

    class ChartUtilsMakeSearchRequestAnswer implements IAnswer<SearchRequest>
    {
        private String key;
        private Object value;
        private SearchRequest searchRequest = new SearchRequest();

        ChartUtilsMakeSearchRequestAnswer(Object value)
        {
            this.value = value;
        }

        ChartUtilsMakeSearchRequestAnswer(String key, Object value)
        {
            this.key = key;
            this.value = value;
        }

        public SearchRequest answer() throws Throwable
        {
            String q = (String) getCurrentArguments()[0];
            Map<String, Object> params = (Map<String, Object>) getCurrentArguments()[1];
            if (key == null)
            {
                if (q.startsWith("filter-"))
                {
                    key = "searchRequest";
                }
                else if (q.startsWith("project-"))
                {
                    key = "project";
                }
                else if (q.startsWith("jql-"))
                {
                    key = "searchRequest";
                }
            }
            params.put(key, value);
            return searchRequest;
        }

        public String getKey()
        {
            return key;
        }

        public SearchRequest getSearchRequest()
        {
            return searchRequest;
        }
    }
}
