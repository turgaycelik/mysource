package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.jira.pageobjects.framework.util.DirtyWarningTerminator;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.inject.Inject;

/**
 * Test rule to eliminate dirty warnings
 *
 * @since 5.1
 */
public class DirtyWarningTerminatorRule extends TestWatcher implements TestRule
{
    private final DirtyWarningTerminator terminator;

    @Inject
    public DirtyWarningTerminatorRule(DirtyWarningTerminator terminator)
    {
        this.terminator = terminator;
    }

    @Override
    protected void finished(Description description)
    {
        terminator.htfuDirtyWarnings();
    }
}
