package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.backdoor.ColumnControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.UserClient;
import com.atlassian.jira.testkit.client.restclient.UserPickerResults;
import com.atlassian.jira.testkit.client.restclient.UserPickerUser;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.meterware.httpunit.WebResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the user resource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestUserResource extends RestFuncTest
{
    private static final String USER_PATH = "user";
    private static final String REST_PATH = "rest/api/2";
    private static final String REST_USER_URL = REST_PATH + "/" + USER_PATH;

    private UserClient userClient;

    public void testUserResourceNoUsernameNorKey() throws Exception
    {
        Response response = userClient.getUserResponse(null);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("Either the 'username' or the 'key' query parameters need to be provided"));
    }

    public void testUserResourceForUserByNameThatDoesntExist() throws Exception
    {
        Response response = userClient.getUserResponse("bofh");
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("The user named 'bofh' does not exist"));
    }

    public void testUserResourceForUserByKeyThatDoesntExist() throws Exception
    {
        Response response = userClient.getUserResponseByKey("bofh");
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("The user with the key 'bofh' does not exist"));
    }

    public void testUserResourceForUserByName() throws Exception
    {
        final User fred = userClient.get("bloblaw");
        assertNotNull(fred);
        assertEquals("bloblaw", fred.name);
        assertEquals("bob", fred.key);
    }

    public void testUserResourceForUserByKey() throws Exception
    {
        final User fred = userClient.getByKey("bob");
        assertNotNull(fred);
        assertEquals("bloblaw", fred.name);
        assertEquals("bob", fred.key);
    }

    public void testUserResourceForUserByUsernameAndKey() throws Exception
    {
        final WebResponse response = GET(REST_USER_URL + "?username=bloblaw&key=bob");
        assertEquals(400, response.getResponseCode());
    }

    public void testUserResourceTimeZone() throws Exception
    {
       User user = userClient.get(ADMIN_USERNAME);
       assertEquals("Australia/Sydney", user.timeZone);
    }

    public void testSearchUsers()
    {
        List<User> users = userClient.search("fre", "0", null);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals("fred", user.name);
        assertEquals("Fred Normal", user.displayName);

        //empty search should return nothing
        users = userClient.search("", "0", null);
        assertEquals(0, users.size());

        //try searching via e-mail
        users = userClient.search("love", "0", null);
        assertEquals(1, users.size());
        assertEquals("\u611b", users.get(0).name);

        //searching for a should have 3 results
        users = userClient.search("a", null, null);
        assertEquals(3, users.size());
        assertEquals("a\\b", users.get(0).name);
        assertEquals("admin", users.get(1).name);
        assertEquals("sp ace", users.get(2).name);

        //now try paging
        users = userClient.search("a", "0", "1");
        assertEquals(1, users.size());
        assertEquals("a\\b", users.get(0).name);

        users = userClient.search("a", "1", "30");
        assertEquals(2, users.size());
        assertEquals("admin", users.get(0).name);
        assertEquals("sp ace", users.get(1).name);

        // Search only includes active by default
        users = userClient.search("fred", null, null, null, null);
        assertEquals(1, users.size());
        assertEquals("fred", users.get(0).name);

        // include inactive
        users = userClient.search("fred", null, null, null, true);
        assertEquals(2, users.size());
        assertEquals("fred", users.get(0).name);
        assertEquals("fredx", users.get(1).name);

        // include inactive - explicit active
        users = userClient.search("fred", null, null, true, true);
        assertEquals(2, users.size());
        assertEquals("fred", users.get(0).name);
        assertEquals("fredx", users.get(1).name);

        // inactive only
        users = userClient.search("fred", null, null, false, true);
        assertEquals(1, users.size());
        assertEquals("fredx", users.get(0).name);

        // no-one
        users = userClient.search("fred", null, null, false, false);
        assertEquals(0, users.size());

        // with renamed user
        users = userClient.search("blo", null, null, true, false);
        assertEquals(1, users.size());
        assertEquals("bloblaw", users.get(0).name);
        assertEquals("bob", users.get(0).key);
    }

    public void testPickerUsers()
    {
        UserPickerResults results = userClient.picker("fre", null);

        assertEquals(1, results.users.size());
        final UserPickerUser user = results.users.get(0);
        assertEquals("fred", user.name);
        assertEquals("fred", user.key);
        assertEquals("Fred Normal", user.displayName);
        assertEquals("<strong>Fre</strong>d Normal - <strong>fre</strong>d@example.com (<strong>fre</strong>d)", user.html);
        assertEquals("Showing 1 of 1 matching users", results.header);
        //try searching via e-mail
        results = userClient.picker("love", null);

        assertEquals(1, results.users.size());
        assertEquals("\u611b", results.users.get(0).name);
        assertEquals("Showing 1 of 1 matching users", results.header);

        //searching for a should have 3 results
        results = userClient.picker("a", null);
        assertEquals(3, results.users.size());
        assertEquals("<strong>a</strong>\\b - <strong>a</strong>b@example.com (<strong>a</strong>\\b)", results.users.get(0).html);
        assertEquals("<strong>A</strong>dministrator - <strong>a</strong>dmin@example.com (<strong>a</strong>dmin)", results.users.get(1).html);
        assertEquals("sp <strong>a</strong>ce - space@example.com (sp <strong>a</strong>ce)", results.users.get(2).html);
        assertEquals("Showing 3 of 3 matching users", results.header);
    }

    public void testUsersByPermission()
    {
        final Backdoor testkit = new Backdoor(getEnvironmentData());
        testkit.usersAndGroups().addGroup("group");
        testkit.usersAndGroups().addUser("groupie");
        testkit.usersAndGroups().addUserToGroup("groupie", "group");
        testkit.usersAndGroups().addUser("singleton");

        administration.permissionSchemes().defaultScheme().grantPermissionToSingleUser(Permissions.COMMENT_EDIT_ALL, "singleton");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.COMMENT_EDIT_OWN, "group");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.ASSIGNABLE_USER, "group");
        administration.permissionSchemes().defaultScheme().grantPermissionToCurrentAssignee(Permissions.COMMENT_DELETE_OWN);

        assertEquals("HSP-1", testkit.issues().createIssue("HSP", "Permissions test").key);

        class TemporaryClient extends UserClient
        {
            // TODO - move changes to the testkit
            protected TemporaryClient()
            {
                super(getEnvironmentData());
            }

            // copied from parent - why was it made private in the firs place?
            private WebResource applyPagingParams(String query, String startAt, String maxResults, WebResource resource)
            {
                resource = resource.queryParam("username", query);
                if (StringUtils.isNotBlank(startAt))
                {
                    resource = resource.queryParam("startAt", startAt);
                }
                if (StringUtils.isNotBlank(maxResults))
                {
                    resource = resource.queryParam("maxResults", maxResults);
                }
                return resource;
            }

            public List<User> searchByPermission(String query, Iterable<String> permissions, String issueKey, String projectKey, String startAt, String maxResults)
            {
                final String permissionString = Joiner.on(",").join(permissions);
                final WebResource resource = getSearchByPermissionResource(query, permissionString, issueKey, projectKey, startAt, maxResults);
                return Arrays.asList(resource.get(User[].class));
            }

            public WebResource getSearchByPermissionResource(String query, String permissions, String issueKey, String projectKey, String startAt, String maxResults)
            {
                WebResource resource = applyPagingParams(query, startAt, maxResults, createResource().path("user").path("permission").path("search"));
                if (issueKey != null) resource = resource.queryParam("issueKey", issueKey);
                if (projectKey != null) resource = resource.queryParam("projectKey", projectKey);
                return resource.queryParam("permissions", permissions);
            }
        }

        final TemporaryClient temporaryClient = new TemporaryClient();
        final Function<User, String> USERNAME = new Function<User, String>()
        {
            @Override
            public String apply(final User input)
            {
                return input.name;
            }
        };

        assertEquals(ImmutableList.of("singleton"), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("COMMENT_EDIT_ALL"), "HSP-1", null, null, null), USERNAME));
        assertEquals(ImmutableList.of("singleton"), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("COMMENT_EDIT_ALL"), null, "HSP", null, null), USERNAME));

        assertEquals(ImmutableList.of("groupie"), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("COMMENT_EDIT_OWN"), null, "HSP", null, null), USERNAME));

        assertEquals(ImmutableList.<String>of(), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("COMMENT_EDIT_OWN", "COMMENT_DELETE_OWN"), "HSP-1", null, null, null), USERNAME));

        testkit.issues().assignIssue("HSP-1", "groupie");
        assertEquals(ImmutableList.of("groupie"), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("COMMENT_EDIT_OWN", "COMMENT_DELETE_OWN"), "HSP-1", null, null, null), USERNAME));

        assertEquals(ImmutableList.of("a\\b", "admin", "bloblaw", "c/d", "groupie"), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("ASSIGNABLE_USER"), "HSP-1", null, null, null), USERNAME));

        assertEquals(ImmutableList.of("admin", "bloblaw"), Lists.transform(
                temporaryClient.searchByPermission("", ImmutableList.of("ASSIGNABLE_USER"), "HSP-1", null, "1", "2"), USERNAME));

        assertEquals(ImmutableList.of("groupie"), Lists.transform(
                temporaryClient.searchByPermission("gro", ImmutableList.of("ASSIGNABLE_USER"), "HSP-1", null, "0", "1"), USERNAME));


    }


    public void testAssignableAndViewableUsers()
    {
        //only admins will be assignable and only devs can browse issues.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, "jira-developers");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGNABLE_USER, "jira-administrators");
        administration.permissionSchemes().defaultScheme().removePermission(BROWSE, "jira-users");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(BROWSE, "jira-developers");

        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        //searching for a should have 3 results
        List<User> users = userClient.search("a", null, null);
        assertEquals(3, users.size());
        assertEquals("a\\b", users.get(0).name);
        assertEquals("admin", users.get(1).name);
        assertEquals("sp ace", users.get(2).name);

        //only admin is an admin and can there for be assigned issues
        users = userClient.searchAssignable("a", issueKey, null, null);
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).name);

        //these are the only devs which can browse issues
        users = userClient.searchViewableIssue("a", issueKey, null, null);
        assertEquals(2, users.size());
        assertEquals("a\\b", users.get(0).name);
        assertEquals("admin", users.get(1).name);
    }

    // JRA-27212
    public void testUnprivilegedAccessToBrowseUsersYieldsNoResults()
    {
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        // only admins may browse users.
        backdoor.permissions().removeGlobalPermission(USER_PICKER, "jira-users");
        backdoor.permissions().removeGlobalPermission(USER_PICKER, "jira-developers");
        backdoor.permissions().addGlobalPermission(USER_PICKER, "jira-administrators");

        List<User> users = userClient.searchViewableIssue("a", issueKey, null, null);
        assertEquals(3, users.size());

        userClient.loginAs("fred");

        users = userClient.searchViewableIssue("a", issueKey, null, null);
        assertEquals(0, users.size());
    }

    // JRA-27212
    public void testUnprivilegedAccessToAssignableUsersIsDenied()
    {
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        // nobody is allowed to assign issues.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-developers");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-administrators");

        Response response = userClient.getResponse(userClient.getSearchAssignableResource("a", issueKey, null, null));
        assertEquals(401, response.statusCode);
    }
    
    // JRA-27212
    public void testDoNotRequireBrowseUserPermissionToListAssignableUsers()
    {
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        // only developers are allowed to assign issues.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-users");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGN_ISSUE, "jira-developers");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-administrators");
        // only admins may be assigned to.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, "jira-users");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, "jira-developers");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGNABLE_USER, "jira-administrators");
        // neither group is allowed to browse users.
        administration.permissionSchemes().defaultScheme().removePermission(USER_PICKER, "jira-users");
        administration.permissionSchemes().defaultScheme().removePermission(USER_PICKER, "jira-developers");
        administration.permissionSchemes().defaultScheme().removePermission(USER_PICKER, "jira-administrators");

        // log in as a developer.
        userClient.loginAs("c/d", "c/d");

        // ask for a list of assignable people.
        List<User> users = userClient.searchAssignable("a", issueKey, null, null);
        // admin should be returned, because he is the only person who is assignable.
        // "a\\b" should not be returned, as he is a developer.
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).name);
    }
    
    public void testUserResourceGroupsNotExpanded() throws Exception
    {
        final String username = FRED_USERNAME;
        final String userPath = getPathFor(username);

        User user = userClient.get(username);
        assertEquals(getBaseUrlPlus(userPath), user.self);

        // verify that groups are not expanded
        assertEquals("groups", user.expand);

        assertNotNull(user.groups.size);
        assertEquals(1, user.groups.size);

        assertNotNull(user.groups.items);
        assertTrue(user.groups.items.isEmpty());
    }

    public void testUserResourceGroupsExpanded() throws Exception
    {
        final String username = FRED_USERNAME;
        final String userPath = getPathFor(username);

        User user = userClient.get(username, User.Expand.groups);
        assertEquals(getBaseUrlPlus(userPath), user.self);

        assertNotNull(user.groups);
        assertEquals(1, user.groups.size);

        assertNotNull(user.groups.items);
        assertEquals(1, user.groups.items.size());
        assertEquals("jira-users", user.groups.items.get(0).name());
    }

    public void testGetAnonymouslyUserResource() throws Exception
    {
        Response response = userClient.anonymous().getUserResponse("fred");
        assertEquals(401, response.statusCode);
    }

    public void testUnicodeCharacters() throws Exception
    {
        // Unicode 611B = chinese symbol for love
        final String username = "\u611B";
        final String userPath = getPathFor("%E6%84%9B");

        User user = userClient.get(username);
        assertEquals(getBaseUrlPlus(userPath), user.self);

        // Name, id and display name should have UTF-8 encoded Chinese symbols
        assertEquals("\u611b", user.name);
        assertEquals("\u611b \u6237", user.displayName);
        // URLs should have the Unicode character's UTF-8 encoded then the bytes are Percent-encoded
        assertEquals(getBaseUrlPlus(REST_USER_URL + "?username=%E6%84%9B"), user.self);
    }

    public void testAvatarUrls() throws Exception
    {
        final String username = "fred";
        User user = userClient.get(username);

        // Note that the avatar urls no longer contain the username, and therefore do not have any url encoding concerns
        assertThat(user.avatarUrls, equalTo(createUserAvatarUrls(10062L)));
    }

    /*
     * These encoding-related tests are all in the same method for performance reasons.
     */
    public void testUsernamesWithInterestingCharacters() throws Exception
    {
        assertUserRepresentationIsOK("a\\b", "a%5Cb");          // backslash
        assertUserRepresentationIsOK("c/d", "c/d");             // slash
        assertUserRepresentationIsOK("sp ace", "sp+ace");       // space
        assertUserRepresentationIsOK("pl+us", "pl%2Bus");       // +
        assertUserRepresentationIsOK("per%cent", "per%25cent"); // %
        assertUserRepresentationIsOK("\u611B", "%E6%84%9B"); // %
    }

    public void testUserResourceShouldMaskEmailAddresses() throws Exception
    {
        administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.MASKED);
        User user = userClient.get("fred");
        assertThat(user.emailAddress, equalTo("fred at example dot com"));
    }

    public void testUserResourceShouldHideEmailAddresses() throws Exception
    {
        administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.HIDDEN);
        User user = userClient.get("fred");
        assertNull(user.emailAddress);
    }

    public void testUserGetAndSetColumns()
    {
        //New user should have a default set of columns
        List<ColumnControl.ColumnItem> items = backdoor.columnControl().getLoggedInUserColumns();
        List<String> defaultColumns = Lists.newArrayList("issuetype", "issuekey", "summary", "assignee", "reporter",
                                                         "priority", "status", "resolution", "created", "updated", "duedate");
        for (int i = 0; i < items.size(); ++i)
        {
            assertEquals(defaultColumns.get(0), items.get(0).value);
        }
        assertEquals(defaultColumns.size(), items.size());

        List<String> newColumns = Lists.newArrayList(defaultColumns);
        newColumns.add("description");
        newColumns.add("resolutiondate");
        newColumns.remove("summary");
        newColumns.remove("status");

        assertTrue("No errors when setting the column", backdoor.columnControl().setLoggedInUserColumns(newColumns));

        items = backdoor.columnControl().getLoggedInUserColumns();
        for (int i = 0; i < items.size(); ++i)
        {
            assertEquals(newColumns.get(0), items.get(0).value);
        }
        assertEquals(newColumns.size(), items.size());
        assertTrue("No errors when removing all columns", backdoor.columnControl().setLoggedInUserColumns(Lists.<String>newArrayList()));
        assertEquals(0, backdoor.columnControl().getLoggedInUserColumns().size());
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        userClient = new UserClient(getEnvironmentData());
        administration.restoreData("TestUserResource.xml");
    }

    /**
     * Creates the path for the user resource.
     *
     * @param username a String containing the user name
     * @return the path to the user
     */
    protected String getPathFor(String username)
    {
        return getPathFor(username, emptyMap());
    }

    /**
     * Tests that the user representation is being constructed correctly.
     *
     * @param username the username
     * @param encodedUsername the encoded username (if encoding is necessary)
     */
    private void assertUserRepresentationIsOK(String username, String encodedUsername)
    {
        final String userPath = getPathFor(encodedUsername);

        User user = userClient.get(username);
        assertEquals(username, user.name);
        // explicitly assert that the self URL has the encoded username
        assertEquals("The username is not encoded in the self link", getBaseUrlPlus(REST_USER_URL + "?username=" + encodedUsername), user.self);
    }

    /**
     * Creates the path for the user resource, optionally appending any additional query parameters.
     *
     * @param username a String containing the user name
     * @param queryParams a Map containing query parameters
     * @return the path to the user
     */
    protected String getPathFor(String username, Map<?, ?> queryParams)
    {
        // append the query params in "&key=value" format
        return REST_USER_URL + "?username=" + username + StringUtils.join(Collections2.transform(queryParams.entrySet(), new Function<Map.Entry, Object>()
        {
            public Object apply(Map.Entry from)
            {
                return String.format("&%s=%s", from.getKey(), from.getValue());
            }
        }), "");
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
}
