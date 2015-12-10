package com.atlassian.jira.rest.v2.preference;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectCategoryJsonBean;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectCategoryImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.ProjectCategoryResource;
import com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestProjectCategoryResource
{
    private static final String NORMAL_USER = "user";
    private static final String ADMIN_USER = "admin";

    private static final Long projectCategoryId1 = 10000l;

    private static final ProjectCategory CATEGORY1 = new ProjectCategoryImpl(projectCategoryId1, "FIRST", "First Project Category");
    private static final ProjectCategory CATEGORY2 = new ProjectCategoryImpl(10001l, "SECOND", "Second Project Category");
    private static final List<ProjectCategory> PROJECT_CATEGORIES = ImmutableList.of(CATEGORY1, CATEGORY2);

    private static final ProjectCategoryBean NEW_CATEGORY = new ProjectCategoryBean(CATEGORY1, null);

    @Mock
    private ProjectManager projectManager;

    @Mock
    private JiraBaseUrls jiraBaseUrls;

    @Mock
    private JiraAuthenticationContext authContext;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private I18nHelper i18n;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Rule
    public RuleChain chain = MockitoMocksInContainer.forTest(this);

    @Rule
    public InitMockitoMocks intiMocks = new InitMockitoMocks(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ProjectCategoryResource projectCategoryResource;

    private void noUser()
    {
        when(authContext.getUser()).thenReturn(null);
    }

    private void normalUser()
    {
        final ApplicationUser applicationUser = new MockApplicationUser(NORMAL_USER);
        when(authContext.getUser()).thenReturn(applicationUser);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(Boolean.FALSE);
    }

    private void adminUser()
    {
        final ApplicationUser applicationUser = new MockApplicationUser(ADMIN_USER);
        when(authContext.getUser()).thenReturn(applicationUser);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(Boolean.TRUE);
    }

    @Before
    public void setUp() throws Exception
    {
        when(applicationProperties.getEncoding()).thenReturn(ApplicationPropertiesImpl.DEFAULT_ENCODING);
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn(UriBuilder.fromUri("http://localhost").build().toString());
        projectCategoryResource = new ProjectCategoryResource(projectManager, jiraBaseUrls, authContext, permissionManager, i18n);
    }

    @Test
    public void gettingAllProjectCategoriesAsUnauthenticatedUserReturnsIterables()
    {
        noUser();
        when(projectManager.getAllProjectCategories()).thenReturn(PROJECT_CATEGORIES);

        final Response response = projectCategoryResource.getAllProjectCategories();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        //noinspection unchecked
        final Iterable<ProjectCategory> entity = (Iterable<ProjectCategory>) response.getEntity();
        final Matcher<Iterable<ProjectCategory>> iterableWithSizeMatcher = iterableWithSize(PROJECT_CATEGORIES.size());
        assertThat(entity, iterableWithSizeMatcher);
    }

    @Test
    public void gettingProjectCategoriesByIdThatExistsAsUnauthenticatedUserReturnsIt()
    {
        noUser();
        when(projectManager.getProjectCategoryObject(projectCategoryId1)).thenReturn(CATEGORY1);

        final Response response = projectCategoryResource.getProjectCategoryById(projectCategoryId1);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertResponseHasProjectCategory(response, CATEGORY1);
    }

    @Test
    public void creatingProjectCategoryWithNormalUserFails()
    {
        normalUser();
        final Response response = projectCategoryResource.createProjectCategory(NEW_CATEGORY);
        assertThat(response.getStatus(), equalTo(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void removingProjectCategoryWithNormalUserFails() throws AtlassianCoreException
    {
        normalUser();
        final Response response = projectCategoryResource.removeProjectCategory(CATEGORY1.getId());
        assertThat(response.getStatus(), equalTo(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void updatingProjectCategoryWithNormalUserFails() throws AtlassianCoreException
    {
        normalUser();
        final Response response = projectCategoryResource.updateProjectCategory(CATEGORY2.getId(), NEW_CATEGORY);
        assertThat(response.getStatus(), equalTo(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void creatingProjectCategoryWithAdminUserSucceeds()
    {
        adminUser();
        when(projectManager.createProjectCategory(CATEGORY1.getName(), CATEGORY1.getDescription())).thenReturn(CATEGORY1);

        final Response response = projectCategoryResource.createProjectCategory(NEW_CATEGORY);
        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        assertResponseHasProjectCategory(response, CATEGORY1);
        verify(projectManager).createProjectCategory(CATEGORY1.getName(), CATEGORY1.getDescription());
    }

    @Test
    public void removingProjectCategoryWithAdminUserSucceeds() throws AtlassianCoreException
    {
        adminUser();
        when(projectManager.getProjectCategoryObject(CATEGORY1.getId())).thenReturn(CATEGORY1);

        final Response response = projectCategoryResource.removeProjectCategory(CATEGORY1.getId());
        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
        verify(projectManager).removeProjectCategory(CATEGORY1.getId());
    }

    @Test
    public void updatingProjectCategoryWithAdminUserSucceeds() throws AtlassianCoreException
    {
        adminUser();
        when(projectManager.getProjectCategoryObject(CATEGORY2.getId())).thenReturn(CATEGORY1);

        final Response response = projectCategoryResource.updateProjectCategory(CATEGORY2.getId(), NEW_CATEGORY);

        verify(projectManager).updateProjectCategory(any(ProjectCategory.class));
        verify(projectManager, atLeastOnce()).getProjectCategoryObject(CATEGORY2.getId());
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertResponseHasProjectCategory(response, CATEGORY1);
    }

    private void assertResponseHasProjectCategory(final Response response, final ProjectCategory projectCategory)
    {
        assertThat(response.getEntity(), instanceOf(ProjectCategoryJsonBean.class));
        ProjectCategoryJsonBean bean = (ProjectCategoryJsonBean)response.getEntity();
        assertThat(Long.parseLong(bean.getId()), equalTo(projectCategory.getId()));
        assertThat(bean.getName(), equalTo(projectCategory.getName()));
        assertThat(bean.getDescription(), equalTo(projectCategory.getDescription()));
    }
}
