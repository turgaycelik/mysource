package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

/**
 * Context Provider that provides the xsrf token for the request.
 * Referenced by "${atl_token}" or ${atl_token_escaped} for the URL escaped version.
 *
 * @since v4.4
 */
public class XsrfTokenContextProvider implements ContextProvider
{
    private final XsrfTokenGenerator xsrfTokenGenerator;

    public XsrfTokenContextProvider(XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.xsrfTokenGenerator = xsrfTokenGenerator;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);
        paramsBuilder.add("atl_token", xsrfTokenGenerator.generateToken());
        paramsBuilder.add("atl_token_escaped", JiraUrlCodec.encode(xsrfTokenGenerator.generateToken()));

        return paramsBuilder.toMap();

    }
}
