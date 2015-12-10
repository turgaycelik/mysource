package com.atlassian.jira.plugin.link.applinks.rest.resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.IssueFinder;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @since v6.3
 */
@RunWith (MockitoJUnitRunner.class)
public class TestAppLinksInfoResource
{
    private static final String APPLICATION_TYPE = "com.atlassian.applinks.application.jira.JiraApplicationType";

    private static final String ISSUE_KEY = "DEMO-1";
    private static final int ISSUE_ID = 100;

    private static final int PROJECT_ID = 100;

    @Mock
    private ApplicationLinkService applicationLinkService;

    @Mock
    private IssueFinder issueFinder;

    @Mock
    private JiraAuthenticationContext authenticationContext;

    private MockPermissionManager permissionManager;

    @Before
    public void setUp() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(ISSUE_ID, ISSUE_KEY);
        final Project mockProject = new MockProject(PROJECT_ID);
        mockIssue.setProjectObject(mockProject);

        when(issueFinder.findIssue(eq(ISSUE_KEY), any(ErrorCollection.class))).thenReturn(mockIssue);

        permissionManager = new MockPermissionManager() {
            @Override
            public boolean hasPermission(@Nonnull ProjectPermissionKey permissionKey, @Nonnull Issue issue, @Nullable ApplicationUser user)
            {
                return isDefaultPermission();
            }
        };
    }

    @Test
    public void testPermissionCheckFailsOnLink()
    {
        AppLinksInfoResource resource = new AppLinksInfoResource(applicationLinkService, issueFinder, authenticationContext, permissionManager);

        Response response = resource.getAppLinksInfo(APPLICATION_TYPE, ISSUE_KEY);
        assertThat(response.getStatus(), equalTo(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void testPermissionLink()
    {
        // since we are not checking any other permissions
        permissionManager.setDefaultPermission(true);

        AppLinksInfoResource resource = new AppLinksInfoResource(applicationLinkService, issueFinder, authenticationContext, permissionManager);

        Response response = resource.getAppLinksInfo(APPLICATION_TYPE, ISSUE_KEY);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
}
