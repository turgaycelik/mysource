package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Errors;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.testkit.client.restclient.Avatar;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.IssueType;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Func test for ProjectResource.
 *
 * @since v4.2
 */
@WebTest ( { Category.FUNC_TEST, Category.REST })
public class TestProjectResource extends RestFuncTest
{
    private ProjectClient projectClient;

    public void testViewProject() throws Exception
    {
        //Check the HSP project.
        checkProject(createProjectMky(), true, false);
        checkProject(createProjectAtl(), true, true);
        checkProject(createProjectHid(), false, false);
        checkProject(createProjectFred(), true, false);
        checkProject(createProjectDodo(), false, false);
    }

    public void testViewProjectNoPermissionToViewByKey() throws Exception
    {
        assertCantSeeProject("fred", "HID");
        assertCantSeeProject(null, "MKY");
        assertCantSeeProject(null, "HID");
        assertCantSeeProject(null, "FRED");
    }

    public void testViewProjectNoPermissionToViewById() throws Exception
    {
        assertCantSeeProject("fred", 10110L);
        assertCantSeeProject(null, 10001L);
        assertCantSeeProject(null, 10110L);
        assertCantSeeProject(null, 10111L);
    }

    public void testViewProjects() throws Exception
    {
        Project projectAtl = makeSimple(createProjectAtl());
        Project projectFred = makeSimple(createProjectFred());
        Project projectHsp = makeSimple(createProjectHsp());
        Project projectHid = makeSimple(createProjectHid());
        Project projectMky = makeSimple(createProjectMky());
        Project projectDodo = makeSimple(createProjectDodo());

        //System admin should see all projects.
        assertEquals(list(projectAtl, projectDodo, projectFred, projectHid, projectHsp, projectMky), projectClient.getProjects());

        //Fred should see projects he can see or admin.
        assertEquals(list(projectAtl, projectFred, projectHsp, projectMky), projectClient.loginAs("fred").getProjects());

        //Anonymous should only see one project.
        assertEquals(list(projectAtl), projectClient.anonymous().getProjects());
    }

    public void testViewProjectByKeyDoesNotExist() throws Exception
    {
        checkProjectNotFound("key", "XXX");
    }

    public void testViewProjectByIdDoesNotExist() throws Exception
    {
        checkProjectNotFound("id", "20000");
    }

    private void checkProjectNotFound(final String field, final String projectIdOrKey)
    {
        Response respXXX = projectClient.getResponse(projectIdOrKey);
        assertEquals(404, respXXX.statusCode);
        assertEquals(1, respXXX.entity.errorMessages.size());
        assertThat(respXXX.entity.errorMessages, contains(String.format("No project could be found with %s '%s'.", field, projectIdOrKey)));
    }

    public void testViewProjectVersions() throws Exception
    {
        //Make sure no versions works.
        assertTrue(projectClient.getVersions("10001").isEmpty());
        assertTrue(projectClient.getVersions("MKY").isEmpty());

        //Make sure it works for a particular project.
        assertEquals(createVersionsAtl(), projectClient.getVersions("10010"));
        assertEquals(createVersionsAtl(), projectClient.getVersions("ATL"));
    }

    public void testViewProjectVersionsAnonymous() throws Exception
    {
        assertEquals(createVersionsAtl(), projectClient.anonymous().getVersions("10010"));
        assertEquals(createVersionsAtl(), projectClient.anonymous().getVersions("ATL"));

        checkProjectNotFoundForVersions("id", "10001");
        checkProjectNotFoundForVersions("key", "MKY");
    }

    private void checkProjectNotFoundForVersions(final String field, final String projectIdOrKey)
    {
        Response response = projectClient.getVersionsResponse(projectIdOrKey);
        assertEquals(404, response.statusCode);
        assertThat(response.entity.errorMessages, hasItem(String.format("No project could be found with %s '%s'.", field, projectIdOrKey)));
    }

    public void testViewProjectComponents() throws Exception
    {
        //Make sure no components works.
        assertTrue(projectClient.getComponents("10001").isEmpty());
        assertTrue(projectClient.getComponents("MKY").isEmpty());

        //Make sure it works for a particular project.
        assertEquals(createComponentsHsp(), projectClient.getComponents("10000"));
        assertEquals(createComponentsHsp(), projectClient.getComponents("HSP"));
    }

    public void testViewProjectComponentsAnonymous() throws Exception
    {
        assertEquals(createComponentsAtlFull(), projectClient.anonymous().getComponents("10010"));
        assertEquals(createComponentsAtlFull(), projectClient.anonymous().getComponents("ATL"));

        checkProjectNotFoundForComponents("id", "10001");
        checkProjectNotFoundForComponents("key", "MKY");
    }

    private void checkProjectNotFoundForComponents(final String field, final String projectIdOrKey)
    {
        try
        {
            projectClient.getComponents(projectIdOrKey);
            fail("Should throw exception.");
        }
        catch (UniformInterfaceException e)
        {
            final ClientResponse response = e.getResponse();
            assertEquals(404, response.getStatus());
            assertThat(response.getEntity(Errors.class).errorMessages, hasItem(String.format("No project could be found with %s '%s'.", field, projectIdOrKey)));
        }
    }

    public void testGetAvatars() throws Exception
    {
        checkGetAvatars("10000");
        checkGetAvatars("HSP");
    }

    private void checkGetAvatars(final String projectIdOrKey)
    {
        Map<String, List<Avatar>> avatars = projectClient.getAvatars(projectIdOrKey);

        List<Avatar> systemAvatars = avatars.get("system");
        List<Avatar> customAvatars = avatars.get("custom");

        assertThat(systemAvatars.size(), equalTo(16));
        assertThat(customAvatars.size(), equalTo(1));
    }

    public void testViewProjectWithEditedKey() throws Exception
    {
        Project expectedProject = createProjectMky().key("TST");
        backdoor.project().editProjectKey(Long.valueOf(expectedProject.id), "TST");
        assertThat(projectClient.loginAs("admin").get("TST"), equalTo(expectedProject));
        assertThat(projectClient.loginAs("admin").get("MKY"), equalTo(expectedProject));
    }

    private Project makeSimple(Project project)
    {
        return project.email(null).components(null).assigneeType(null).description(null).lead(null).versions(null).issueTypes(null).roles(null).expand(null);
    }

    private void assertCantSeeProject(String username, String key)
    {
        assertCantSeeProject(key, username, "key");
    }

    private void assertCantSeeProject(String username, Long id)
    {
        assertCantSeeProject(Long.toString(id), username, "id");
    }

    private void assertCantSeeProject(final String projectIdOrKey, final String username, final String field)
    {
        if (username == null)
        {
            projectClient.anonymous();
        }
        else
        {
            projectClient.loginAs(username);
        }

        Response response = projectClient.getResponse(projectIdOrKey);
        assertEquals(404, response.statusCode);
        assertThat(response.entity.errorMessages, hasItem(String.format("No project could be found with %s '%s'.", field, projectIdOrKey)));
    }

    private void checkProject(Project expectedProject, boolean fred, boolean anonymous)
    {
        checkProjectByKeyOrId(expectedProject.id, expectedProject, fred, anonymous);
        checkProjectByKeyOrId(expectedProject.key, expectedProject, fred, anonymous);
    }

    private void checkProjectByKeyOrId(final String projectKeyOrId, final Project expectedProject, final boolean fred, final boolean anonymous)
    {
        Project actualProject = projectClient.loginAs("admin").get(projectKeyOrId);
        assertThat(actualProject, equalTo(expectedProject));

        if (fred)
        {
            actualProject = projectClient.loginAs("fred").get(expectedProject.key);
            assertEquals(expectedProject, actualProject);
        }

        if (anonymous)
        {
            actualProject = projectClient.anonymous().get(expectedProject.key);
            assertEquals(expectedProject, actualProject);
        }
    }

    private List<IssueType> createStandardIssueTypes()
    {
        return CollectionBuilder.newBuilder(
                new IssueType().self(getRestApiUrl("issuetype/1")).id("1").name("Bug").iconUrl(getBaseUrlPlus("/images/icons/issuetypes/bug.png")).description("A problem which impairs or prevents the functions of the product."),
                new IssueType().self(getRestApiUrl("issuetype/2")).id("2").name("New Feature").iconUrl(getBaseUrlPlus("/images/icons/issuetypes/newfeature.png")).description("A new feature of the product, which has yet to be developed."),
                new IssueType().self(getRestApiUrl("issuetype/3")).id("3").name("Task").iconUrl(getBaseUrlPlus("/images/icons/issuetypes/task.png")).description("A task that needs to be done."),
                new IssueType().self(getRestApiUrl("issuetype/4")).id("4").name("Improvement").iconUrl(getBaseUrlPlus("/images/icons/issuetypes/improvement.png")).description("An improvement or enhancement to an existing feature or task.")
        ).asList();
    }

    private Map<String, String> createStandardRoles(String projectKey)
    {
        return MapBuilder.<String, String>newBuilder()
                .add("Users", getRestApiUri("project", projectKey, "role", "10000").toString())
                .add("Developers", getRestApiUri("project", projectKey, "role", "10001").toString())
                .add("Administrators", getRestApiUri("project", projectKey, "role", "10002").toString())
                .toMap();
    }

    private Project createProjectMky()
    {
        return new Project().self(getRestApiUri("project/10001")).key("MKY").name("monkey")
                .expand("projectKeys")
                .id("10001")
                .email("mky@example.com")
                .lead(createUserAdmin()).description("project for monkeys")
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("10001"))
                .components(Collections.<Component>emptyList()).versions(Collections.<Version>emptyList())
                .avatarUrls(createProjectAvatarUrls(10001L, 10011L));
    }

    private Project createProjectHid()
    {
        return new Project().self(getRestApiUri("project/10110")).key("HID").name("HIDDEN")
                .expand("projectKeys")
                .id("10110")
                .description("")
                .components(Collections.<Component>emptyList()).versions(Collections.<Version>emptyList())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("10110"))
                .issueTypes(createStandardIssueTypes())
                .lead(createUserAdmin())
                .avatarUrls(createProjectAvatarUrls(10110L, 10011L));
    }

    private Project createProjectHsp()
    {
        return new Project().self(getRestApiUri("project/10000")).key("HSP").name("homosapien")
                .expand("projectKeys")
                .id("10000")
                .description("project for homosapiens")
                .versions(createVersionsHsp()).components(createComponentsHsp())
                .issueTypes(createStandardIssueTypes())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("10000"))
                .lead(createUserAdmin())
                .avatarUrls(createProjectAvatarUrls(10000L, 10140L));
    }

    private Project createProjectFred()
    {
        return new Project().self(getRestApiUri("project/10111")).key("FRED").name("Fred")
                .expand("projectKeys")
                .id("10111")
                .description("")
                .components(Collections.<Component>emptyList())
                .versions(Collections.<Version>emptyList())
                .issueTypes(createStandardIssueTypes())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .roles(createStandardRoles("10111"))
                .lead(createUserFred())
                .avatarUrls(createProjectAvatarUrls(10111L, 10011L));
    }

    private Project createProjectDodo()
    {
        return new Project().self(getRestApiUri("project/10112")).key("DD").name("Dead Leader")
                .expand("projectKeys")
                .id("10112")
                .description("")
                .components(Collections.<Component>emptyList())
                .versions(Collections.<Version>emptyList())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("10112"))
                .lead(createUserDodo())
                .avatarUrls(createProjectAvatarUrls(10112L, 10011L));
    }

    private Project createProjectAtl()
    {
        return new Project().self(getRestApiUri("project/10010")).key("ATL").name("Atlassian")
                .expand("projectKeys")
                .id("10010")
                .description("")
                .lead(createUserAdmin()).components(createComponentsAtlShort())
                .assigneeType(Project.AssigneeType.PROJECT_LEAD)
                .issueTypes(createStandardIssueTypes())
                .roles(createStandardRoles("10010"))
                .versions(createVersionsAtl())
                .avatarUrls(createProjectAvatarUrls(10010L, 10011L));
    }

    private Map<String, String> createProjectAvatarUrls(final Long projectId, final Long avatarId)
    {
        return ImmutableMap.<String, String>builder()
            .put("24x24", getBaseUrlPlus("secure/projectavatar?size=small&pid="+projectId+"&avatarId="+avatarId))
            .put("16x16", getBaseUrlPlus("secure/projectavatar?size=xsmall&pid="+projectId+"&avatarId="+avatarId))
            .put("32x32", getBaseUrlPlus("secure/projectavatar?size=medium&pid="+projectId+"&avatarId="+avatarId))
            .put("48x48", getBaseUrlPlus("secure/projectavatar?pid="+projectId+"&avatarId="+avatarId))
// TODO JRADEV-20790 - Re-enable the larger avatar sizes.
//            .put("64x64", getBaseUrlPlus("secure/projectavatar?size=xlarge&pid="+projectId+"&avatarId="+avatarId))
//            .put("96x96", getBaseUrlPlus("secure/projectavatar?size=xxlarge&pid="+projectId+"&avatarId="+avatarId))
//            .put("128x128", getBaseUrlPlus("secure/projectavatar?size=xxxlarge&pid=" + projectId + "&avatarId=" + avatarId))
//            .put("192x192", getBaseUrlPlus("secure/projectavatar?size=xxlarge%402x&pid=" + projectId + "&avatarId=" + avatarId)) // %40 == "@"
//            .put("256x256", getBaseUrlPlus("secure/projectavatar?size=xxxlarge%402x&pid=" + projectId + "&avatarId=" + avatarId))
            .build();
    }

    private Map<String, String> createUserAvatarUrls(Long avatarId)
    {
        return ImmutableMap.<String,String>builder()
            .put("24x24", getBaseUrlPlus("secure/useravatar?size=small&avatarId="+avatarId))
            .put("16x16", getBaseUrlPlus("secure/useravatar?size=xsmall&avatarId="+avatarId))
            .put("32x32", getBaseUrlPlus("secure/useravatar?size=medium&avatarId="+avatarId))
            .put("48x48", getBaseUrlPlus("secure/useravatar?avatarId="+avatarId))
// TODO JRADEV-20790 - Re-enable the larger avatar sizes.
//            .put("64x64", getBaseUrlPlus("secure/useravatar?size=xlarge&avatarId="+avatarId))
//            .put("96x96", getBaseUrlPlus("secure/useravatar?size=xxlarge&avatarId="+avatarId))
//            .put("128x128", getBaseUrlPlus("secure/useravatar?size=xxxlarge&avatarId="+avatarId))
//            .put("192x192", getBaseUrlPlus("secure/useravatar?size=xxlarge%402x&avatarId="+avatarId)) // %40 == "@"
//            .put("256x256", getBaseUrlPlus("secure/useravatar?size=xxxlarge%402x&avatarId="+avatarId))
            .build();
    }

    private List<Version> createVersionsAtl()
    {
        CollectionBuilder<Version> builder = CollectionBuilder.newBuilder();

        builder.add(new Version().self(createVersionUri(10014)).archived(true)
                .released(false).name("Five").description("Five").id(10014L).projectId(10010l));

        builder.add(new Version().self(createVersionUri(10013)).archived(true)
                .released(true).name("Four").description("Four")
                .releaseDate("09/Mar/11").id(10013L).projectId(10010l));

        builder.add(new Version().self(createVersionUri(10012)).archived(false)
                .released(true).name("Three")
                .releaseDate("09/Mar/11").id(10012L).projectId(10010l));

        builder.add(new Version().self(createVersionUri(10011)).archived(false)
                .released(false).name("Two").description("Description").id(10011L).projectId(10010l));

        builder.add(new Version().self(createVersionUri(10010)).archived(false)
                .released(false).name("One").releaseDate("01/Mar/11").overdue(true).id(10010L).projectId(10010l));

        return builder.asList();
    }

    private List<Version> createVersionsHsp()
    {
        CollectionBuilder<Version> builder = CollectionBuilder.newBuilder();

        builder.add(new Version().self(createVersionUri(10000)).archived(false)
                .released(false).name("New Version 1").description("Test Version Description 1").id(10000L));

        builder.add(new Version().self(createVersionUri(10001)).archived(false)
                .released(false).name("New Version 4").description("Test Version Description 4").id(10001L));

        builder.add(new Version().self(createVersionUri(10002)).archived(false)
                .released(false).name("New Version 5").description("Test Version Description 5").id(10002L));

        return builder.asList();
    }

    private List<Component> createComponentsHsp()
    {
        CollectionBuilder<Component> builder = CollectionBuilder.newBuilder();

        builder.add(new Component().self(createComponentUri(10000)).id(10000L).name("New Component 1").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));
        builder.add(new Component().self(createComponentUri(10001)).id(10001L).name("New Component 2").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));
        builder.add(new Component().self(createComponentUri(10002)).id(10002L).name("New Component 3").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));

        return builder.asList();
    }

    private List<Component> createComponentsAtlFull()
    {
        CollectionBuilder<Component> builder = CollectionBuilder.newBuilder();

        builder.add(new Component().self(createComponentUri(10003)).id(10003L).name("New Component 4").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));
        builder.add(new Component().self(createComponentUri(10004)).id(10004L).name("New Component 5").assigneeType(Component.AssigneeType.PROJECT_DEFAULT)
                .assignee(createUserAdmin()).realAssigneeType(Component.AssigneeType.PROJECT_DEFAULT).realAssignee(createUserAdmin()).isAssigneeTypeValid(true));

        return builder.asList();
    }

    private List<Component> createComponentsAtlShort()
    {
        CollectionBuilder<Component> builder = CollectionBuilder.newBuilder();

        builder.add(new Component().self(createComponentUri(10003)).id(10003L).name("New Component 4"));
        builder.add(new Component().self(createComponentUri(10004)).id(10004L).name("New Component 5"));

        return builder.asList();
    }

    private User createUserAdmin()
    {

        return new User()
                .self(createUserUri("admin"))
                .name("admin")
                .key("admin")
                .displayName("Administrator")
                .active(true)
                .avatarUrls(createUserAvatarUrls(10062L));
    }

    private User createUserFred()
    {
        return new User()
                .self(createUserUri("fred"))
                .name("fred")
                .key("fred")
                .displayName("Fred Normal")
                .active(true)
                .avatarUrls(createUserAvatarUrls(10062L));
    }

    private User createUserDodo()
    {
        return new User()
                .self(createUserUri("dodo"))
                .name("dodo")
                .key("dodo")
                .displayName("dodo")
                .active(false)
                .avatarUrls(createUserAvatarUrls(10063L));
    }

    private URI createVersionUri(long id)
    {
        return getRestApiUri("version", String.valueOf(id));
    }

    private URI createComponentUri(long id)
    {
        return getRestApiUri("component", String.valueOf(id));
    }

    private URI createUserUri(String name)
    {
        return getRestApiUri(String.format("user?username=%s", name));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        projectClient = new ProjectClient(getEnvironmentData());
        administration.restoreData("TestProjectResource.xml");
    }

    @Override
    protected void tearDownTest()
    {
        super.tearDownTest();
        projectClient = null;
        administration = null;
    }

    private static <T, S extends T> List<T> list(S... element)
    {
        return Lists.<T>newArrayList(element);
    }
}
