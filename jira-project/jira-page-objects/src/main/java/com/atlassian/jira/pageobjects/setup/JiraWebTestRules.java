package com.atlassian.jira.pageobjects.setup;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.junit4.rule.CreateUserRule;
import com.atlassian.jira.pageobjects.config.junit4.rule.DirtyWarningTerminatorRule;
import com.atlassian.jira.pageobjects.config.junit4.rule.LoginRule;
import com.atlassian.jira.pageobjects.config.junit4.rule.RestoreDataMethodRule;
import com.atlassian.jira.pageobjects.config.junit4.rule.RuleChainBuilder;
import com.atlassian.jira.pageobjects.config.junit4.rule.WebSudoRule;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.SessionCleanupRule;
import com.atlassian.webdriver.testing.rule.WindowSizeRule;

import org.junit.rules.RuleChain;

import static com.atlassian.jira.pageobjects.config.junit4.rule.RuleChainBuilder.Conditionally.around;

/**
 * Default set of rules for JIRA product web tests.
 *
 * @since 5.1
 */
@Internal
public final class JiraWebTestRules
{
    private JiraWebTestRules()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static RuleChain forJira(JiraTestedProduct jira)
    {
        return RuleChainBuilder.forProduct(jira)
                // before-test
                .around(WindowSizeRule.class)
                .around(SessionCleanupRule.class)
                .around(RestoreDataMethodRule.class)
                .when(!jira.shouldSkipSetup(), around(WebSudoRule.class))
                .around(CreateUserRule.class)
                .around(LoginRule.class)
                        // after-test
                .around(JiraWebDriverScreenshotRule.class)
                .around(DirtyWarningTerminatorRule.class)
                .around(new LogPageSourceRule(jira.getTester().getDriver(), JiraWebTestLogger.LOGGER))
                .build();
    }
}
