package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.restclient.DarkFeature;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SiteDarkFeaturesClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Client for the site dark features resource with support for batch updates.
 *
 * @since v6.0
 */
public class SiteDarkFeaturesClientExt extends SiteDarkFeaturesClient
{
    private static final GenericType<Map<String, DarkFeature>> MAP_TYPE = new GenericType<Map<String, DarkFeature>>(HashMap.class);

    /**
     * Constructs a new SiteDarkFeaturesClientExt for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public SiteDarkFeaturesClientExt(final JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Enables or disables a set of dark features.
     *
     * @param featuresMap a Map of dark feature name to enabled state
     * @return a Set containing the keys of all enabled dark features
     */
    public DarkFeatures post(Map<String, DarkFeature> featuresMap)
    {
        return siteDarkFeatures().type(APPLICATION_JSON_TYPE).put(DarkFeatures.class, featuresMap);
    }

    /**
     * Enables or disables a set of dark features and returns the response object.
     *
     * @return a Set containing the keys of all enabled dark features
     */
    public Response<?> postResponse(final Map<String, Boolean> featuresMap)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return siteDarkFeatures().type(APPLICATION_JSON_TYPE).put(ClientResponse.class, featuresMap);
            }
        });
    }
}
