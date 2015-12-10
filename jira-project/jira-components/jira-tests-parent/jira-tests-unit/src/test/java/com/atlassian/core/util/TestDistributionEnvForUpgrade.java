package com.atlassian.core.util;

import java.io.IOException;

import com.atlassian.inception.JiraUpgradeHelper;

import junit.framework.TestCase;

/**
 * This test verifies the expectation the upgrader(installer) has on the format of setenv.sh and setenv.bat.
 * When installer permorms the upgrade it transferes the max/min/pemgen memory and other settings to the upgraded installation.
 * It relies on the paricular format of the file to extract those values so if the format is changed the upgrader will have to change.
 * IF THIS TEST BREAKES PLEASE VERIFY THAT UPGRADER STILL WORKS WHEN FIXING IT.
 *
 * @since v4.4
 */
public class TestDistributionEnvForUpgrade extends TestCase
{
    private JiraUpgradeHelper upgradeHelper;
    private boolean isWindows = false;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        upgradeHelper = new JiraUpgradeHelper("../../../jira-distribution/jira-standalone-distribution/src/main/tomcat/")
        {
            protected boolean isWindows()
            {
                return isWindows;
            }
        };
    }

    public void testMinMemory() throws IOException
    {
        assertEquals("384m", upgradeHelper.getJvmMinMemory());
        isWindows = true;
        assertEquals("384m", upgradeHelper.getJvmMinMemory());
    }

    public void testMaxMemory() throws IOException
    {
        assertEquals("768m", upgradeHelper.getJvmMaxMemory());
        isWindows = true;
        assertEquals("768m", upgradeHelper.getJvmMaxMemory());
    }

    public void testSupportArgs() throws IOException
    {
        assertEquals("", upgradeHelper.getJvmSupportArgs());
        isWindows = true;
        assertEquals("", upgradeHelper.getJvmSupportArgs());
    }

    public void testPermGen() throws IOException
    {
        assertEquals("384m", upgradeHelper.getJvmPermSize());
        isWindows = true;
        assertEquals("384m", upgradeHelper.getJvmPermSize());
    }
}
