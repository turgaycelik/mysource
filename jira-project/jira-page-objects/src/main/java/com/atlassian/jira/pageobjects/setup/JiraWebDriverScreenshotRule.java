package com.atlassian.jira.pageobjects.setup;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.TestEnvironment;
import com.atlassian.webdriver.Drivers;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;

import javax.inject.Inject;

/**
 * An extension of the screenshot rule that gets the target dir from
 * {@link com.atlassian.jira.pageobjects.config.TestEnvironment}.
 *
 * @since 2.1
 */
@Internal
public class JiraWebDriverScreenshotRule extends WebDriverScreenshotRule
{

    @Inject
    public JiraWebDriverScreenshotRule(JiraTestedProduct product, TestEnvironment environment)
    {
        super(Drivers.fromProduct(product), environment.artifactDirectory());
    }
}
