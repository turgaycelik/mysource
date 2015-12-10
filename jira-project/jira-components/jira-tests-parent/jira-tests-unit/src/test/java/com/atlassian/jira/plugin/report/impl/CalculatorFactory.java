package com.atlassian.jira.plugin.report.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactoryImpl;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;

/**
 * Handy for tests.
 */
class CalculatorFactory
{
    static AggregateTimeTrackingCalculatorFactory get(String userName)
    {
        User user = new MockUser(userName);
        JiraAuthenticationContext authContext = new MockAuthenticationContext(user);
        PermissionManager perms = new MockPermissionManager();
        return new AggregateTimeTrackingCalculatorFactoryImpl(authContext, null, perms);
    }
}