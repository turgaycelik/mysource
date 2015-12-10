package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class TestUpgradeTask_Build520 extends MockControllerTestCase
{
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = getMock(ApplicationProperties.class);
    }

    @Test
    public void testGetBuildNumber()
    {
        assertEquals("520", instantiate(UpgradeTask_Build520.class).getBuildNumber());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        applicationProperties.setString("jira.maximum.authentication.attempts.allowed", "3");
        expectLastCall();
        instantiate(UpgradeTask_Build520.class).doUpgrade(false);

    }
}
