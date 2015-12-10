package com.atlassian.jira.web.action;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import com.atlassian.query.Query;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestIssueActionSupport
{
    @Rule
    public MockHttp.DefaultMocks httpMocks = MockHttp.withDefaultMocks();

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    public HttpSession session;

    private IssueActionSupport ias;

    @Mock
    @AvailableInContainer
    private IssueManager issueManager;
    @Mock
    @AvailableInContainer
    private CustomFieldManager customFieldManager;
    @Mock
    @AvailableInContainer
    private AttachmentManager attachmentManager;
    @Mock
    @AvailableInContainer
    private VersionManager versionManager;
    @Mock
    @AvailableInContainer
    private UserIssueHistoryManager userHistoryManager;
    @Mock
    @AvailableInContainer
    private TimeTrackingConfiguration timeTrackingConfiguration;
    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;
    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;
    @Mock
    @AvailableInContainer
    private SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    @Mock
    @AvailableInContainer
    private SearchRequestService searchRequestService;
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;
    @AvailableInContainer (instantiateMe = true)
    private MockI18nHelper i18nHelper;
    @Mock
    @AvailableInContainer
    private SearchService searchService;
    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;
    @AvailableInContainer (instantiateMe = true)
    private MockOfBizDelegator mockOfBizDelegator;

    @Mock
    private SessionSearchRequestManager sessionSearchRequestManager;


    @Before
    public void setUp() throws Exception
    {

        when(sessionSearchObjectManagerFactory.createSearchRequestManager(httpMocks.mockRequest())).thenReturn(sessionSearchRequestManager);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        ias = new IssueActionSupport();

    }

    @Test
    public void getSearchRequestShouldSetPermissionsWhenRequestWasModified()
    {
        SearchRequest sr = spy(MockSearchRequest.get("fred", 123L));
        SearchRequest fromDb = MockSearchRequest.get("fred", 123L);
        when(sessionSearchRequestManager.getCurrentObject()).thenReturn(sr);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(123L))).thenReturn(fromDb);

        sr.setModified(true);

        Assert.assertSame(sr, ias.getSearchRequest());

        verify(sr).setPermissions(fromDb.getPermissions());
    }

    @Test
    public void getSearchRequestShouldReplaceSearchWithFetchedFromDBWhenNothingHasChanged()
    {
        SearchRequest sr = spy(MockSearchRequest.get("fred", 123L));
        SearchRequest fromDb = spy(MockSearchRequest.get("fred", 123L));
        when(sessionSearchRequestManager.getCurrentObject()).thenReturn(sr);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(123L))).thenReturn(fromDb);

        sr.setModified(false);
        sr.setUseColumns(true);

        Assert.assertSame(fromDb, ias.getSearchRequest());

        verify(fromDb).setUseColumns(sr.useColumns());
        verify(sessionSearchRequestManager).setCurrentObject(fromDb);
    }

    @Test
    public void testGetSearchRequestNull()
    {
        Assert.assertNull(ias.getSearchRequest());
    }

    @Test
    public void testSetSearchRequest()
    {
        SearchRequest sr = new SearchRequest();

        ias.setSearchRequest(sr);

        verify(sessionSearchRequestManager).setCurrentObject(sr);
    }

    @Test
    public void testGetCurrentSQL()
    {
        Query q = mock(Query.class);
        SearchRequest sr = new MockSearchRequest("name", 123L);
        sr.setQuery(q);
        when(sessionSearchRequestManager.getCurrentObject()).thenReturn(sr);
        when(searchService.getJqlString(q)).thenReturn("strange JQL string!");

        assertEquals("strange JQL string!", ias.getCurrentJQL());
    }

    @Test
    public void testGetURLEncoded()
    {
        when(applicationProperties.getEncoding()).thenReturn("UTF-8");
        assertEquals(URLEncoder.encode("foobar baz"), ias.getUrlEncoded("foobar baz"));
    }

    @Test
    public void testGetPossibleVersions() throws Exception
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        final Version unreleased1 = new MockVersion(1, "foo");
        final Version unreleased2 = new MockVersion(2, "bar");
        final Version released1 = new MockVersion(3, "baz");
        final Version released2 = new MockVersion(4, "bat");
        when(versionManager.getVersionsUnreleased(1L, false)).thenReturn(ImmutableList.of(unreleased1, unreleased2));
        when(versionManager.getVersionsReleased(1L, false)).thenReturn(ImmutableList.of(released1, released2));

        List versions = ias.getPossibleVersions(project);
        assertEquals(6, versions.size());

        VersionProxy proxy = (VersionProxy) versions.get(0);
        assertEquals(-2, proxy.getKey());
        assertEquals("common.filters.unreleasedversions", proxy.getValue());

        proxy = (VersionProxy) versions.get(1);
        assertEquals(1, proxy.getKey());
        assertEquals("foo", proxy.getValue());

        proxy = (VersionProxy) versions.get(2);
        assertEquals(2, proxy.getKey());
        assertEquals("bar", proxy.getValue());

        proxy = (VersionProxy) versions.get(3);
        assertEquals(-3, proxy.getKey());
        assertEquals("common.filters.releasedversions", proxy.getValue());

        proxy = (VersionProxy) versions.get(4);
        assertEquals(4, proxy.getKey());
        assertEquals("bat", proxy.getValue());

        proxy = (VersionProxy) versions.get(5);
        assertEquals(3, proxy.getKey());
        assertEquals("baz", proxy.getValue());
    }


}
