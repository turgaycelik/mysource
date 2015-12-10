package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.jira.testkit.client.restclient.EntityProperty;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyKeys;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.Groups;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;

import static com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util.PropertyAssertions.assertUniformInterfaceException;
import static com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util.PropertyAssertions.propertyKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ENTITY_PROPERTIES, Category.REST })
public class TestCommentPropertyResource extends FuncTestCase
{
    public static final String PROPERTY_KEY = "comment.meta";
    public static final String PROJECT_KEY = "HSP";
    private EntityPropertyClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        this.client = new EntityPropertyClient(environmentData, "comment");
    }

    public void testCreatingNewCommentProperty()
    {
        administration.restoreBlankInstance();
        setupPermissionsToEditComments();
        final IssueCreateResponse issue = backdoor.issues().createIssue(PROJECT_KEY, "Issue with comment");
        final Comment comment = backdoor.issues().commentIssue(issue.key, "comment with property").body;

        assertThat(client.getKeys(comment.id).keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>empty());

        JSONObject jsonObject = new JSONObject(ImmutableMap.<String, Object>of("visible", 0));

        client.put(comment.id, PROPERTY_KEY, jsonObject);
        EntityProperty entityProperty = client.get(comment.id, PROPERTY_KEY);

        assertThat(entityProperty.key, is(PROPERTY_KEY));
        assertThat(jsonObject, is(new JSONObject(entityProperty.value)));

        // test that id is also valid input
        Iterable<EntityPropertyKeys.EntityPropertyKey> keys = client.getKeys(comment.id).keys;
        assertThat(keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>hasItem(propertyKey(PROPERTY_KEY)));

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.anonymous().getKeys(comment.id);
                return null;
            }
        }, Response.Status.UNAUTHORIZED);
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.anonymous().get(comment.id, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.UNAUTHORIZED);
    }

    public void testDeletingProperties()
    {
        administration.restoreBlankInstance();
        setupPermissionsToEditComments();

        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with comment");
        final Comment comment = backdoor.issues().commentIssue(issue.key, "comment with property").body;

        assertThat(client.getKeys(comment.id).keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>empty());

        JSONObject jsonObject = new JSONObject(ImmutableMap.<String, Object>of("visible", 0));
        client.put(comment.id, PROPERTY_KEY, jsonObject);

        EntityProperty entityProperty = client.get(comment.id, PROPERTY_KEY);

        assertThat(entityProperty.key, is(PROPERTY_KEY));
        assertThat(jsonObject, is(new JSONObject(entityProperty.value)));

        client.delete(comment.id, PROPERTY_KEY);
        assertThat(client.getKeys(comment.id).keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>empty());

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.get(comment.id, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.NOT_FOUND);
    }

    public void testForbiddenForUserWithoutPermissionsToProject()
    {
        backdoor.restoreBlankInstance();
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);

        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with comment");
        final Comment comment = backdoor.issues().commentIssue(issue.key, "comment with property").body;

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.loginAs(BOB_USERNAME, BOB_PASSWORD).put(comment.id, PROPERTY_KEY, new JSONObject(ImmutableMap.<String, Object>of("visible", 0)));
                return null;
            }
        }, Response.Status.FORBIDDEN);
    }

    public void testCommentPropertyRemovedWhenCommentRemoved()
    {
        backdoor.restoreBlankInstance();
        setupPermissionsToEditComments();

        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);

        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with comment");
        final Comment comment = backdoor.issues().commentIssue(issue.key, "comment with property").body;

        JSONObject jsonObject = new JSONObject(ImmutableMap.<String, Object>of("visible", 0));
        client.put(comment.id, PROPERTY_KEY, jsonObject);

        EntityProperty entityProperty = client.get(comment.id, PROPERTY_KEY);

        assertThat(entityProperty.key, is(PROPERTY_KEY));
        assertThat(jsonObject, is(new JSONObject(entityProperty.value)));

        backdoor.issues().deleteIssue(issue.key, true);

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.get(comment.id, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.NOT_FOUND);
    }

    private void setupPermissionsToEditComments()
    {
        Long permSchemeId = backdoor.permissionSchemes().copyDefaultScheme("comment perm scheme");
        Long projectId = backdoor.project().getProjectId("HSP");
        backdoor.permissionSchemes().addGroupPermission(permSchemeId, Permissions.COMMENT_EDIT_ALL, Groups.ADMINISTRATORS);
        backdoor.project().setPermissionScheme(projectId, permSchemeId);
    }
}
