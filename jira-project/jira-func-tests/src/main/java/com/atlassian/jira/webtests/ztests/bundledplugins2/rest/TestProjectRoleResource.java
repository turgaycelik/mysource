package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Errors;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestProjectRoleResource extends RestFuncTest
{
    private ProjectRoleClient projectRoleClient;

    public void testViewProjectRoles() throws Exception
    {
        viewProjectRoles("10001");
        viewProjectRoles("MKY");
    }

    private void viewProjectRoles(final String projectIdOrKey) throws JSONException
    {
        final Map<String, String> mky = projectRoleClient.get(projectIdOrKey);
        assertEquals(3, mky.size());

        // We want some rudimentary testing that the URLs actually go someplace.
        List<String> roles = asList("Users", "Administrators", "Developers");
        for (String role : roles)
        {
            final JSONObject jsonRole = getJSON(mky.get(role));
            assertEquals(role, jsonRole.get("name"));
        }
    }

    public void testViewRole() throws Exception
    {
        viewRole("10001");
        viewRole("MKY");
    }

    private void viewRole(final String projectIdOrKey)
    {
        final ProjectRole projectRole = projectRoleClient.get(projectIdOrKey, "Users");
        assertEquals("Users", projectRole.name);
        assertEquals("A project role that represents users in a project", projectRole.description);
        assertNotNull(projectRole.self);
        assertNotNull(projectRole.id);

        assertEquals(2, projectRole.actors.size());

        // We turn this into a map so that we can always find the one we care about to perform assertions on
        // without having to worry about order stability making this an intermittently failing test
        final Map<String, ProjectRole.Actor> map = makeMap(projectRole.actors);
        ProjectRole.Actor actor = map.get("admin");

        assertEquals("Administrator", actor.displayName);
        assertEquals("atlassian-user-role-actor", actor.type);
        assertEquals("admin", actor.name);
    }

    public void testViewRoleActorsIsSortedByDisplayName() throws Exception
    {
        ProjectRole projectRole = projectRoleClient.get("MKY", "Users");

        assertEquals(2, projectRole.actors.size());

        final ProjectRole.Actor admin = projectRole.actors.get(0);

        assertEquals("Administrator", admin.displayName);
        assertEquals("atlassian-user-role-actor", admin.type);
        assertEquals("admin", admin.name);

        final ProjectRole.Actor actor = projectRole.actors.get(1);

        assertEquals("jira-users", actor.displayName);
        assertEquals("atlassian-group-role-actor", actor.type);
        assertEquals("jira-users", actor.name);

        backdoor.usersAndGroups().addUser("aaaa", "aaaa", "zzzz", "aaa@aaa.com");
        backdoor.usersAndGroups().addUser("zzzz", "zzzz", "aaaa", "zzz@zzz.com");

        projectRoleClient.addActors("MKY", "Users", new String[] { }, new String[] { "aaaa", "zzzz" });

        projectRole = projectRoleClient.get("MKY", "Users");

        assertEquals(4, projectRole.actors.size());

        assertEquals("aaaa", projectRole.actors.get(0).displayName);
        assertEquals("Administrator", projectRole.actors.get(1).displayName);
        assertEquals("jira-users", projectRole.actors.get(2).displayName);
        assertEquals("zzzz", projectRole.actors.get(3).displayName);

    }

    public void testSetRoleActors() throws Exception
    {
        ProjectRole projectRole = projectRoleClient.get("MKY", "Users");

        List<ProjectRole.Actor> actors = projectRole.actors;
        assertEquals(2, actors.size());

        final ProjectRole.Actor admin = actors.get(0);

        assertEquals("Administrator", admin.displayName);
        assertEquals("atlassian-user-role-actor", admin.type);
        assertEquals("admin", admin.name);

        final ProjectRole.Actor actor = actors.get(1);

        assertEquals("jira-users", actor.displayName);
        assertEquals("atlassian-group-role-actor", actor.type);
        assertEquals("jira-users", actor.name);

        backdoor.usersAndGroups().addUser("aaaa", "aaaa", "zzzz", "aaa@aaa.com");
        backdoor.usersAndGroups().addUser("zzzz", "zzzz", "aaaa", "zzz@zzz.com");
        backdoor.usersAndGroups().addGroup("ladida");

        projectRoleClient.setActors("MKY", "Users", ImmutableMap.<String, String[]>builder()
                .put("atlassian-user-role-actor", new String[] { "aaaa", "zzzz" })
                .put("atlassian-group-role-actor", new String[] { "ladida" })
                .build());

        projectRole = projectRoleClient.get("MKY", "Users");
        actors = projectRole.actors;

        assertEquals(3, actors.size());

        assertEquals("aaaa", actors.get(0).displayName);
        assertEquals("atlassian-user-role-actor", actors.get(0).type);
        assertEquals("zzzz", actors.get(0).name);

        assertEquals("ladida", actors.get(1).displayName);
        assertEquals("atlassian-group-role-actor", actors.get(1).type);
        assertEquals("ladida", actors.get(1).name);

        assertEquals("zzzz", actors.get(2).displayName);
        assertEquals("atlassian-user-role-actor", actors.get(2).type);
        assertEquals("aaaa", actors.get(2).name);

        final Response response = projectRoleClient.setActors("MKY", "Users", ImmutableMap.<String, String[]>builder()
                .put("atlassian-user-role-actor", new String[] { "aaaa"})
                .put("atlassian-group-role-actor", new String[] { "azza" })
                .build() );

        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("The actor: 'azza' could not be found in any user directory. Therefore, it is not possible to add it to the project role: '10000'"), response.entity);
    }


    Map<String, ProjectRole.Actor> makeMap(final Collection<ProjectRole.Actor> actors)
    {
        final Map<String, ProjectRole.Actor> map = new HashMap<String, ProjectRole.Actor>();
        for (ProjectRole.Actor actor : actors)
        {
            assertFalse(map.containsKey(actor.name));
            map.put(actor.name, actor);
        }
        return map;
    }

    public void testDeleteRoleActor() throws Exception
    {
        final ProjectRole projectRole = projectRoleClient.get("MKY", "Users");
        assertEquals(2, projectRole.actors.size());

        projectRoleClient.deleteGroup("MKY", "Users", "jira-users");
        assertEquals(1, projectRoleClient.get("MKY", "Users").actors.size());

        projectRoleClient.deleteUser("MKY", "Users", "admin");
        assertEquals(0, projectRoleClient.get("MKY", "Users").actors.size());
    }

    public void testAddRoleActor() throws Exception
    {
        ProjectRole projectRole = projectRoleClient.get("MKY", "Developers");
        assertEquals(0, projectRole.actors.size());

        projectRoleClient.addActors("MKY", "Developers", new String[] { "jira-developers" }, null);

        projectRole = projectRoleClient.get("MKY", "Developers");
        assertEquals(1, projectRole.actors.size());
        final ProjectRole.Actor actor = projectRole.actors.get(0);
        assertEquals("jira-developers", actor.name);
        assertEquals("jira-developers", actor.displayName);
        assertEquals("atlassian-group-role-actor", actor.type);
    }

    public void testAddRoleActors() throws Exception
    {
        ProjectRole projectRole = projectRoleClient.get("MKY", "Developers");
        assertEquals(0, projectRole.actors.size());

        projectRoleClient.addActors("MKY", "Developers", new String[] { "jira-users", "jira-administrators", "jira-developers" },
                new String[]{"admin"});

        projectRole = projectRoleClient.get("MKY", "Developers");
        assertEquals(4, projectRole.actors.size());

        final ProjectRole.Actor admin = projectRole.actors.get(0);
        assertEquals("admin", admin.name);
        assertEquals("Administrator", admin.displayName);
        assertEquals("atlassian-user-role-actor", admin.type);

        final ProjectRole.Actor administrators = projectRole.actors.get(1);
        assertEquals("jira-administrators", administrators.name);
        assertEquals("jira-administrators", administrators.displayName);
        assertEquals("atlassian-group-role-actor", administrators.type);

        final ProjectRole.Actor developers = projectRole.actors.get(2);
        assertEquals("jira-developers", developers.name);
        assertEquals("jira-developers", developers.displayName);
        assertEquals("atlassian-group-role-actor", developers.type);

        final ProjectRole.Actor users = projectRole.actors.get(3);
        assertEquals("jira-users", users.name);
        assertEquals("jira-users", users.displayName);
        assertEquals("atlassian-group-role-actor", users.type);

    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        projectRoleClient = new ProjectRoleClient(getEnvironmentData());
        administration.restoreData("TestProjectRoleResource.xml");
    }

    JSONObject getJSON(final String uri) throws JSONException
    {
        getTester().getDialog().gotoPage(uri);
        return new JSONObject(getTester().getDialog().getResponseText());
    }
}