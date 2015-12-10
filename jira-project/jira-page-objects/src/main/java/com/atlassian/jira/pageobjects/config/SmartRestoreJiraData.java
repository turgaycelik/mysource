package com.atlassian.jira.pageobjects.config;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.JiraTestedProduct;

/**
 * Delegates restore action to the best available implementation.
 *
 * @since 6.1
 */
public class SmartRestoreJiraData implements RestoreJiraData
{
    @Inject private JiraTestedProduct jiraProduct;
    @Inject private TestkitPluginDetector pluginDetector;
    @Inject private JiraConfigProvider jiraConfigProvider;
    @Inject private Backdoor backdoor;
    @Inject private SimpleJiraSetup jiraSetup;

    private RestoreJiraData restoreJiraData;

    private RestoreJiraData getDelegate()
    {
        if (restoreJiraData == null) {
            if (pluginDetector.isInstalled())
            {
                restoreJiraData = new RestoreJiraDataFromBackdoor(jiraProduct);
            }
            else
            {
                restoreJiraData = new RestoreJiraDataFromUi(jiraProduct, jiraConfigProvider);
            }
        }
        return restoreJiraData;
    }

    @Override
    public void restore(final String resourcePath)
    {
        getDelegate().restore(resourcePath);
    }

    @Override
    public void restoreBlank()
    {
        getDelegate().restoreBlank();
    }
}
