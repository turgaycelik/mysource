package com.atlassian.jira.pageobjects.config;

import com.atlassian.pageobjects.ProductInstance;

import com.google.inject.Inject;

/**
 * Detects whether the func test plugin has been installed in tested JIRA instance.
 *
 * @since v4.4
 */
public class FuncTestPluginDetector extends AbstractPluginDetector
{
    @Inject private ProductInstance jiraProduct;

    @Override
    protected boolean checkInstalled()
    {
        return checkInstalledViaGet(jiraProduct.getBaseUrl() + "/rest/func-test/1.0/slomo/default");
    }
}
