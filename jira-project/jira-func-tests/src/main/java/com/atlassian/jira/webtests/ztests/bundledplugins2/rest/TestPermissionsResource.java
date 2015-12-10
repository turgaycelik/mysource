package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestPermissionsResource extends RestFuncTest
{

    private PermissionsClient permissionsClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        permissionsClient = new PermissionsClient(getEnvironmentData());
        administration.restoreData("TestPermissionsRest.xml");
        // The restore didn't restart the plugin system, but we need this module restarted
        backdoor.plugins().disablePluginModule("com.atlassian.jira.dev.func-test-plugin:func.test.global.permission");
        backdoor.plugins().enablePluginModule("com.atlassian.jira.dev.func-test-plugin:func.test.global.permission");
    }

    public void testGetPermissions()
    {
        Response<PermissionsOuter> outer = permissionsClient.getPermissions();

        // test a global permission
        PermissionJsonBean admin = outer.body.permissions.get("ADMINISTER");
        assertNotNull(admin);
        assertEquals("Ability to perform most administration functions (excluding Import & Export, SMTP Configuration, etc.).", admin.description);
        assertEquals("0", admin.id);
        assertEquals("ADMINISTER", admin.key);
//        assertEquals("GLOBAL", admin.type);
        assertEquals("JIRA Administrators", admin.name);
        assertEquals(true, admin.havePermission);

        // test a project permission
        PermissionJsonBean browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals("Ability to browse projects and the issues within them.", browse.description);
        assertEquals("10", browse.id);
        assertEquals("BROWSE", browse.key);
//        assertEquals("PROJECT", browse.type);
        assertEquals("Browse Projects", browse.name);
        assertEquals(true, browse.havePermission);

        // test an issue permission
        PermissionJsonBean comment = outer.body.permissions.get("COMMENT_ISSUE");
        assertNotNull(comment);
        assertEquals("Ability to comment on issues.", comment.description);
        assertEquals("15", comment.id);
        assertEquals("COMMENT_ISSUE", comment.key);
//        assertEquals("COMMENTS", comment.type);
        assertEquals("Add Comments", comment.name);
        assertEquals(true, comment.havePermission);

        //try a user without these permissions
        outer = permissionsClient.loginAs("user", "user").getPermissions();

        // test a global permission
        admin = outer.body.permissions.get("ADMINISTER");
        assertNotNull(admin);
        assertEquals(false, admin.havePermission);

        // test a project permission
        browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals(false, browse.havePermission);

        // test an issue permission
        comment = outer.body.permissions.get("COMMENT_ISSUE");
        assertNotNull(comment);
        assertEquals(false, comment.havePermission);

        // test a plugged global permission
        PermissionJsonBean plugGlobalPermission = outer.body.permissions.get("func.test.global.permission");
        assertEquals("func.test.global.permission.description", plugGlobalPermission.description);
        assertEquals("func.test.global.permission", plugGlobalPermission.key);
        assertEquals("func.test.global.permission.name", plugGlobalPermission.name);
        assertEquals(false, plugGlobalPermission.havePermission);
    }

    public void testProjectFilter()
    {
        Response<PermissionsOuter> outer = permissionsClient.getPermissions();

        // test a project permission
        PermissionJsonBean browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals("Ability to browse projects and the issues within them.", browse.description);
        assertEquals("10", browse.id);
        assertEquals("BROWSE", browse.key);
//        assertEquals("PROJECT", browse.type);
        assertEquals("Browse Projects", browse.name);
        assertEquals(true, browse.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("projectKey", "PROJECTA").toMap());
        browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals("10", browse.id);
        assertEquals("Browse Projects", browse.name);
        assertEquals(true, browse.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("projectKey", "PROJECTB").toMap());
        browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals("10", browse.id);
        assertEquals("Browse Projects", browse.name);
        assertEquals(false, browse.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("projectId", "10000").toMap());
        browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals("10", browse.id);
        assertEquals("Browse Projects", browse.name);
        assertEquals(true, browse.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("projectId", "10001").toMap());
        browse = outer.body.permissions.get("BROWSE");
        assertNotNull(browse);
        assertEquals("10", browse.id);
        assertEquals("Browse Projects", browse.name);
        assertEquals(false, browse.havePermission);
    }

    public void testIssueFiltering()
    {
        Response<PermissionsOuter> outer = permissionsClient.getPermissions();
        PermissionJsonBean commentDeleteAll = outer.body.permissions.get("COMMENT_DELETE_ALL");
        assertNotNull(commentDeleteAll);
        assertEquals("Ability to delete all comments made on issues.", commentDeleteAll.description);
        assertEquals("36", commentDeleteAll.id);
        assertEquals("COMMENT_DELETE_ALL", commentDeleteAll.key);
//        assertEquals("PROJECT", commentDeleteAll.type);
        assertEquals("Delete All Comments", commentDeleteAll.name);
        assertEquals(true, commentDeleteAll.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("projectKey", "PROJECTA").toMap());
        commentDeleteAll = outer.body.permissions.get("COMMENT_DELETE_ALL");
        assertNotNull(commentDeleteAll);
        assertEquals("36", commentDeleteAll.id);
        assertEquals("COMMENT_DELETE_ALL", commentDeleteAll.key);
        assertEquals(true, commentDeleteAll.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("issueKey", "PROJECTA-3").toMap());
        commentDeleteAll = outer.body.permissions.get("COMMENT_DELETE_ALL");
        assertNotNull(commentDeleteAll);
        assertEquals("36", commentDeleteAll.id);
        assertEquals("COMMENT_DELETE_ALL", commentDeleteAll.key);
        assertEquals(true, commentDeleteAll.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("issueKey", "PROJECTA-4").toMap());
        commentDeleteAll = outer.body.permissions.get("COMMENT_DELETE_ALL");
        assertNotNull(commentDeleteAll);
        assertEquals("36", commentDeleteAll.id);
        assertEquals("COMMENT_DELETE_ALL", commentDeleteAll.key);
        assertEquals(false, commentDeleteAll.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("issueId", "10200").toMap());
        commentDeleteAll = outer.body.permissions.get("COMMENT_DELETE_ALL");
        assertNotNull(commentDeleteAll);
        assertEquals("36", commentDeleteAll.id);
        assertEquals("COMMENT_DELETE_ALL", commentDeleteAll.key);
        assertEquals(true, commentDeleteAll.havePermission);

        outer = permissionsClient.getPermissions(MapBuilder.<String, String>newBuilder().add("issueId", "10201").toMap());
        commentDeleteAll = outer.body.permissions.get("COMMENT_DELETE_ALL");
        assertNotNull(commentDeleteAll);
        assertEquals("36", commentDeleteAll.id);
        assertEquals("COMMENT_DELETE_ALL", commentDeleteAll.key);
        assertEquals(false, commentDeleteAll.havePermission);

    }
    
    // test error reporting


    private class PermissionsClient extends RestApiClient<PermissionsClient>
    {
        protected PermissionsClient(JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        private Response<PermissionsOuter> getPermissions()
        {
            return toResponse(new Method()
            {
                @Override
                public ClientResponse call()
                {
                    return createResource().path("mypermissions").get(ClientResponse.class);
                }
            }, PermissionsOuter.class);
        }

        private Response<PermissionsOuter> getPermissions(final Map<String, String> params)
        {
            return toResponse(new Method()
            {
                @Override
                public ClientResponse call()
                {
                    WebResource path = createResource().path("mypermissions");
                    for (Map.Entry<String, String> entry : params.entrySet())
                    {
                        path = path.queryParam(entry.getKey(), entry.getValue());
                    }
                    return path.get(ClientResponse.class);
                }
            }, PermissionsOuter.class);
        }
    }

    private static class PermissionsOuter
    {
        @JsonProperty
        HashMap<String, PermissionJsonBean> permissions;
    }

    private static class PermissionJsonBean
    {
        @JsonProperty
        String id;
        @JsonProperty
        String key;
        @JsonProperty
        String name;
        @JsonProperty
        String description;
//        @JsonProperty
//        String type;
        @JsonProperty
        boolean havePermission;
    }
}
