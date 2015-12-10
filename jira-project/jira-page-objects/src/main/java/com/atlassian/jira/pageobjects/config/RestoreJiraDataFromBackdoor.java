package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.testkit.client.Backdoor;

import org.apache.log4j.Logger;

/**
 * Implementation of {@link RestoreJiraData} that uses the func test plugin REST resource if present
 */
public class RestoreJiraDataFromBackdoor implements RestoreJiraData
{
    private static final Logger LOGGER = Logger.getLogger(RestoreJiraDataFromBackdoor.class);

    private final Backdoor testkit;

    public RestoreJiraDataFromBackdoor(final JiraTestedProduct jiraProduct)
    {
        this.testkit = new Backdoor(jiraProduct.environmentData());
    }

    @Override
    public void restore(final String resourcePath)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(String.format("Restoring '%s' via backdoor", resourcePath));
        }
        testkit.restoreDataFromResource(resourcePath);
    }

    @Override
    public void restoreBlank()
    {
        LOGGER.debug("Restoring the blank instance via backdoor");
        testkit.restoreBlankInstance();
    }
}
