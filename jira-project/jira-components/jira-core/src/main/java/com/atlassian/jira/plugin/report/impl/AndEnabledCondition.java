package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.EnabledCondition;
import com.atlassian.jira.util.dbc.Null;

/**
 * Composite AND of two EnabledConditions.
 *
 * @since v3.11
 */
public class AndEnabledCondition implements EnabledCondition
{
    private final EnabledCondition condition1;
    private final EnabledCondition condition2;

    public AndEnabledCondition(EnabledCondition condition1, EnabledCondition condition2)
    {
        this.condition1 = condition1;
        this.condition2 = condition2;

        Null.not("condition1", condition1);
        Null.not("condition2", condition2);
    }

    public boolean isEnabled()
    {
        return condition1.isEnabled() && condition2.isEnabled();
    }
}
