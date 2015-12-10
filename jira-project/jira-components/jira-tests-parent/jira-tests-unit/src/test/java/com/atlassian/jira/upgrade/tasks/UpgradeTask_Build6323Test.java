package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

public class UpgradeTask_Build6323Test
{
    @Rule
    public TestRule rule = MockitoMocksInContainer.rule(this);

    @AvailableInContainer
    @Mock
    private OfBizDelegator delegator;

    @Test
    public void updateRunsRightSqlQuery()
    {
        final UpgradeTask_Build6323 build6323 = new UpgradeTask_Build6323();
        build6323.doUpgrade(false);

        verify(delegator).removeByAnd("PluginState",
                ImmutableMap.<String, Object>of("key", "com.atlassian.jira.plugin.system.licenseroles"));

        verify(delegator).removeByAnd("PluginState",
                ImmutableMap.<String, Object>of("key", "com.atlassian.jira.plugin.system.licenseroles:businessUser"));
    }
}