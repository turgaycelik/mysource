package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestCurrentUserPreferencesResource extends RestFuncTest
{

    private static final String MYPREFERENCES = "mypreferences";
    private static final String KEY = "key";
    private static final String SOME_KEY = "someKey";
    private static final String SOME_VALUE = "someValue";

    private CurrentUserPreferencesClient preferencesClient;


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        preferencesClient = new CurrentUserPreferencesClient(getEnvironmentData());
        backdoor.restoreBlankInstance();
    }

    @Override
    protected void tearDownTest()
    {
        preferencesClient.close();
    }


    public void testHappyPathUseCase()
    {
        //reading preference - shouldn't be there
        ClientResponse responseGet1 = preferencesClient.getPreference(SOME_KEY);
        assertThat(responseGet1.getStatus(), equalTo(ClientResponse.Status.NOT_FOUND.getStatusCode()));

        //writing preference
        ClientResponse responsePut = preferencesClient.setPreference(SOME_KEY, SOME_VALUE);
        assertNotNull(responsePut);
        assertThat(responsePut.getStatus(), equalTo(ClientResponse.Status.NO_CONTENT.getStatusCode()));

        //reading preference
        ClientResponse responseGet2 = preferencesClient.getPreference(SOME_KEY);
        assertThat(responseGet2.getStatus(), equalTo(ClientResponse.Status.OK.getStatusCode()));
        String message = responseGet2.getEntity(String.class);
        assertThat(message, equalTo(SOME_VALUE));

        //removing preference
        ClientResponse responseDelete1 = preferencesClient.removePreference(SOME_KEY);
        assertThat(responseDelete1.getStatus(), equalTo(ClientResponse.Status.NO_CONTENT.getStatusCode()));

        //reading preference
        ClientResponse responseGet3 = preferencesClient.getPreference(SOME_KEY);
        assertThat(responseGet3.getStatus(), equalTo(ClientResponse.Status.NOT_FOUND.getStatusCode()));
    }


    private class CurrentUserPreferencesClient extends RestApiClient<CurrentUserPreferencesClient>
    {
        private Set<ClientResponse> responses = Sets.newHashSet();

        protected CurrentUserPreferencesClient(JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        private ClientResponse getPreference(final String key)
        {
            WebResource webResource = createResource().path(MYPREFERENCES);
            if (key != null)
            {
                webResource = webResource.queryParam(KEY, key);
            }
            final ClientResponse clientResponse = webResource.get(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse setPreference(final String key, final String value)
        {
            WebResource webResource = createResource().path(MYPREFERENCES);
            if (key != null)
            {
                webResource = webResource.queryParam(KEY, key);
            }
            final ClientResponse put = webResource.type("application/json").put(ClientResponse.class, value);
            responses.add(put);
            return put;
        }

        private ClientResponse removePreference(final String key)
        {
            WebResource webResource = createResource().path(MYPREFERENCES);
            if (key != null)
            {
                webResource = webResource.queryParam(KEY, key);
            }
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
