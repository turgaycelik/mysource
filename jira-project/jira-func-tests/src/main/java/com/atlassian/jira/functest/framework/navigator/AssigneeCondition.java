package com.atlassian.jira.functest.framework.navigator;

/**
 * Condition that can be used to interact with the "assignee" navigator UI.
 *
 * @since v4.0
 */
public class AssigneeCondition extends UserGroupPicker
{
    public AssigneeCondition()
    {
        super("assignee");
    }

    public AssigneeCondition setNoReporter()
    {
        setEmpty();
        return this;
    }
}
