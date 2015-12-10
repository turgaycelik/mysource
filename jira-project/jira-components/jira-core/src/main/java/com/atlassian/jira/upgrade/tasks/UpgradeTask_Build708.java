package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Cleans up left over configuration from JIRA issues cache.
 * Need to run this again as all its good work was undone by the consitency checker.
 */
public class UpgradeTask_Build708 extends UpgradeTask_Build605
{

    public UpgradeTask_Build708(PropertiesManager propertiesManager)
    {
        super(propertiesManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "708";
    }
}
