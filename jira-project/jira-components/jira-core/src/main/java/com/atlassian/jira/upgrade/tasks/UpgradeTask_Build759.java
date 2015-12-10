package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * This upgrade task is used to enable GZIP compression by default for new installations only. This works by explicitly
 * setting GZIP to OFF for existing are relying on the default being OFF, since the new default is ON. JRADEV-8940.
 *
 * @since v5.1
 */
public class UpgradeTask_Build759 extends AbstractUpgradeTask
{
    private final PropertiesManager propertiesManager;

    public UpgradeTask_Build759(PropertiesManager propertiesManager)
    {
        super(false);
        this.propertiesManager = propertiesManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "759";
    }

    @Override
    public String getShortDescription()
    {
        return "enable GZIP compression by default for new installations only";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // if the user has not explicitly configured GZIP compression then we explicitly set it to OFF at this
        // point (which was the previous default). this prevents the new default of ON from taking effect in existing
        // installations.
        PropertySet jiraProperties = propertiesManager.getPropertySet();
        if (!jiraProperties.exists(APKeys.JIRA_OPTION_WEB_USEGZIP))
        {
            jiraProperties.setBoolean(APKeys.JIRA_OPTION_WEB_USEGZIP, false);
        }
    }
}
