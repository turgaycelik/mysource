package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.backdoor.ColumnControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Filter;
import com.atlassian.jira.testkit.client.restclient.FilterClient;
import com.atlassian.jira.testkit.client.restclient.Group;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.webtests.Permissions;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.hasItems;
import static com.atlassian.jira.testkit.client.restclient.Filter.FilterPermission;
import static com.atlassian.jira.testkit.client.restclient.matcher.ContainsStringThatStartsWith.containsStringThatStartsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestFilterResource extends RestFuncTest
{

    private FilterClient filterClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        filterClient = new FilterClient(getEnvironmentData());

        administration.restoreData("TestFilterResource.xml");
    }

    public void testAnonymous()
    {
        filterClient.anonymous();

        Response response = filterClient.getResponse("10000");
        assertEquals(400, response.statusCode);
        assertEquals("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.", response.entity.errorMessages.get(0));

        Filter json = filterClient.get("10002");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10002", json.self);
        assertEquals("10002", json.id);
        assertEquals("Public filter", json.name);
        assertEquals("Everyone can see this!", json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);

        // Anonymous should get sanitised JQL.
        assertEquals("project = 10000 AND type = bug", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10002"), json.viewUrl);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/search?jql=project+%3D+10000+AND+type+%3D+bug", json.searchUrl);
        assertFalse(json.favourite);
    }

    public void testFilterJson() throws Exception
    {
        //here's a non favourite filter I created
        Filter json = filterClient.get("10000");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10000", json.self);
        assertEquals("10000", json.id);
        assertEquals("My new awesome filter!", json.name);
        assertEquals("And here's a description", json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        assertEquals("type = Bug", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10000"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql=type+%3D+Bug"), json.searchUrl);
        assertFalse(json.favourite);

        //here's a favourite filter I created
        json = filterClient.get("10001");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10001", json.self);
        assertEquals("10001", json.id);
        assertEquals("All Issues", json.name);
        assertEquals(null, json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        assertEquals("", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10001"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql="), json.searchUrl);
        assertTrue(json.favourite);

        filterClient.loginAs("bob");
        //Shouldn't be able to get a filter that's not shared with me!
        Response response = filterClient.getResponse("10000");
        assertEquals(400, response.statusCode);
        assertEquals("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.", response.entity.errorMessages.get(0));

        //lets get a public filter created by someone else!
        json = filterClient.get("10002");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10002", json.self);
        assertEquals("10002", json.id);
        assertEquals("Public filter", json.name);
        assertEquals("Everyone can see this!", json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        assertEquals("project = homosapien AND type = bug", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10002"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql=project+%3D+homosapien+AND+type+%3D+bug"), json.searchUrl);
        assertTrue(json.favourite);

        //And finally get a filter Bob created
        json = filterClient.get("10003");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10003", json.self);
        assertEquals("10003", json.id);
        assertEquals("This is bob's filter!", json.name);
        assertEquals(null, json.description);
        assertEquals("bob", json.owner.name);
        assertEquals("Bob Brown", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=bob", json.owner.self);
        assertEquals("text ~ \"some fancy text\"", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10003"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql=text+~+%22some+fancy+text%22"), json.searchUrl);
    }

    public void testGetFavouriteFilters() throws Exception
    {
        filterClient.anonymous();
        List<Filter> filters = filterClient.getFavouriteFilters();
        assertTrue(filters.isEmpty());

        filterClient.loginAs("admin");
        List<Filter> favouriteFilters = filterClient.getFavouriteFilters();
        assertEquals(3, favouriteFilters.size());
        assertEquals("10001", favouriteFilters.get(0).id);
        assertEquals("10100", favouriteFilters.get(1).id);
        assertEquals("10002", favouriteFilters.get(2).id);
    }

    public void testCreateFilter() throws Exception
    {
        //testing a basic filter
        Filter filter1 = new Filter();
        filter1.name = "test";
        filter1.jql = "project=homosapien";
        filter1.description = "test description";
        filter1.favourite = true;
        Response<Filter> response = filterClient.postFilterResponse(filter1);

        assertEquals("test", response.body.name);
        assertEquals("project = homosapien", response.body.jql);
        assertEquals("test description", response.body.description);
        assertEquals(true, response.body.favourite);

        //test a false filter and status code 200
        filter1.name = "test2";
        filter1.favourite = false;
        response = filterClient.postFilterResponse(filter1);
        assertEquals(200, response.statusCode);
        assertEquals(false, response.body.favourite);

        //test errors
        //bad jql
        filter1.name = "test3";
        filter1.jql = "a=";
        filter1.description = "test description";
        filter1.favourite = true;
        response = filterClient.postFilterResponse(filter1);
        assertEquals(400, response.statusCode);
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("Error in JQL Query:"));

        //test bad jql 2
        filter1.name = "test3.1";
        filter1.jql = "1234";
        filter1.description = "test description";
        filter1.favourite = true;
        response = filterClient.postFilterResponse(filter1);
        assertEquals(400, response.statusCode);
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("Error in the JQL Query:"));


        //empty jql
        filter1.name = "test4";
        filter1.jql = "";
        response = filterClient.postFilterResponse(filter1);
        assertEquals(200, response.statusCode);
        assertEquals("", response.body.jql);

        //empty filter name
        filter1.jql = "project=homosapien";
        filter1.name = "";
        response = filterClient.postFilterResponse(filter1);
        assertEquals(400, response.statusCode);
        assertEquals("You must specify a name to save this filter as.", response.entity.errors.get("filterName"));

        //test two errors at once
        filter1.jql = "project=";
        filter1.name = "";
        response = filterClient.postFilterResponse(filter1);
        assertEquals(400, response.statusCode);
        assertEquals(2, response.entity.errorMessages.size() + response.entity.errors.size());
        assertEquals("You must specify a name to save this filter as.", response.entity.errors.get("filterName"));
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("Error in JQL Query:"));

        //filter name too long
        filter1.jql = "project=homosapien";
        filter1.name = "ehdWvkq23yifp2xxXUd1xJcaUM9U2i4U7SUTlKQE1vHw3J8VY03sfkKjvNje6pqqHH69HlUwpO3gbJh5uujOZP00O"
                + "HH06YxhX8NcTvpjWYzE2qQDa9Ji6MhoZ0kjYA1GmEd7MMAa88s9PJX01Hxtmfg5WiYeDFviROgNX1xy2WeeywetM4jKNuwJV"
                + "3AFJnzxMcT0wjFi1xWknNicdq9G9qp7SUTm6b6MfOS5XU63MKWmI6RisTIOgAjnFeqEjp4SnzdB";
        response = filterClient.postFilterResponse(filter1);
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size() + response.entity.errors.size());
        assertEquals("The entered filter name is too long, it must be less than 255 chars.", response.entity.errors.get("filterName"));

        //filter already saved with that name
        filter1.jql = "project=homosapien";
        filter1.name = "test";
        response = filterClient.postFilterResponse(filter1);
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size() + response.entity.errors.size());
        assertEquals("Filter with same name already exists.", response.entity.errors.get("filterName"));

        //check query validation is being applied
        filter1.name = "test5";
        filter1.jql = "a=b";
        filter1.description = "test description";
        filter1.favourite = true;
        response = filterClient.postFilterResponse(filter1);

        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size() + response.entity.errors.size());
        assertEquals("Field 'a' does not exist or you do not have permission to view it.", response.entity.errorMessages.get(0));


        //unauthorised and  empty filter name and broken jql (check ordering of concerns)
        filterClient.anonymous();
        response = filterClient.postFilterResponse(filter1);
        assertEquals(401, response.statusCode);
        assertEquals("No user currently logged in.", response.entity.errorMessages.get(0));


        //test user permissions are correct for filter
        filterClient.loginAs("admin");
        filter1 = new Filter();
        filter1.name = "test6";
        filter1.jql = "project=homosapien";
        filter1.description = "test description";
        filter1.favourite = true;
        String newFilterId = filterClient.postFilterResponse(filter1).body.id;

        //check that our filter is private
        filterClient.loginAs("fred");
        response = filterClient.getResponse(newFilterId);
        assertEquals(400, response.statusCode);
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("The selected filter is not available to you"));


    }

    public void testCopyFilter()
    {
        filterClient.loginAs("admin");
        Filter filter = new Filter();
        filter.id = "10100";
        filter.name = "blah clone";
        filter.description = "blah description";
        Response<Filter> response = filterClient.postFilterResponse(filter);
        assertEquals("", response.body.jql);
        assertEquals("blah clone", response.body.name);
        assertEquals("blah description", response.body.description);
        assertEquals("Expected the filters column config to also be copied",
                Lists.newArrayList("issuetype", "issuekey", "assignee", "reporter", "priority", "status", "resolution", "created", "updated", "duedate"),
                backdoor.filters().getColumnsForFilter(response.body.id));

        filterClient.loginAs("admin");
        filter = new Filter();
        filter.id = "10100";
        filter.name = "only clone columns";
        filter.jql = "project in (HSP, MKY)";
        response = filterClient.postFilterResponse(filter);
        assertEquals("only clone columns", response.body.name);
        assertEquals("project in (HSP, MKY)", response.body.jql);
        assertEquals("Expected the filters column config to also be copied",
                Lists.newArrayList("issuetype", "issuekey", "assignee", "reporter", "priority", "status", "resolution", "created", "updated", "duedate"),
                backdoor.filters().getColumnsForFilter(response.body.id));

        filter = new Filter();
        filter.id = "10002";
        filter.name = "public filter clone";
        response = filterClient.postFilterResponse(filter);
        assertEquals("project = homosapien AND type = bug", response.body.jql);
        assertTrue("Expected a different id as we cloned the filter", !response.body.id.equals(filter.id));
        assertEquals("public filter clone", response.body.name);
        String privateFilterId = response.body.id;
        assertEquals("Expected the user column config",
                Lists.newArrayList("issuetype", "issuekey", "summary", "assignee", "reporter", "priority", "status", "resolution", "created", "updated", "duedate"),
                backdoor.filters().getColumnsForFilter(privateFilterId));

        filter = new Filter();
        filter.id = "1001322";
        filter.name = "public filter clone";
        response = filterClient.postFilterResponse(filter);
        assertEquals("Invalid filter id should not throw server error", response.statusCode, 400);
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("The selected filter is not available to you"));

        filterClient.loginAs("fred");
        filter = new Filter();
        filter.id = privateFilterId;
        filter.name = "public filter clone";
        response = filterClient.postFilterResponse(filter);
        assertEquals("Private filter id should not throw server error", response.statusCode, 400);
        assertThat(response.entity.errorMessages, containsStringThatStartsWith("The selected filter is not available to you"));
    }

    public void testUpdateFilter()
    {
        // Test valid change
        String filterId = "10000";
        Filter filter = filterClient.get(filterId);

        filter.name = "New Filter Name";
        filter.description = "New Filter Description";
        filter.jql = "project = MKY";

        Response<Filter> putResponse = filterClient.putFilterResponse(filter);
        assertNotNull(putResponse);
        assertEquals(200, putResponse.statusCode);
        assertFilterEqual(filter, putResponse.body);

        // Updating the name of the filter only
        filter.name = "new name";
        filter.jql = null;
        filter.description = null;

        putResponse = filterClient.putFilterResponse(filter);
        assertNotNull(putResponse);
        assertEquals(200, putResponse.statusCode);

        filter.description = "New Filter Description";
        filter.jql = "project = MKY";
        assertFilterEqual(filter, putResponse.body);

        // Updating the description of the filter only

        filter.name = null;
        filter.jql = null;
        filter.description = "new description";

        putResponse = filterClient.putFilterResponse(filter);
        assertNotNull(putResponse);
        assertEquals(200, putResponse.statusCode);

        filter.jql = "project = MKY";
        filter.name = "new name";

        assertFilterEqual(filter, putResponse.body);

        // Updating the jql of the filter only
        filter.name = null;
        filter.jql = "project = HSP";
        filter.description = null;

        putResponse = filterClient.putFilterResponse(filter);
        assertNotNull(putResponse);
        assertEquals(200, putResponse.statusCode);

        filter.name = "new name";
        filter.description = "new description";

        assertFilterEqual(filter, putResponse.body);

        // Invalid JQL
        filter.name = "New Filter Name";
        filter.description = "New Filter Description";
        filter.jql = "invalid JQL";

        putResponse = filterClient.putFilterResponse(filter);
        assertNotNull(putResponse);
        assertEquals(400, putResponse.statusCode);
        assertThat(putResponse.entity.errorMessages, containsStringThatStartsWith("Error in the JQL Query: Expecting operator but got"));

        // Empty JQL
        filter.jql = "";
        putResponse = filterClient.putFilterResponse(filter);
        assertNotNull(putResponse);
        assertEquals(200, putResponse.statusCode);
        assertFilterEqual(filter, putResponse.body);

        // Empty filter name
        filter.name = "";
        putResponse = filterClient.putFilterResponse(filter);
        assertEquals(400, putResponse.statusCode);
        assertEquals("You must specify a name to save this filter as.", putResponse.entity.errors.get("filterName"));

        //filter name too long
        filter.jql = "project=homosapien";
        filter.name = "ehdWvkq23yifp2xxXUd1xJcaUM9U2i4U7SUTlKQE1vHw3J8VY03sfkKjvNje6pqqHH69HlUwpO3gbJh5uujOZP00O"
                + "HH06YxhX8NcTvpjWYzE2qQDa9Ji6MhoZ0kjYA1GmEd7MMAa88s9PJX01Hxtmfg5WiYeDFviROgNX1xy2WeeywetM4jKNuwJV"
                + "3AFJnzxMcT0wjFi1xWknNicdq9G9qp7SUTm6b6MfOS5XU63MKWmI6RisTIOgAjnFeqEjp4SnzdB";
        putResponse = filterClient.putFilterResponse(filter);
        assertEquals(400, putResponse.statusCode);
        assertEquals(1, putResponse.entity.errorMessages.size() + putResponse.entity.errors.size());
        assertEquals("The entered filter name is too long, it must be less than 255 chars.", putResponse.entity.errors.get("filterName"));

        //filter already saved with that name
        filter.jql = "project=homosapien";
        filter.name = "All Issues";
        putResponse = filterClient.putFilterResponse(filter);
        assertEquals(400, putResponse.statusCode);
        assertEquals(1, putResponse.entity.errorMessages.size() + putResponse.entity.errors.size());
        assertEquals("Filter with same name already exists.", putResponse.entity.errors.get("filterName"));

        //check query validation is being applied
        filter.name = "test5";
        filter.jql = "a=b";
        filter.description = "test description";
        filter.favourite = true;
        putResponse = filterClient.putFilterResponse(filter);

        assertEquals(400, putResponse.statusCode);
        assertEquals(1, putResponse.entity.errorMessages.size() + putResponse.entity.errors.size());
        assertEquals("Field 'a' does not exist or you do not have permission to view it.", putResponse.entity.errorMessages.get(0));

        //unauthorised and  empty filter name and broken jql (check ordering of concerns)
        filterClient.anonymous();
        filter.jql = "asd";
        filter.name = "";
        putResponse = filterClient.putFilterResponse(filter);
        assertEquals(401, putResponse.statusCode);
        assertEquals("No user currently logged in.", putResponse.entity.errorMessages.get(0));

        //test user permissions are correct for filter
        filterClient.loginAs("admin");
        filter.name = "test6";
        filter.jql = "project=homosapien";
        filter.description = "test description";
        filter.favourite = true;
        String newFilterId = filterClient.putFilterResponse(filter).body.id;

        //check that our filter is private
        filterClient.loginAs("fred");
        putResponse = filterClient.getResponse(newFilterId);
        assertEquals(400, putResponse.statusCode);
        assertThat(putResponse.entity.errorMessages, containsStringThatStartsWith("The selected filter is not available to you"));

        // Authorised PUTting unknown filter with bad jql and no name
        filterClient.loginAs("admin");
        filter.id = "bad";
        filter.jql = "asd";
        filter.name = "";
        putResponse = filterClient.putFilterResponse(filter);
        assertEquals(404, putResponse.statusCode);
    }

    public void testSharePermissions()
    {
        // global
        runSinglePermission(
                backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "globes", "", "[{\"type\":\"global\"}]"),
                new FilterPermission().type("global"));

        // project
        runSinglePermission(
                backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "projes", "", "[{\"type\":\"project\", \"param1\":\"10000\"}]"),
                new FilterPermission().type("project").project(new Project().id("10000")));

        // project - role
        runSinglePermission(
                backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "projes roles", "", "[{\"type\":\"project\", \"param1\":\"10000\", \"param2\":\"10000\"}]"),
                new FilterPermission().type("project").project(new Project().id("10000")).role(new ProjectRole().id(10000L)));

        // group
        runSinglePermission(
                backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "projes roles", "", "[{\"type\":\"group\", \"param1\":\"jira-administrators\"}]"),
                new FilterPermission().type("group").group(new Group("jira-administrators")));

        // multi
        runMultiplePermission(
                backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "multiman", "", "[{\"type\":\"project\", \"param1\":\"10000\", \"param2\":\"10000\"}, {\"type\":\"group\", \"param1\":\"jira-administrators\"}]"),
                new FilterPermission().type("group").group(new Group("jira-administrators")),
                new FilterPermission().type("project").project(new Project().id("10000")).role(new ProjectRole().id(10000L)));

        // a user has project access but not project access
        // fred is is jira-users; default permission scheme in this test does not grant access to jira-users
        String filterId = backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "multi-openproject", "", "[{\"type\":\"project\", \"param1\":\"10000\"}, {\"type\":\"group\", \"param1\":\"jira-users\"}]");

        filterClient.loginAs("fred");
        runSinglePermission(filterId,
                new FilterPermission().type("group").group(new Group("jira-users")));

        filterClient.loginAs("admin");
        runMultiplePermission(filterId,
                new FilterPermission().type("group").group(new Group("jira-users")),
                new FilterPermission().type("project").project(new Project().id("10000")));
    }

    public void testFilterSubscriptions()
    {
        filterClient.loginAs("admin");
        String filterId = backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "filter with subs", "", "[{\"type\":\"project\", \"param1\":\"10000\", \"param2\":\"10000\"}, {\"type\":\"group\", \"param1\":\"jira-administrators\"}]");
        backdoor.filterSubscriptions().addSubscription(Long.valueOf(filterId), "jira-administrators", "0 0 0 ? 1 MON#3", false);
        Filter filter = filterClient.get(filterId, Filter.Expand.subscriptions);

        assertEquals(1, filter.subscriptions.size);
        assertEquals("admin", filter.subscriptions.items.get(0).user.name);
        assertEquals("jira-administrators", filter.subscriptions.items.get(0).group.name());

        backdoor.filterSubscriptions().addSubscription(Long.valueOf(filterId), null, "0 0 0 ? 1 MON#3", false);
        filter = filterClient.get(filterId, Filter.Expand.subscriptions);

        assertEquals(2, filter.subscriptions.size);
        assertEquals("admin", filter.subscriptions.items.get(1).user.name);
        assertNull(filter.subscriptions.items.get(1).group);
    }

    public void testSharedUsersWithGroup() throws Exception
    {
        final String filterId = backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "shared with group", "", "[{\"type\":\"group\", \"param1\":\"jira-administrators\"}]");
        Matcher<Iterable<String>> matcher = hasItems(String.class, "admin");
        assertSharedUsers(filterId, matcher);
    }

    public void testSharedUsersGlobal() throws Exception
    {
        final String filterId = backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "shared with global", "", "[{\"type\":\"global\"}]");
        Matcher<Iterable<String>> matcher = hasItems(String.class, "admin", "bob", "fred");
        assertSharedUsers(filterId, matcher);
    }

    public void testSharedUsersWithRole() throws Exception
    {
        final String filterId = backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "shared with role", "", "[{\"type\":\"project\", \"param1\":\"10000\", \"param2\":\"10000\"}]");
        Matcher<Iterable<String>> matcher = hasItems(String.class, "admin", "bob", "fred");
        assertSharedUsers(filterId, matcher);
    }

    public void testSharedUsersWithProject() throws Exception
    {
        final String filterId = backdoor.searchRequests().createFilter("admin", "assignee=currentUser()", "shared with project", "", "[{\"type\":\"project\", \"param1\":\"10000\"}]");
        Matcher<Iterable<String>> matcher = hasItems(String.class, "admin", "bob");
        assertSharedUsers(filterId, matcher);
    }

    public void testGetAndSetColumns()
    {
        String filterId = backdoor.filters().createFilter("", "myfilter");

        String message = null;
        try
        {
            //New filter doesn't have columns, will 404
            backdoor.columnControl().getFilterColumns(filterId);
        }
        catch (UniformInterfaceException e)
        {
            message = e.getMessage();
        }

        assertEquals("Client response status: 404", message);

        List<String> defaultColumns = Lists.newArrayList("issuetype", "issuekey", "summary", "assignee", "reporter");
        assertTrue("No errors when setting columns to filter", backdoor.columnControl().setFilterColumns(filterId, defaultColumns));
        List<ColumnControl.ColumnItem> filterColumns = backdoor.columnControl().getFilterColumns(filterId);
        for (int i = 0; i < filterColumns.size(); ++i)
        {
            assertEquals(defaultColumns.get(0), filterColumns.get(0).value);
        }
        assertEquals(defaultColumns.size(), filterColumns.size());

        assertTrue("No errors when removing all columns of filter", backdoor.columnControl().setFilterColumns(filterId, Lists.<String>newArrayList()));
        assertEquals(0, backdoor.columnControl().getFilterColumns(filterId).size());
    }

    private void assertSharedUsers(String filterId, Matcher<Iterable<String>> matcher)
    {
        // First check that no expansion is happening.
        assertEquals(0, filterClient.get(filterId).sharedUsers.items.size());

        List<User> sharedUsers = filterClient.get(filterId, Filter.Expand.sharedUsers).sharedUsers.items;
        Collection<String> sharedUserNames = Collections2.transform(sharedUsers, new Function<User, String>()
        {
            @Override
            public String apply(@Nullable User user)
            {
                assert user != null;
                return user.name;
            }
        });

        // Now check that we are expanding the jira-administrators group as expected.
        assertThat(sharedUserNames, matcher);
    }

    public void testDefaultShareScope()
    {
        Map<String,String> result;

        backdoor.permissions().addGlobalPermission(Permissions.CREATE_SHARED_OBJECT, "jira-users");

        // Default scope is private
        filterClient.loginAs("fred");
        result = filterClient.getDefaultShareScope();
        assertEquals("PRIVATE", result.get("scope"));

        // Set default to global
        Map<String,String> scope = new LinkedHashMap<String, String>();
        scope.put("scope", "GLOBAL");
        result = filterClient.setDefaultShareScope(scope);
        assertEquals("GLOBAL", result.get("scope"));
        result = filterClient.getDefaultShareScope();
        assertEquals("GLOBAL", result.get("scope"));

        // Remove permission to create shared filters
        backdoor.permissions().removeGlobalPermission(Permissions.CREATE_SHARED_OBJECT, "jira-users");
        result = filterClient.getDefaultShareScope();
        assertEquals("PRIVATE", result.get("scope"));
    }

    private void runSinglePermission(String filterId, FilterPermission expected)
    {
        Filter filter = filterClient.get(filterId);
        assertThat(filter.sharePermissions.get(0), matchesFilterPermission(expected));
    }

    private void runMultiplePermission(String filterId, FilterPermission... expected)
    {
        Filter filter = filterClient.get(filterId);
        assertEquals(expected.length, filter.sharePermissions.size());
        List<FilterPermission> expectedFilterPermissions = Arrays.asList(expected);
        for (FilterPermission  expectedFilterPermission : expectedFilterPermissions)
        {
            assertThat(filter.sharePermissions, Matchers.<FilterPermission>hasItem(matchesFilterPermission(expectedFilterPermission)));
        }
    }

    public static Matcher<FilterPermission> matchesFilterPermission(final FilterPermission expected){

        return new DiagnosingMatcher<FilterPermission>()
        {

            protected FilterPermission theExpected = expected;

            @Override
            public boolean matches(final Object actualObj,Description mismatch)
            {
                FilterPermission actual = (FilterPermission)actualObj;
                if (!expected.type.equals(actual.type))
                {
                    return false;
                }

                if (null == expected.project)
                {
                    if (actual.project != null)
                    {
                        return false;
                    }
                }
                else
                {
                    if (actual.project == null)
                    {
                        return false;
                    }
                    if (!actual.project.id.equals(expected.project.id))
                    {
                        return false;
                    }
                }

                if (null == expected.role)
                {
                    if (actual.role != null)
                    {
                        return false;
                    }
                }
                else
                {
                    if (actual.role == null)
                    {
                        return false;
                    }
                    if (actual.role.id.longValue() != expected.role.id.longValue())
                    {
                        return false;
                    }
                }

                if (null == expected.group)
                {
                    if (actual.group != null)
                    {
                        return false;
                    }
                }
                else
                {
                    if (actual.group == null)
                    {
                        return false;
                    }
                    if (!actual.group.name().equals(expected.group.name()))
                    {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Id=").appendValue(expected.id);
                description.appendText(", Type=").appendText(expected.type);
                if (expected.project != null)
                {
                    description.appendText(", Project=").appendText(expected.project.id);
                }
                else
                {
                    description.appendText(", Project=null");
                }

                if (expected.role != null)
                {
                    description.appendText(", Role=").appendValue(expected.role.id);
                }
                else
                {
                    description.appendText(", Role=null");
                }

                if (expected.group != null)
                {
                    description.appendText(", Group=").appendValue(expected.group.name());
                }
                else
                {
                    description.appendText(", Group=null");
                }
            }

        };
    }

    /**
     * This tests the sent and received components are virtually the same although they may hve slightly different shapes.
     * @param expectedFilter The expected component
     * @param actualFilter The actual component
     */
    private void assertFilterEqual(Filter expectedFilter, Filter actualFilter)
    {
        assertNotNull(actualFilter.id);
        assertNotNull(actualFilter.self);

        assertEquals(expectedFilter.name, actualFilter.name);
        assertEquals(expectedFilter.description, actualFilter.description);
        assertEquals(expectedFilter.jql, actualFilter.jql);
    }

}
