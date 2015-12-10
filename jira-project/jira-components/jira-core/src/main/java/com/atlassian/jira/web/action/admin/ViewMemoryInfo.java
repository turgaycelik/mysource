package com.atlassian.jira.web.action.admin;

import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformation;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.util.system.JiraRuntimeInformationFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * View extended memory information
 *
 * @since v4.0
 */
@WebSudoRequired
public class ViewMemoryInfo extends ViewSystemInfo
{
    private final RuntimeInformation runtimeInformation = JiraRuntimeInformationFactory.getRuntimeInformationInMegabytes();

    public ViewMemoryInfo(LocaleManager localeManager, final PluginInfoProvider pluginInfoProvider, final FeatureManager featureManager)
    {
        super(localeManager, pluginInfoProvider, featureManager);
    }

    public RuntimeInformation getRuntimeInformation()
    {
        return runtimeInformation;
    }
}
