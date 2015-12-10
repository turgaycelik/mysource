package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.mock.MockApplicationProperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestUpgradeTask_Build509
{
    @Test
    public void testBuildNumberAndDescription()
    {
        final UpgradeTask_Build509 upgradeTask = new UpgradeTask_Build509(null);
        assertEquals("509", upgradeTask.getBuildNumber());
        assertEquals("Upgrades link, active link and text color to new default of #3c78b5.", upgradeTask.getShortDescription());
    }

    @Test
    public void testUpgradeColorNotSet() throws Exception
    {
        final MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        final UpgradeTask_Build509 upgradeTask = new UpgradeTask_Build509(mockApplicationProperties);

        assertNull(mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR));
        assertNull(mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertNull(mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR));
        upgradeTask.doUpgrade(false);

        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR));
        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR));
    }

    @Test
    public void testUpgradeColorOnOldDefault() throws Exception
    {
        final MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        final UpgradeTask_Build509 upgradeTask = new UpgradeTask_Build509(mockApplicationProperties);
        mockApplicationProperties.setString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, "#003366");
        mockApplicationProperties.setString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, "#003366");
        mockApplicationProperties.setString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, "#003366");

        assertEquals("#003366", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR));
        assertEquals("#003366", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertEquals("#003366", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR));

        upgradeTask.doUpgrade(false);

        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR));
        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR));
    }

    @Test
    public void testUpgradeColor() throws Exception
    {
        final MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        final UpgradeTask_Build509 upgradeTask = new UpgradeTask_Build509(mockApplicationProperties);
        mockApplicationProperties.setString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, "#003366");
        mockApplicationProperties.setString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, "#ffeecc");
        mockApplicationProperties.setString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, "#222222");

        assertEquals("#003366", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR));
        assertEquals("#ffeecc", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertEquals("#222222", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR));

        upgradeTask.doUpgrade(false);

        assertEquals("#3c78b5", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR));
        assertEquals("#ffeecc", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertEquals("#222222", mockApplicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR));
    }
    
}
