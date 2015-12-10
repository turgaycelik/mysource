package com.atlassian.jira.pageobjects.config;

import com.atlassian.pageobjects.ProductInstance;

import com.google.inject.Inject;

/**
 * Detects whether the jira-testkit-plugin is installed in tested JIRA instance.
 *
 * @since v6.1
 */
public class TestkitPluginDetector extends AbstractPluginDetector
{
    @Inject
    private ProductInstance jiraProduct;

    @Override
    protected boolean checkInstalled()
    {
        return checkInstalledViaGet(jiraProduct.getBaseUrl() + "/rest/testkit-test/1.0/dataImport/importConfig");
    }
}
