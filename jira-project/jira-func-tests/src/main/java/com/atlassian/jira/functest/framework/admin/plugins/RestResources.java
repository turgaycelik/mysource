package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import net.sourceforge.jwebunit.WebTester;

import java.io.IOException;

/**
 * Represents the rest resources included in the reference plugin.
 *
 * @since 4.4
 */
public class RestResources extends ReferencePluginModule
{
    private static final String MODULE_KEY = "reference-plugin-resources";
    private static final String MODULE_NAME = "Reference REST endpointless resource";

    private final WebTester tester;
    private final EndPoint endPoint;

    public RestResources(WebTester tester, Administration administration)
    {
        super(administration);
        this.tester = tester;
        this.endPoint = new EndPoint(tester);
    }

    @Override
    public String moduleKey()
    {
        return MODULE_KEY;
    }

    @Override
    public String moduleName()
    {
        return MODULE_NAME;
    }

    public EndPoint endPoint()
    {
        return endPoint;
    }

    /**
     * Represents the "endpoint" resource included in the reference plugin.
     */
    public class EndPoint
    {
        private static final String URL = "/rest/reference-plugin/1.0/endpoint";

        private final WebTester tester;

        public EndPoint(final WebTester tester)
        {
            this.tester = tester;
        }

        public String url()
        {
            return URL;
        }

        /**
         * Invoke the GET HTTP method on this resource.
         * @return the JSON object in the response.
         * @throws com.atlassian.jira.util.json.JSONException If the JSON is malformed.
         */
        public JSONObject get() throws JSONException
        {
            try
            {
                tester.gotoPage(url());
                return new JSONObject(tester.getDialog().getResponse().getText());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        public boolean isReachable()
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

            tester.gotoPage(url());

            return tester.getDialog().getResponse().getResponseCode() != UnReachableResource.RESPONSE_CODE;
        }
    }

    private class UnReachableResource
    {
        public static final int RESPONSE_CODE = 404;
    }
}
