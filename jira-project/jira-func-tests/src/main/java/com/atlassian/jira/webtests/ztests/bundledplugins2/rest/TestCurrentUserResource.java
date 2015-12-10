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
public class TestCurrentUserResource extends RestFuncTest
{
    private static final String SELF = "self";

    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String DISPLAY_NAME = "displayName";

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
        backdoor.restoreBlankInstance();
        backdoor.usersAndGroups().addUser("notadmin", "withpassword", "DisplayNoAdminName", "noadmin@localhost");

        userClient = new UserClient(environmentData);
        userClient.loginAs("notadmin", "withpassword");
    }

    @Override
    protected void tearDownTest()
    {
        userClient.close();
    }


    public void testHappyPathUseCase()
    {
        //try to update myself
        updateUser();

        //change my password
        changeMyPassword();
    }

    private String getJiraExperimentalApiUserPath()
    {
        return environmentData.getBaseUrl() + REST_URL;
    }

    private void updateUser()
    {
        //try to update it
        final ClientResponse responseUpdate = userClient.updateUser(EMAIL_ADDRESS_CHANGED_VALUE, DISPLAY_NAME_CHANGED_VALUE);
        assertThat(responseUpdate.getStatus(), equalTo(Response.Status.OK.getStatusCode()));

        final UserBean userBean = getMapEntity(responseUpdate);
        responseUpdate.close();
        assertThat(userBean, notNullValue());
        assertThat(userBean.getEmailAddress(), equalTo(EMAIL_ADDRESS_CHANGED_VALUE));
        assertThat(userBean.getDisplayName(), equalTo(DISPLAY_NAME_CHANGED_VALUE));
        assertThat(userBean.getSelf().toString(), startsWith(getJiraExperimentalApiUserPath()));

    }

    private void changeMyPassword()
    {
        final ClientResponse response = userClient.changePassword("hocuspocus");
        assertThat(response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
        response.close();
    }

    private UserBean getMapEntity(final ClientResponse responseRead)
    {
        return responseRead.getEntity(UserBean.class);
    }


    private class UserClient extends RestApiClient<UserClient>
    {
        private static final String CURRENT_USER_PATH = "myself";
        private static final String PASSWORD_PATH = "password";

        private final Set<ClientResponse> responses = Sets.newHashSet();

        protected UserClient(final JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        protected WebResource createResource()
        {
            return resourceRoot(environmentData.getBaseUrl().toExternalForm()).path("rest").path("api").path("2");
        }

        private ClientResponse getUser(final String key)
        {
            checkNotNull(key);

            final WebResource webResource = createResource().path(CURRENT_USER_PATH);

            final ClientResponse clientResponse = webResource.get(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse updateUser(final String emailAddress, final String displayName)
        {
            final WebResource webResource = createResource().path(CURRENT_USER_PATH);

            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
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

        private ClientResponse changePassword(final String password)
        {
            final WebResource webResource = createResource().path(CURRENT_USER_PATH + "/" + PASSWORD_PATH);

            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            if (password != null)
            {
                builder.put(PASSWORD, password);
            }

            final ClientResponse clientResponse = webResource.type("application/json").put(ClientResponse.class, builder.build());
            responses.add(clientResponse);
            return clientResponse;
        }

        private void close()
        {
            for (final ClientResponse response : responses)
            {
                response.close();
            }
        }
    }
}
