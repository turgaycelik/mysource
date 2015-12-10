package com.atlassian.jira.functest.framework.navigator;

/**
 * Condition that can be used to interact with the "reporter" navigator UI.
 *
 * @since v4.0
 */
public class ReporterCondition extends UserGroupPicker
{
    public ReporterCondition()
    {
        super("reporter");
    }

    public ReporterCondition setNoReporter()
    {
        setEmpty();
        return this;
    }
}
