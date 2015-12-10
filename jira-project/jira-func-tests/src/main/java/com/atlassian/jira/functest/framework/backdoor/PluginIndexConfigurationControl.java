package com.atlassian.jira.functest.framework.backdoor;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;

public class PluginIndexConfigurationControl extends BackdoorControl<PluginIndexConfigurationControl>
{
    public PluginIndexConfigurationControl(final JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<String> getDocumentsForEntity(final String entityKey)
    {
        return createResource().path("plugin-index-configuration")
                .queryParam("entityKey", entityKey)
                .get(ArrayList.class);
    }

    public void putDocumentConfiguration(final String pluginKey, final String moduleKey, final String configuration)
    {
        createResource().path("plugin-index-configuration")
                .queryParam("pluginKey", pluginKey)
                .queryParam("moduleKey", moduleKey)
                .header("Content-type", "application/json")
                .entity(configuration)
                .put();
    }
}
