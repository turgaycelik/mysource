package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

import java.io.IOException;

/**
 * <p>Responsible for verifying that a rest endpoint defined withing a plugin works as expected after the module has
 * been enabled. This scenario assumes that the module has never been enabled and that the plugin is loaded in a
 * disabled state when JIRA starts up.</p>
 * <br/>
 * <p>This is also what we call the from ZERO to ON scenario.</p>
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestRestModuleEnabling extends AbstractReloadablePluginsTest
{
    private static final String REFERENCE_PLUGIN_REST_ENDPOINT_URL = "/rest/reference-plugin/1.0/endpoint";

    public void testShouldNotExistAndBeAccessibleBeforeEnablingThePlugin() throws Exception
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

        tester.gotoPage(REFERENCE_PLUGIN_REST_ENDPOINT_URL);

        assertEquals(tester.getDialog().getResponse().getResponseCode(), NoSuchEndpoint.CODE);
    }

    public void testShouldBeReachableAfterEnablingTheReferencePlugin() throws IOException, JSONException
    {
        administration.plugins().referencePlugin().enable();

        tester.gotoPage(REFERENCE_PLUGIN_REST_ENDPOINT_URL);

        final JSONObject endpointJSONResponse = new JSONObject(tester.getDialog().getResponse().getText());
        assertEquals(endpointJSONResponse.get("endpoint"), false);
    }

    private static class NoSuchEndpoint
    {
        public static final int CODE = 404;
    }
}
