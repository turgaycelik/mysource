package com.atlassian.jira.functest.rules;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Clean up attachments directory before and after the test case.
 *
 * @since v6.1
 */
public class EnableAttachmentsRule implements TestRule
{
    protected final Backdoor backdoor;
    protected final JIRAEnvironmentData environmentData;

    public EnableAttachmentsRule(FuncTestCase testCase)
    {
        this.environmentData = testCase.getEnvironmentData();
        this.backdoor = new Backdoor(environmentData);
    }

    public EnableAttachmentsRule(final Backdoor backdoor)
    {
        this.environmentData = new LocalTestEnvironmentData();
        this.backdoor = backdoor;
    }

    public void before()
    {
        backdoor.attachments().enable();
    }

    public void after()
    {
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }


}
