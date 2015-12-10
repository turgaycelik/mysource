package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.EntityProperty;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;

import static com.atlassian.jira.testkit.client.restclient.EntityPropertyKeys.EntityPropertyKey;
import static com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util.PropertyAssertions.assertUniformInterfaceException;
import static com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util.PropertyAssertions.propertyKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ENTITY_PROPERTIES, Category.REST })
public class TestIssuePropertyResource extends FuncTestCase
{
    public static final String PROPERTY_KEY = "issue.status";
    public static final String PROPERTY_KEY2 = "issue.label";
    private EntityPropertyClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        this.client = new EntityPropertyClient(environmentData, "issue");
    }

    public void testCreatingNewIssueProperty() throws JSONException
    {
        administration.restoreBlankInstance();
        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "First issue with properties");

        assertThat(client.getKeys(issue.key).keys, Matchers.<EntityPropertyKey>empty());

        JSONObject jsonObject = new JSONObject(ImmutableMap.<String, Object>of("status", "done", "resolution", "won't fix"));
        client.put(issue.key, PROPERTY_KEY, jsonObject);
        EntityProperty issueProperty = client.get(issue.key, PROPERTY_KEY);

        assertThat(issueProperty.key, is(PROPERTY_KEY));
        assertThat(jsonObject, is(new JSONObject(issueProperty.value)));

        // test that id is also valid input
        Iterable<EntityPropertyKey> keys = client.getKeys(issue.id).keys;

        assertThat(keys, Matchers.<EntityPropertyKey>hasItem(propertyKey(PROPERTY_KEY)));

        // anonymous user are not allowed to get the property
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.anonymous().getKeys(issue.id);
                return null;
            }
        }, Response.Status.UNAUTHORIZED);
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.anonymous().get(issue.key, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.UNAUTHORIZED);
    }

    public void testDeletingProperties() throws JSONException
    {
        administration.restoreBlankInstance();
        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with properties");

        assertThat(client.getKeys(issue.key).keys, Matchers.<EntityPropertyKey>empty());
        JSONObject jsonObject = new JSONObject(ImmutableMap.of("label", "todo", "color", "red"));
        client.put(issue.key, PROPERTY_KEY2, jsonObject);

        EntityProperty issueProperty = client.get(issue.key, PROPERTY_KEY2);
        assertThat(issueProperty.key, is(PROPERTY_KEY2));
        assertThat(jsonObject, is(new JSONObject(issueProperty.value)));

        client.delete(issue.key, PROPERTY_KEY2);
        assertThat(client.getKeys(issue.key).keys, Matchers.<EntityPropertyKey>empty());

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.get(issue.key, PROPERTY_KEY2);
                return null;
            }
        }, Response.Status.NOT_FOUND);
    }

    public void testForbiddenForUserWithoutPermissionsToProject()
    {
        backdoor.restoreBlankInstance();
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);

        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with properties");
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.loginAs(BOB_USERNAME, BOB_PASSWORD).put(issue.id, PROPERTY_KEY, new JSONObject(ImmutableMap.of("status", "done")));
                return null;
            }
        }, Response.Status.FORBIDDEN);
    }

    public void testPropertiesAreRemovedWhenIssueIsRemoved() throws JSONException
    {
        backdoor.restoreBlankInstance();

        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with properties");

        final JSONObject jsonObject = new JSONObject(ImmutableMap.of("status", "unresolved"));
        client.put(issue.key, PROPERTY_KEY, jsonObject);
        EntityProperty property = client.get(issue.key, PROPERTY_KEY);
        assertThat(new JSONObject(property.value), is(jsonObject));

        backdoor.issues().deleteIssue(issue.key, true);

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.get(issue.key, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.NOT_FOUND);
    }
}
