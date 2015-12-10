package com.atlassian.jira.workflow.condition;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

/**
 * A workflow condition that will always fail.
 *
 * @since 6.3.3
 */
public class AlwaysFalseCondition extends AbstractJiraCondition
{
    @Override
    public boolean passesCondition(final Map transientVars, final Map args, final PropertySet ps)
            throws WorkflowException
    {
        return false;
    }
}
