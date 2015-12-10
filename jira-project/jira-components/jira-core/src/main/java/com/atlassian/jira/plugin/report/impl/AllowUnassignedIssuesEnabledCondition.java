package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.EnabledCondition;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;

/**
 * EnabledCondition that checks whether Issues are allowed to be Unassigned or not.
 *
 * @since v3.11
 */
public class AllowUnassignedIssuesEnabledCondition implements EnabledCondition
{
    public boolean isEnabled()
    {
        return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }
}
