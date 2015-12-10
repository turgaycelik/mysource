package com.atlassian.jira.sharing.type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;

import com.google.common.collect.ImmutableList;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link com.atlassian.jira.sharing.type.ProjectShareTypeRenderer}.
 *
 * @since v3.13
 */

@SuppressWarnings ("ResultOfObjectAllocationIgnored")
public class TestProjectShareTypeRenderer
{
    private static final User ANONYMOUS = null;
    private static final String PROJECT1_KEY = "PROJONE";
    private static final String PROJECT1_NAME = "Project One";
    private static final long PROJECT1_ID = 10L;

    private static final long ROLE1_ID = 1000L;
    private static final String ROLE1_NAME = "Cool";
    private static final String ROLE1_DESCRIPTION = "Cool Users Role";

    private static final Project PROJECT1 = new MockProject(PROJECT1_ID, PROJECT1_KEY, PROJECT1_NAME);
    private static final Project PROJECT2 = new MockProject(11, "PROJTWO", "Project Two");
    private static final Project PROJECT3 = new MockProject(12, "ABC", "ABC Project");
    private static final Project PROJECTXSS = new MockProject(13, "XSS", "<script>alert(\"I'm an XSS attack\");</script>");
    public static final String PROJECTXSS_ENCODED = "&lt;script&gt;alert(&quot;I&#39;m an XSS attack&quot;);&lt;/script&gt;";

    private static final ProjectRole ROLE1 = new MockProjectRoleManager.MockProjectRole(ROLE1_ID, ROLE1_NAME, ROLE1_DESCRIPTION);
    private static final ProjectRole ROLE2 = new MockProjectRoleManager.MockProjectRole(1001, "Dude", "The dudemisters.");
    private static final ProjectRole ROLE3 = new MockProjectRoleManager.MockProjectRole(1002, "Nerd", "The really really nerds.");
    private static final ProjectRole ROLE4 = new MockProjectRoleManager.MockProjectRole(1003, "Geek", "The really really geeks.");
    private static final ProjectRole ROLEXSS = new MockProjectRoleManager.MockProjectRole(1004, "<b>name</b>", "This is an XSS attack.");
    private static final String ROLEXSS_NAME_ENCODED = "&lt;b&gt;name&lt;/b&gt;";

    private static final SharePermission PROJECT_PERM = new SharePermissionImpl(ProjectShareType.TYPE, String.valueOf(PROJECT1_ID), null);
    private static final SharePermission PROJECT_PERM_XSS = new SharePermissionImpl(ProjectShareType.TYPE, PROJECTXSS.getId().toString(), null);
    private static final SharePermission ROLE_PERM_1 = new SharePermissionImpl(ProjectShareType.TYPE, String.valueOf(PROJECT1_ID), String.valueOf(ROLE1_ID));
    private static final SharePermission ROLE_PERM_XSS = new SharePermissionImpl(ProjectShareType.TYPE, String.valueOf(PROJECT1_ID), ROLEXSS.getId().toString());

    private static final String VELOCITY_RETURN = "<selector><option>b</option></selector>";
    private static final String UNKNOWN_PROJECT = "[Unknown Project]";
    private static final String UNKNOWN_ROLE = "[Unknown Role]";
    private static final String PROJECTS_KEY = "projects";
    private static final String ROLES_KEY = "roles";
    private static final String ROLES_MAP_KEY = "rolesMap";

    private JiraAuthenticationContext userCtx;
    private JiraAuthenticationContext anonymousCtx;
    private User user;
    private VelocityTemplatingEngine templatingEngine;
    private PermissionManager permMgr;
    private ProjectManager projectMgr;
    private ProjectRoleManager projectRoleMgr;
    private ProjectShareTypeRenderer renderer;
    private ProjectFactory projectFactory;
    private EncodingConfiguration encoding = new EncodingConfiguration()
    {
        public String getEncoding()
        {
            return "UTF-8";
        }
    };

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        user = new MockUser("test");
        userCtx = createAuthenticationContext(user);
        anonymousCtx = createAuthenticationContext(null);
        templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput(VELOCITY_RETURN).get();

        permMgr = mock(PermissionManager.class, new Strict());
        projectMgr = mock(ProjectManager.class, new Strict());
        projectRoleMgr = mock(ProjectRoleManager.class, new Strict());
        projectFactory = new MockProjectFactory();
        renderer = new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, projectFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        userCtx = null;
        user = null;
        templatingEngine = null;
        encoding = null;
        anonymousCtx = null;

        projectRoleMgr = null;
        projectMgr = null;
        permMgr = null;

        projectFactory = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullProjectManager()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, null, projectRoleMgr, permMgr, projectFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullProjectRoleManager()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, null, permMgr, projectFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullPermissionManager()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, null, projectFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullProjectFactory()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, null);
    }

    @Test
    public void testGetShareTypeLabel()
    {
        final String html = renderer.getShareTypeLabel(userCtx);
        assertThat(html, not(blank()));
    }

    @Test
    public void testGetShareTypeLabelAnonymousUser()
    {
        final String html = renderer.getShareTypeLabel(anonymousCtx);
        assertThat(html, not(blank()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToGetAShareTypeLabelGivenANullAuthenticationContext()
    {
        renderer.getShareTypeLabel(null);
    }

    @Test
    public void testIsAddButtonNeededTrue()
    {
        doReturn(ImmutableList.of(PROJECT1.getGenericValue())).when(permMgr).getProjects(Permissions.BROWSE, user);

        assertThat(renderer.isAddButtonNeeded(userCtx), is(true));
    }

    @Test
    public void testIsAddButtonNeededFalse()
    {
        doReturn(Collections.emptyList()).when(permMgr).getProjects(Permissions.BROWSE, user);

        assertThat(renderer.isAddButtonNeeded(userCtx), is(false));
    }

    @Test
    public void testIsAddButtonNeededTrueWithAnonymousUser()
    {
        doReturn(ImmutableList.of(PROJECT1.getGenericValue())).when(permMgr).getProjects(Permissions.BROWSE, ANONYMOUS);

        assertThat(renderer.isAddButtonNeeded(anonymousCtx), is(true));
    }

    @Test
    public void testIsAddButtonNeededFalseWithAnonymousUser()
    {
        doReturn(Collections.emptyList()).when(permMgr).getProjects(Permissions.BROWSE, ANONYMOUS);

        assertThat(renderer.isAddButtonNeeded(anonymousCtx), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkingWhetherAnAddButtonIsNeededOrNotShouldFailGivenANullAuthenticationContext()
    {
        renderer.isAddButtonNeeded(null);
    }

    @Test
    public void testRenderPermissionOfProjectWithUser()
    {
        doReturn(PROJECT1).when(projectMgr).getProjectObj(PROJECT1_ID);

        final String html = renderer.renderPermission(PROJECT_PERM, userCtx);
        assertThat(html, containsString(PROJECT1_NAME));
    }

    @Test
    public void testRenderPermissionOfProjectXSS()
    {
        doReturn(PROJECTXSS).when(projectMgr).getProjectObj(PROJECTXSS.getId());

        final String html = renderer.renderPermission(PROJECT_PERM_XSS, userCtx);
        assertThat(html, containsString(PROJECTXSS_ENCODED));
        assertThat(html, not(containsString(PROJECTXSS.getName())));
    }

    @Test
    public void testRenderPermissionOfRoleXSS()
    {
        doReturn(PROJECT1).when(projectMgr).getProjectObj(PROJECT1_ID);
        doReturn(ROLEXSS).when(projectRoleMgr).getProjectRole(ROLEXSS.getId());

        final String html = renderer.renderPermission(ROLE_PERM_XSS, userCtx);
        assertThat(html, containsString(PROJECT1_NAME));
        assertThat(html, containsString(ROLEXSS_NAME_ENCODED));
        assertThat(html, not(containsString(ROLEXSS.getName())));
    }

    @Test
    public void testRenderPermissionOfNoProjectWithUser()
    {
        doReturn(null).when(projectMgr).getProjectObj(PROJECT1_ID);

        final String html = renderer.renderPermission(PROJECT_PERM, userCtx);
        assertThat(html, containsString(UNKNOWN_PROJECT));
    }

    @Test
    public void testRenderPermissionOfNoRoleWithUser()
    {
        doReturn(PROJECT1).when(projectMgr).getProjectObj(PROJECT1_ID);
        doReturn(null).when(projectRoleMgr).getProjectRole(ROLE1_ID);

        final String html = renderer.renderPermission(ROLE_PERM_1, userCtx);
        assertThat(html, containsString(PROJECT1_NAME));
        assertThat(html, containsString(UNKNOWN_ROLE));
    }

    @Test
    public void testRenderPermissionOfNoProjectNoRoleWithUser()
    {
        doReturn(null).when(projectMgr).getProjectObj(PROJECT1_ID);
        doReturn(null).when(projectRoleMgr).getProjectRole(ROLE1_ID);

        final String html = renderer.renderPermission(ROLE_PERM_1, userCtx);
        assertThat(html, containsString(UNKNOWN_PROJECT));
        assertThat(html, containsString(UNKNOWN_ROLE));
    }

    @Test
    public void testRenderPermissionOfProjectWithAnonymousUser()
    {
        doReturn(PROJECT1).when(projectMgr).getProjectObj(PROJECT1_ID);

        final String html = renderer.renderPermission(PROJECT_PERM, anonymousCtx);
        assertThat(html, containsString(PROJECT1_NAME));
    }

    @Test
    public void testRenderPermissionOfRoleWithAnonymousUser()
    {
        doReturn(PROJECT1).when(projectMgr).getProjectObj(PROJECT1_ID);
        doReturn(ROLE1).when(projectRoleMgr).getProjectRole(ROLE1_ID);

        final String html = renderer.renderPermission(ROLE_PERM_1, anonymousCtx);
        assertThat(html, containsString(PROJECT1_NAME));
        assertThat(html, containsString(ROLE1_NAME));
    }

    @Test
    public void testRenderPermissionOfNoProjectWithAnonymousUser()
    {
        doReturn(null).when(projectMgr).getProjectObj(PROJECT1_ID);

        final String html = renderer.renderPermission(PROJECT_PERM, anonymousCtx);
        assertThat(html, containsString(UNKNOWN_PROJECT));
    }

    @Test
    public void testRenderPermissionOfNoRoleWithAnonymousUser()
    {
        doReturn(PROJECT1).when(projectMgr).getProjectObj(PROJECT1_ID);
        doReturn(null).when(projectRoleMgr).getProjectRole(ROLE1_ID);

        final String html = renderer.renderPermission(ROLE_PERM_1, anonymousCtx);
        assertThat(html, containsString(PROJECT1_NAME));
        assertThat(html, containsString(UNKNOWN_ROLE));
    }

    @Test
    public void testRenderPermissionOfNoProjectNoRoleWithAnonymousUser()
    {
        doReturn(null).when(projectMgr).getProjectObj(PROJECT1_ID);
        doReturn(null).when(projectRoleMgr).getProjectRole(ROLE1_ID);

        final String html = renderer.renderPermission(ROLE_PERM_1, anonymousCtx);
        assertThat(html, containsString(UNKNOWN_PROJECT));
        assertThat(html, containsString(UNKNOWN_ROLE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRenderANullPermissionType()
    {
        renderer.renderPermission(null, userCtx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRenderAPermissionGivenANullAuthenticationContext()
    {
        renderer.renderPermission(ROLE_PERM_1, null);
    }

    @Test
    public void testGetShareTypeEditor()
    {
        doReturn(ImmutableList.of(PROJECT1.getGenericValue(), PROJECT2.getGenericValue(), PROJECT3.getGenericValue()))
                .when(permMgr).getProjects(Permissions.BROWSE, user);
        doReturn(Collections.emptyList()).when(projectRoleMgr).getProjectRoles(user, PROJECT1);
        doReturn(ImmutableList.of(ROLE2, ROLE3)).when(projectRoleMgr).getProjectRoles(user, PROJECT2);
        doReturn(ImmutableList.of(ROLE3, ROLE4)).when(projectRoleMgr).getProjectRoles(user, PROJECT3);

        renderer = new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, projectFactory)
        {
            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertThat(params, hasEntry(PROJECTS_KEY, (Object)ImmutableList.of(PROJECT3, PROJECT1, PROJECT2)));
                assertThat(params, hasEntry(ROLES_KEY, (Object) ImmutableList.of(ROLE2, ROLE4, ROLE3)));
                assertThat(params, hasKey(ROLES_MAP_KEY));

                final Map<?,?> rolesMap = (Map<?,?>) params.get(ROLES_MAP_KEY);
                assertJsonEquals((String) rolesMap.get(PROJECT1.getId()), Collections.emptyList());
                assertJsonEquals((String) rolesMap.get(PROJECT2.getId()), ImmutableList.of(1001L, 1002L));
                assertJsonEquals((String) rolesMap.get(PROJECT3.getId()), ImmutableList.of(1003L, 1002L));
                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(userCtx);
        assertEquals(VELOCITY_RETURN, html);
    }

    @Test
    public void testGetShareTypeEditorAnonymous()
    {
        doReturn(ImmutableList.of(PROJECT1.getGenericValue(), PROJECT2.getGenericValue(), PROJECT3.getGenericValue()))
                .when(permMgr).getProjects(Permissions.BROWSE, ANONYMOUS);
        doReturn(Collections.emptyList()).when(projectRoleMgr).getProjectRoles(ANONYMOUS, PROJECT1);
        doReturn(ImmutableList.of(ROLE2, ROLE3)).when(projectRoleMgr).getProjectRoles(ANONYMOUS, PROJECT2);
        doReturn(ImmutableList.of(ROLE1, ROLE3, ROLE4)).when(projectRoleMgr).getProjectRoles(ANONYMOUS, PROJECT3);

        renderer = new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, projectFactory)
        {
            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertThat(params, hasEntry(PROJECTS_KEY, (Object)ImmutableList.of(PROJECT3, PROJECT1, PROJECT2)));
                assertThat(params, hasEntry(ROLES_KEY, (Object) ImmutableList.of(ROLE1, ROLE2, ROLE4, ROLE3)));
                assertThat(params, hasKey(ROLES_MAP_KEY));

                final Map<?,?> rolesMap = (Map<?,?>) params.get(ROLES_MAP_KEY);
                assertJsonEquals((String) rolesMap.get(PROJECT1.getId()), Collections.emptyList());
                assertJsonEquals((String) rolesMap.get(PROJECT2.getId()), ImmutableList.of(1001L, 1002L));
                assertJsonEquals((String) rolesMap.get(PROJECT3.getId()), ImmutableList.of(1000L, 1003L, 1002L));
                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(anonymousCtx);
        assertEquals(VELOCITY_RETURN, html);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingAShareTypeEditorShouldFailGivenANullAuthenticationContext()
    {
        renderer.getShareTypeEditor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedTemplatesForAnEntityAndARenderModeShouldFailGivenANullAuthenticationContext()
    {
        renderer.getTranslatedTemplates(null, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRetrieveTheTranslatedTemplatesForANullEntityType()
    {
        renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedTemplatesForAnEntityTypeShouldFailGivenANullRenderMode()
    {
        renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, null);
    }

    @Test
    public void testGetTranslatedTemplates()
    {
        Map<String,String> actualTemplates = renderer.getTranslatedTemplates(userCtx, SearchRequest.ENTITY_TYPE, RenderMode.EDIT);
        assertTemplates(actualTemplates, "share_invalid_project", "share_invalid_role", "share_project_display_all",
                "share_project_display", "share_project_description", "share_role_description");

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
        assertTemplates(actualTemplates, "share_invalid_project", "share_invalid_role", "share_project_display_all",
                "share_project_display", "share_project_description", "share_role_description");

        actualTemplates = renderer.getTranslatedTemplates(userCtx, SearchRequest.ENTITY_TYPE, RenderMode.SEARCH);
        assertTemplates(actualTemplates, "share_invalid_project", "share_invalid_role", "share_project_description",
                "share_role_description");

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.SEARCH);
        assertTemplates(actualTemplates, "share_invalid_project", "share_invalid_role", "share_project_description",
                "share_role_description");
    }

    private void assertTemplates(final Map<String,String> actualTemplates, String... expectedKeys)
    {
        assertNotNull("Template map was null", actualTemplates);
        assertThat(actualTemplates.keySet(), containsInAnyOrder(expectedKeys));
        for (Map.Entry<String,String> entry : actualTemplates.entrySet())
        {
            assertTrue("Template for key '" + entry.getKey() + "' is blank.", isNotBlank(entry.getValue()));
        }
    }

    private void assertJsonEquals(final String array, final Collection<?> expectedItems)
    {
        try
        {
            final JSONArray jsonArray = new JSONArray(array);
            final List<Long> jsonItems = newArrayListWithCapacity(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                jsonItems.add(jsonArray.getLong(i));
            }
            assertEquals(expectedItems, jsonItems);
        }
        catch (final JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static JiraAuthenticationContext createAuthenticationContext(final User user)
    {
        return new MockAuthenticationContext(user);
    }

    static Matcher<String> blank()
    {
        return Blank.INSTANCE;
    }

    static class Blank extends BaseMatcher<String>
    {
        static Blank INSTANCE = new Blank();

        @Override
        public boolean matches(final Object item)
        {
            return item == null || (item instanceof String && isBlank((String)item));
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("blank string");
        }
    }

    static class MockProjectFactory implements ProjectFactory
    {
        public Project getProject(final GenericValue projectGV)
        {
            return new MockProject(projectGV);
        }

        @Nonnull
        public List<Project> getProjects(@Nonnull final Collection<GenericValue> projectGVs)
        {
            final List<Project> projects = newArrayListWithCapacity(projectGVs.size());
            for (GenericValue projectGV : projectGVs)
            {
                projects.add(getProject(projectGV));
            }
            return projects;
        }
    }
}
