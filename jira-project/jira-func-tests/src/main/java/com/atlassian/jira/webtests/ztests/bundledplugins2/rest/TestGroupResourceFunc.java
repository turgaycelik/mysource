package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.HttpHeaders;

import java.util.Set;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestGroupResourceFunc extends RestFuncTest
{
    private static boolean initialized;

    private GroupClient groupClient;
    private final Set<ClientResponse> responses = Sets.newHashSet();

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        environmentData = getEnvironmentData();
        groupClient = new GroupClient(environmentData);
        if (!initialized)
        {
            backdoor.restoreBlankInstance();

            // Create a default group via REST
            createGroup("jedi");
            initialized = true;
        }
    }

    @Override
    protected void tearDownTest()
    {
        groupClient.close();
    }

    public void testGroupResourceAddAndRemoveUserHappyPath()
    {
        ensureGroupExists("jedi");
        addUserToGroup("jedi", "fred");
        ensureCantAddUserToGroup("jedi", "fred");

        removeUserFromGroup("jedi", "fred");
        ensureCantRemoveUserFromGroup("jedi", "fred");
    }

    public void testGroupResourceDeleteGroupHappyPath()
    {
        ensureGroupExists("jedi");
        deleteGroup("jedi");

        ensureNoGroup("jedi");
        ensureCantDeleteGroup("jedi");
    }

    private void ensureCantRemoveUserFromGroup(final String group, final String user)
    {
        final ClientResponse response = groupClient.removeUserFromGroup(group, user);
        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        response.close();
    }

    private void ensureGroupExists(final String group)
    {
        final ClientResponse response = groupClient.getGroup(group);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        response.close();
    }

    private void ensureNoGroup(final String group)
    {
        final ClientResponse response = groupClient.getGroup(group);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        response.close();
    }

    private void createGroup(final String group)
    {
        final ClientResponse response = groupClient.createGroup(group);
        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        final String createResult = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final String expectedUrl = environmentData.getBaseUrl() + "/rest/api/2/group?groupname=" + group;
        assertThat(createResult, startsWith(expectedUrl));
        response.close();
    }

    private void addUserToGroup(final String group, final String user)
    {
        final ClientResponse response = groupClient.addUserToGroup(group, user);
        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));

        final String createResult = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final String expectedUrl = environmentData.getBaseUrl() + "/rest/api/2/group?groupname=" + group;
        assertThat(createResult, startsWith(expectedUrl));
        response.close();
    }

    private void ensureCantAddUserToGroup(final String group, final String user)
    {
        final ClientResponse response = groupClient.addUserToGroup(group, user);
        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        response.close();
    }

    private void deleteGroup(final String group)
    {
        final ClientResponse response = groupClient.deleteGroup(group);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        response.close();
    }

    private void ensureCantDeleteGroup(final String group)
    {
        final ClientResponse response = groupClient.deleteGroup(group);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        response.close();
    }

    private void removeUserFromGroup(final String group, final String user)
    {
        final ClientResponse response = groupClient.removeUserFromGroup(group, user);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        response.close();
    }
   

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class GroupClient extends RestApiClient<GroupClient>
    {

        public static final String GROUP_RESOURCE = "group";
        private static final String GROUP_ADD_USER_RESOURCE = "group/user";
        public static final String GROUP_NAME = "groupname";
        public static final String USER_NAME = "username";
        public static final String NAME = "name";

        public GroupClient(final JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        public ClientResponse createGroup(final String group)
        {
            final WebResource webResource = createResource().path(GROUP_RESOURCE);
            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            builder.put("name", group);
            return doPost(webResource, builder);
        }

        public ClientResponse deleteGroup(final String group)
        {
            final WebResource webResource = createResource().path(GROUP_RESOURCE)
                    .queryParam(GROUP_NAME, group);
            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            return doDelete(webResource, builder);

        }

        public ClientResponse addUserToGroup(final String group, final String user)
        {
            final WebResource webResource = createResource().path(GROUP_ADD_USER_RESOURCE).queryParam(GROUP_NAME, group);
            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            builder.put(NAME, user);
            return doPost(webResource, builder);
        }

        public ClientResponse removeUserFromGroup(final String group, final String user)
        {
            final WebResource webResource = createResource().path(GROUP_ADD_USER_RESOURCE)
                    .queryParam(GROUP_NAME, group)
                    .queryParam(USER_NAME, user);
            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            return doDelete(webResource, builder);
        }

        private ClientResponse doPost(final WebResource webResource, final ImmutableMap.Builder<Object, Object> builder)
        {
            final ClientResponse clientResponse = webResource
                    .type("application/json")
                    .post(ClientResponse.class, builder.build());

            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse doDelete(final WebResource webResource, final ImmutableMap.Builder<Object, Object> builder)
        {
            final ClientResponse clientResponse = webResource
                    .type("application/json")
                    .delete(ClientResponse.class, builder.build());

            responses.add(clientResponse);
            return clientResponse;
        }

        public ClientResponse getGroup(final String group)
        {
            final WebResource webResource = createResource().path(GROUP_RESOURCE).queryParam(GROUP_NAME, group);

            final ClientResponse clientResponse = webResource.get(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private void close()
        {
            for (ClientResponse response : responses)
            {
                response.close();
            }
        }
    }
}
