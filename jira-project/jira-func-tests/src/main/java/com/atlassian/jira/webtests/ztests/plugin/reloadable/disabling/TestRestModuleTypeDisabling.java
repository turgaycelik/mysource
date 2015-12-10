package com.atlassian.jira.webtests.ztests.plugin.reloadable.disabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

/**
 * <p>Responsible for verifying that the rest resources defined within a plugin are disabled correctly.</p>
 *
 * <p>After being disabled, the rest resource should not be reachable by any request made to it.</p>
 *
 * <p>This scenario assumes that the module has never been enabled and that the plugin is loaded in a
 * disabled state when JIRA starts up.</p>
 *
 * <p>This is also what we call the from ON to OFF scenario.</p>
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestRestModuleTypeDisabling extends AbstractReloadablePluginsTest
{
    public void testShouldNotBeReachableAfterBeingDisabled() throws JSONException
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().restResources().endPoint().get();
        administration.plugins().referencePlugin().restResources().disable();

        assertFalse(administration.plugins().referencePlugin().restResources().endPoint().isReachable());
    }

    public void testShouldNotBeReachableAfterDisablingTheReferencePlugin() throws JSONException
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().restResources().endPoint().get();
        administration.plugins().referencePlugin().disable();

        assertFalse(administration.plugins().referencePlugin().restResources().endPoint().isReachable());
    }

    /**
     * Tries to enable the <strong>whole plugin</strong> after disabling it and then assert that the resource is
     * reachable again and returns the expected response (it has been "reloaded").
     *
     * @throws com.atlassian.jira.util.json.JSONException If the response from the reference rest resource contains.
     * malformed JSON.
     */
    public void testShouldBeReachableAfterEnablingTheReferencePluginBackAgain() throws JSONException
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().restResources().endPoint().get();
        administration.plugins().referencePlugin().disable();
        administration.plugins().referencePlugin().enable();

        assertTrue(administration.plugins().referencePlugin().restResources().endPoint().isReachable());

        final JSONObject endpointJSONResponse =
                administration.plugins().referencePlugin().restResources().endPoint().get();

        assertEquals(endpointJSONResponse.get("endpoint"), false);
    }

    /**
     * Tries to enable the rest resources <strong>module</strong> after disabling it and then assert that the resource
     * is reachable again and returns the expected response (it has been "reloaded").
     *
     * @throws com.atlassian.jira.util.json.JSONException If the response from the reference rest resource contains.
     * malformed JSON.
     */
    public void testShouldBeReachableAfterEnablingItBackAgain() throws JSONException
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().restResources().endPoint().get();
        administration.plugins().referencePlugin().restResources().disable();
        administration.plugins().referencePlugin().restResources().enable();

        assertTrue(administration.plugins().referencePlugin().restResources().endPoint().isReachable());

        final JSONObject endpointJSONResponse =
                administration.plugins().referencePlugin().restResources().endPoint().get();

        assertEquals(endpointJSONResponse.get("endpoint"), false);
    }
}
