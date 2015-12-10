package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.util.json.JSONObject;
import com.sun.jersey.api.client.WebResource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Put here anything you need in Backdoor/TestKit. Later it will be promoted to the official package.
 * @since v5.2
 */
public class EntityEngineControl extends BackdoorControl<EntityEngineControl>
{
    public EntityEngineControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<Map<String, String>> findByAnd(final String entityName, final Map restriction)
    {
        WebResource.Builder resource = createResource().path("entityEngine").path("findByAnd").queryParam("entity", entityName).type(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE);
        return resource.post(List.class, restriction);
    }
}
