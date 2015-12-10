package com.atlassian.jira.pageobjects.config.junit4.rule;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.config.SimpleJiraSetup;

import org.apache.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Setups JIRA if needed.
 *
 * @since v6.1
 */
public class SetupJiraRule implements TestRule
{
    private static final Logger log = Logger.getLogger(SetupJiraRule.class);

    private final Backdoor backdoor;
    private final SimpleJiraSetup jiraSetup;

    @Inject
    public SetupJiraRule(final Backdoor backdoor, final SimpleJiraSetup jiraSetup)
    {
        this.backdoor = backdoor;
        this.jiraSetup = jiraSetup;
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                log.debug("Checking if JIRA is setup");
                if (!backdoor.dataImport().isSetUp())
                {
                    log.debug("Setting up JIRA");
                    jiraSetup.performSetUp();
                }
                base.evaluate();
            }
        };
    }
}
