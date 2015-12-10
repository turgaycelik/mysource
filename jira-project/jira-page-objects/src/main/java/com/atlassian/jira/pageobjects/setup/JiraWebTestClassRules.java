package com.atlassian.jira.pageobjects.setup;

import javax.inject.Inject;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.junit4.rule.RestoreDataClassRule;
import com.atlassian.jira.pageobjects.config.junit4.rule.RuleChainBuilder;
import com.atlassian.jira.pageobjects.config.junit4.rule.SetupJiraRule;

import static com.atlassian.jira.pageobjects.config.junit4.rule.RuleChainBuilder.Conditionally.around;

/**
 * Provides class-level rules chain for web test.
 *
 * @since v6.1
 */
public class JiraWebTestClassRules implements TestRule
{

    private final RuleChain ruleChain;

    @Inject
    public JiraWebTestClassRules(final JiraTestedProduct jira)
    {
        if (jira.shouldSkipSetup())
        {
            ruleChain = RuleChainBuilder.forProduct(jira).build();
        }
        else
        {
            ruleChain = RuleChainBuilder.forProduct(jira)
                    .around(SetupJiraRule.class)
                    .around(RestoreDataClassRule.class)
                    .build();
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return ruleChain.apply(base, description);
    }
}
