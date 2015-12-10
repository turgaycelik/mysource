package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

/**
 * Cleans up left over application properties
 */
public class UpgradeTask_Build606 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build606.class);

    private final PropertiesManager propertiesManager;

    public UpgradeTask_Build606(PropertiesManager propertiesManager)
    {
        super(false);
        this.propertiesManager = propertiesManager;
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        removeOption(APKeys.JIRA_PATH_BACKUP);

        log.debug("Removed deprecated application properties");
    }

    private void removeOption(String key)
    {
        PropertySet ps = propertiesManager.getPropertySet();
        if (ps.exists(key))
        {
            ps.remove(key);
        }
    }

    @Override
    public String getBuildNumber()
    {
        return "606";
    }

    @Override
    public String getShortDescription()
    {
        return "Cleans up left over application properties";
    }
}
