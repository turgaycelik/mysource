package com.atlassian.jira.web.action.admin.user;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.user.PreDeleteUserErrorsManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDeleteVeto;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteUserTest
{
    final private ApplicationUser mockApplicationUser = mock(ApplicationUser.class);
    final private User mockUser = mock(User.class);
    final private SearchRequestService mockSearchRequestService = mock(SearchRequestService.class, RETURNS_DEEP_STUBS);
    final private PortalPageService mockPortalPageService = mock(PortalPageService.class, RETURNS_DEEP_STUBS);
    final private UserUtil mockUserUtil = mock(UserUtil.class, RETURNS_DEEP_STUBS);
    final private UserDeleteVeto mockUserDeleteVeto = mock(UserDeleteVeto.class);

    private DeleteUser deleteUser;
    private Project mockProject;
    private String testKey;
    private String testName;
    private String testUserName;
    private int intCount;
    private long longCount;

    @org.junit.Before
    public void setUp() throws Exception
    {

        final BaseUrl mockBaseUrlLocator = mock(BaseUrl.class);
        mockProject = mock(Project.class);
        final ProjectComponent mockProjectComponent = mock(ProjectComponent.class);
        final PreDeleteUserErrorsManager mockPreDeleteUserErrorsManager = mock(PreDeleteUserErrorsManager.class);
        final ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
        final JiraAuthenticationContext mockAuthenticationContext = mock(JiraAuthenticationContext.class);
        final ProjectManager mockProjectManager = mock(ProjectManager.class);

        testUserName = "fred";
        testKey = "testKey";
        testName = "testName";
        final String testEncoding = "UTF-8";
        final long testProjectId = 1L;
        intCount = 1;
        longCount = 1;

        when(mockApplicationUser.getDirectoryUser()).thenReturn(mockUser);
        when(mockApplicationUser.getName()).thenReturn(testUserName);

        when(mockPreDeleteUserErrorsManager.getWarnings(mockUser)).thenReturn(emptyMessages());

        when(mockSearchRequestService.getNonPrivateFilters(mockApplicationUser).size()).thenReturn(0);
        when(mockSearchRequestService.getFiltersFavouritedByOthers(mockApplicationUser).size()).thenReturn(0);
        when(mockPortalPageService.getNonPrivatePortalPages(mockApplicationUser).size()).thenReturn(0);


        when(mockUserDeleteVeto.getCommentCountByAuthor(mockApplicationUser)).thenReturn(0L);

        when(mockUserUtil.getNumberOfAssignedIssuesIgnoreSecurity(mockUser,mockUser)).thenReturn(0L);
        when(mockUserUtil.getNumberOfReportedIssuesIgnoreSecurity(mockUser, mockUser)).thenReturn(0L);
        when(mockUserUtil.getComponentsUserLeads(mockApplicationUser).size()).thenReturn(0);
        when(mockUserUtil.getProjectsLeadBy(mockUser).size()).thenReturn(0);

        when(mockProject.getKey()).thenReturn(testKey);
        when(mockProject.getName()).thenReturn(testName);
        when(mockProjectComponent.getProjectId()).thenReturn(testProjectId);
        when(mockProjectComponent.getName()).thenReturn(testName);
        when(mockUserUtil.getComponentsUserLeads(mockUser)).thenReturn(Lists.newArrayList(mockProjectComponent));
        when(mockProjectManager.getProjectObj(testProjectId)).thenReturn(mockProject);

        when(mockBaseUrlLocator.getBaseUrl()).thenReturn("");

        when(mockApplicationProperties.getEncoding()).thenReturn(testEncoding);


        I18nHelper i18nHelper = new NoopI18nHelper();

        when(mockAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);


        new MockComponentWorker()
                .addMock(FeatureManager.class, mock(FeatureManager.class))
                .addMock(ApplicationProperties.class, mockApplicationProperties)
                .addMock(JiraAuthenticationContext.class, mockAuthenticationContext)
                .addMock(ProjectManager.class, mockProjectManager)
                .init();


        deleteUser = new DeleteUser(null, null, mockSearchRequestService, null, mockUserUtil, mockPortalPageService, null, null, mockUserDeleteVeto, mockPreDeleteUserErrorsManager, mockBaseUrlLocator)
        {
            @Override
            public ApplicationUser getApplicationUser()
            {
                return mockApplicationUser;
            }

            @Override
            public User getLoggedInUser()
            {
                return mockUser;
            }

            @Override
            public User getUser()
            {
                return mockUser;
            }

            @Override
            public UserManager.UserState getUserState()
            {
                return UserManager.UserState.NORMAL_USER;
            }
        };
    }

    private ImmutableList<WebErrorMessage> emptyMessages()
    {
        return ImmutableList.of();
    }

    @org.junit.Test
    public void testGetLinkableWarningsWithNonPrivateFilters() throws Exception
    {
        when(mockSearchRequestService.getNonPrivateFilters(mockApplicationUser).size()).thenReturn(intCount);
        Map<String, String> result = deleteUser.getLinkableWarnings();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.filters.created.counted{[" + intCount + "]}", "/secure/admin/filters/ViewSharedFilters.jspa?searchOwnerUserName=" + testUserName);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableWarningsWithFavoriteFilters() throws Exception
    {
        when(mockSearchRequestService.getFiltersFavouritedByOthers(mockApplicationUser).size()).thenReturn(intCount);
        Map<String, String> result = deleteUser.getLinkableWarnings();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.filters.favourited.counted{[" + intCount + "]}", null);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableWarningsWithNonPrivatePortalPages() throws Exception
    {
        when(mockPortalPageService.getNonPrivatePortalPages(mockApplicationUser).size()).thenReturn(intCount);
        Map<String, String> result = deleteUser.getLinkableWarnings();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.portalpages.created.counted{[" + intCount + "]}", "/secure/admin/dashboards/ViewSharedDashboards.jspa?searchOwnerUserName=" + testUserName);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableWarningsWithComponentsUserLeads() throws Exception
    {
        when(mockUserUtil.getComponentsUserLeads(mockApplicationUser).size()).thenReturn(intCount);
        Map<String, String> result = deleteUser.getLinkableWarnings();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.components.lead.counted{[" + intCount + "]}", null);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableErrorsWithNumberOfAssignedIssues() throws Exception
    {
        when(mockUserUtil.getNumberOfAssignedIssuesIgnoreSecurity(mockUser, mockUser)).thenReturn(longCount);
        Map<String, String> result = deleteUser.getLinkableErrors();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.assigned.issues.counted{[" + longCount + "]}", "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter%2Forder=ASC&sorter%2Ffield=priority&assigneeSelect=specificuser&assignee=" + testUserName);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableErrorsWithNumberOfReportedIssues() throws Exception
    {
        when(mockUserUtil.getNumberOfReportedIssuesIgnoreSecurity(mockUser, mockUser)).thenReturn(longCount);
        Map<String, String> result = deleteUser.getLinkableErrors();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.reported.issues.counted{[" + longCount + "]}", "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter%2Forder=ASC&sorter%2Ffield=priority&reporterSelect=specificuser&reporter=" + testUserName);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableErrorsWithCommentCountByAuthor() throws Exception
    {
        when(mockUserDeleteVeto.getCommentCountByAuthor(mockApplicationUser)).thenReturn(longCount);
        Map<String, String> result = deleteUser.getLinkableErrors();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.issue.comments.counted{[" + longCount + "]}", null);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetLinkableErrorsWithProjectsLead() throws Exception
    {
        when(mockUserUtil.getProjectsLeadBy(mockUser).size()).thenReturn(intCount);
        Map<String, String> result = deleteUser.getLinkableErrors();
        Map<String, String> expected = Maps.newHashMap();
        expected.put("admin.deleteuser.projects.lead.counted{[" + intCount + "]}", null);
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetProjectsUserLeadsError() throws Exception
    {
        when(mockUserUtil.getProjectsLeadBy(mockUser)).thenReturn(Lists.newArrayList(mockProject));
        Map<String, String> result = deleteUser.getProjectsUserLeadsError();
        Map<String, String> expected = Maps.newHashMap();
        expected.put(testName, "/plugins/servlet/project-config/" + testKey + "/summary");
        assertEquals(expected, result);
    }

    @org.junit.Test
    public void testGetComponentsUserLeadsWarning() throws Exception
    {
        Map<String, String> result = deleteUser.getComponentsUserLeadsWarning();
        Map<String, String> expected = Maps.newHashMap();
        expected.put(testName, "/plugins/servlet/project-config/" + testKey + "/components");
        assertEquals(expected, result);
    }
}
