package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.HttpHeaders;

import java.util.Set;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Verification for Create, Update and Delete operations
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestUserResourceForCreateUpdateDelete extends RestFuncTest
{
    private static final String SELF = "self";
    private static final String NAME = "name";
    private static final String KEY = "key";
    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String DISPLAY_NAME = "displayName";

    private static final String KEY_VALUE = "charlie";
    private static final String NAME_VALUE = "Charlie";
    private static final String PASSWORD_VALUE = "abracadabra";
    private static final String EMAIL_ADDRESS_VALUE = "charlie@localhost";
    private static final String DISPLAY_NAME_VALUE = "Charlie of Atlassian";

    private static final String NAME_CHANGED_VALUE = "Charlie2";
    private static final String EMAIL_ADDRESS_CHANGED_VALUE = "charlie2@localhost";
    private static final String DISPLAY_NAME_CHANGED_VALUE = "Charlie of Atlassian II";
    private static final String REST_URL = "/rest/api/2/user?";


    private UserClient userClient;
    private JIRAEnvironmentData environmentData;


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        environmentData = getEnvironmentData();
        userClient = new UserClient(environmentData);
        backdoor.restoreBlankInstance();
    }

    @Override
    protected void tearDownTest()
    {
        userClient.close();
    }


    public void testHappyPathUseCase()
    {

        //no user at first
        thereIsNoUser(KEY_VALUE);

        //then create one
        final String createdUserKey = createUser();

        //try to update user
        updateUser(createdUserKey);

        //change his password
        changeUserPassword(createdUserKey);

        //remove him
        removeExistingUser(createdUserKey);

        //what if I try to remove it again...
        removeNotExistingUser(createdUserKey);
    }



    private String createUser()
    {
        //then we create it
        final ClientResponse responseCreate = userClient.createUser(NAME_VALUE, PASSWORD_VALUE, EMAIL_ADDRESS_VALUE, DISPLAY_NAME_VALUE);
        assertThat(responseCreate.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        String createResult = responseCreate.getHeaders().get(HttpHeaders.LOCATION).get(0);
        assertThat(createResult, startsWith(getJiraExperimentalApiUserPath()));

        //verify returned user
        final String createdUserKey = verifyCreatedUserAndReturnKey(responseCreate);
        responseCreate.close();

        //read it to verify
        final ClientResponse responseRead = userClient.getUser(createdUserKey);
        assertThat(responseRead.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        verifyCreatedUserAndReturnKey(responseRead);

        responseRead.close();

        return createdUserKey;
    }

    private String verifyCreatedUserAndReturnKey(final ClientResponse responseCreate)
    {
        final UserBean userBean = getMapEntity(responseCreate);
        assertThat(userBean, notNullValue());
        assertThat(userBean.getName(), equalTo(NAME_VALUE));
        assertThat(userBean.getEmailAddress(), equalTo(EMAIL_ADDRESS_VALUE));
        assertThat(userBean.getDisplayName(), equalTo(DISPLAY_NAME_VALUE));
        assertThat(userBean.getSelf().toString(), startsWith(getJiraExperimentalApiUserPath()));

        return userBean.getKey();
    }

    private String getJiraExperimentalApiUserPath()
    {
        return environmentData.getBaseUrl() + REST_URL;
    }

    private void updateUser(final String userKey)
    {
        //try to update it
        ClientResponse responseUpdate = userClient.updateUser(userKey, NAME_CHANGED_VALUE, EMAIL_ADDRESS_CHANGED_VALUE, DISPLAY_NAME_CHANGED_VALUE);
        assertThat(responseUpdate.getStatus(), equalTo(Response.Status.OK.getStatusCode()));

        final UserBean userBean = getMapEntity(responseUpdate);
        responseUpdate.close();
        assertThat(userBean, notNullValue());
        assertThat(userBean.getName(), equalTo(NAME_CHANGED_VALUE));
        assertThat(userBean.getEmailAddress(), equalTo(EMAIL_ADDRESS_CHANGED_VALUE));
        assertThat(userBean.getDisplayName(), equalTo(DISPLAY_NAME_CHANGED_VALUE));
        assertThat(userBean.getSelf().toString(), startsWith(getJiraExperimentalApiUserPath()));

    }

    private void changeUserPassword(final String userKey)
    {
        ClientResponse response = userClient.changePassword(userKey, "hocuspocus");
        assertThat(response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
        response.close();
    }

    private void removeExistingUser(final String userKey)
    {
        ClientResponse response = userClient.deleteUser(userKey);
        assertThat(response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
        response.close();

        //user should be gone now
        thereIsNoUser(userKey);
    }

    private void thereIsNoUser(final String userKey)
    {
        ClientResponse response = userClient.getUser(userKey);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        response.close();
    }

    private void removeNotExistingUser(final String userKey)
    {
        //removing not existent user
        ClientResponse response = userClient.deleteUser(userKey);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        response.close();
    }

    private UserBean getMapEntity(final ClientResponse responseRead)
    {
        return responseRead.getEntity(UserBean.class);
    }


    private class UserClient extends RestApiClient<UserClient>
    {
        private static final String USER_PATH = "user";
        private static final String PASSWORD_PATH = "password";

        private final Set<ClientResponse> responses = Sets.newHashSet();

        protected UserClient(JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        protected WebResource createResource()
        {
            return resourceRoot(environmentData.getBaseUrl().toExternalForm()).path("rest").path("api").path("2");
        }


        private ClientResponse createUser(final String name, final String password, final String emailAddress, final String displayName)
        {
            WebResource webResource = createResource().path(USER_PATH);

            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();

            builder.put(NAME, name);
            builder.put(PASSWORD, password);
            builder.put(EMAIL_ADDRESS, emailAddress);
            builder.put(DISPLAY_NAME, displayName);

            final ClientResponse clientResponse = webResource.type("application/json").post(ClientResponse.class, builder.build());
            responses.add(clientResponse);
            return clientResponse;
        }


        private ClientResponse getUser(final String key)
        {
            checkNotNull(key);

            final WebResource webResource = createResource().path(USER_PATH).queryParam(KEY, key);

            final ClientResponse clientResponse = webResource.get(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse updateUser(final String key, final String name, final String emailAddress, final String displayName)
        {
            checkNotNull(key);

            final WebResource webResource = createResource().path(USER_PATH).queryParam(KEY, key);

            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            if (name != null)
            {
                builder.put(NAME, name);
            }
            if (emailAddress != null)
            {
                builder.put(EMAIL_ADDRESS, emailAddress);
            }
            if (displayName != null)
            {
                builder.put(DISPLAY_NAME, displayName);
            }

            final ClientResponse clientResponse = webResource.type("application/json").put(ClientResponse.class, builder.build());
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse changePassword(final String key, final String password)
        {
            checkNotNull(key);

            final WebResource webResource = createResource().path(USER_PATH + "/" + PASSWORD_PATH).queryParam(KEY, key);

            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            if (password != null)
            {
                builder.put(PASSWORD, password);
            }

            final ClientResponse clientResponse = webResource.type("application/json").put(ClientResponse.class, builder.build());
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse deleteUser(final String key)
        {
            checkNotNull(key);

            final WebResource webResource = createResource().path(USER_PATH).queryParam(KEY, key);

            final ClientResponse delete = webResource.delete(ClientResponse.class);
            responses.add(delete);
            return delete;
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
