package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.EntityProperty;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyKeys;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util.PropertyAssertions;

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
public class TestProjectPropertyResource extends FuncTestCase
{
    private static final String PROJECT_KEY = "HSP";
    private static final String PROPERTY_KEY = "project.admin";
    private EntityPropertyClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        this.client = new EntityPropertyClient(environmentData, "project");
    }

    public void testCreatingNewProjectProperty()
    {
        administration.restoreBlankInstance();

        assertThat(client.getKeys(PROJECT_KEY).keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>empty());

        JSONObject jsonObject = new JSONObject(ImmutableMap.of("username", "filip", "userkey", "filip2"));
        client.put(PROJECT_KEY, PROPERTY_KEY, jsonObject);
        EntityProperty projectProperty = client.get(PROJECT_KEY, PROPERTY_KEY);

        assertThat(projectProperty.key, is(PROPERTY_KEY));
        assertThat(jsonObject, is(new JSONObject(projectProperty.value)));

        // test that id is also valid input
        Iterable<EntityPropertyKeys.EntityPropertyKey> keys = client.getKeys(PROJECT_KEY).keys;

        assertThat(keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>hasItem(propertyKey(PROPERTY_KEY)));

        // anonymous user are not allowed to get the property
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.anonymous().getKeys(PROJECT_KEY);
                return null;
            }
        }, Response.Status.UNAUTHORIZED);
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.anonymous().get(PROJECT_KEY, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.UNAUTHORIZED);
    }

    public void testDeletingProperties()
    {
        administration.restoreBlankInstance();

        assertThat(client.getKeys(PROJECT_KEY).keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>empty());

        JSONObject jsonObject = new JSONObject(ImmutableMap.of("label", "todo", "color", "red"));
        client.put(PROJECT_KEY, PROPERTY_KEY, jsonObject);

        EntityProperty projectProperty = client.get(PROJECT_KEY, PROPERTY_KEY);
        assertThat(projectProperty.key, is(PROPERTY_KEY));
        assertThat(jsonObject, is(new JSONObject(projectProperty.value)));

        client.delete(PROJECT_KEY, PROPERTY_KEY);

        assertThat(client.getKeys(PROJECT_KEY).keys, Matchers.<EntityPropertyKeys.EntityPropertyKey>empty());

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.get(PROJECT_KEY, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.NOT_FOUND);
    }

    public void testForbiddenForUserWithoutPermissionsToProject()
    {
        backdoor.restoreBlankInstance();
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);

        PropertyAssertions.assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.loginAs(BOB_USERNAME, BOB_PASSWORD).put(PROJECT_KEY, PROPERTY_KEY, new JSONObject(ImmutableMap.of("status", "done")));
                return null;
            }
        }, Response.Status.FORBIDDEN);
    }

    public void testPropertiesAreRemovedWhenProjectIsRemoved() throws JSONException
    {
        backdoor.restoreBlankInstance();

        final JSONObject jsonObject = new JSONObject(ImmutableMap.of("status", "unresolved"));
        client.put(PROJECT_KEY, PROPERTY_KEY, jsonObject);
        EntityProperty property = client.get(PROJECT_KEY, PROPERTY_KEY);
        assertThat(new JSONObject(property.value), is(jsonObject));

        backdoor.project().deleteProject(PROJECT_KEY);

        PropertyAssertions.assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                client.get(PROJECT_KEY, PROPERTY_KEY);
                return null;
            }
        }, Response.Status.NOT_FOUND);
    }
}
