package com.atlassian.jira.upgrade;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6124;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith (MockitoJUnitRunner.class)
public class TestUpgradeTask_Build6124
{
    private UpgradeTask_Build6124 upgradeTask;

    @Mock private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        upgradeTask = new UpgradeTask_Build6124(applicationProperties);
    }

    @Test
    public void testUnassignedAllowAfterInstall() throws Exception
    {
        upgradeTask.doUpgrade(true);

        verify(applicationProperties).setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
    }

    @Test
    public void testUnassignedNotAllowedAfterUpgrade() throws Exception
    {
        upgradeTask.doUpgrade(false);

        verifyNoMoreInteractions(applicationProperties);
    }
}
