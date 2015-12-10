package com.atlassian.sal.jira.upgrade;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Fixing the classname for services using JiraPluginSchedulerService class
 */
public class UpgradeTo_v2 implements PluginUpgradeTask
{
    private static final Logger log = Logger.getLogger(PluginUpgradeTask.class);
    private static final String OLD_CLASS_NAME = "com.atlassian.sal.jira.scheduling.JiraPluginScheduler$JiraPluginSchedulerService";
    private static final String NEW_CLASS_NAME = "com.atlassian.sal.jira.scheduling.JiraPluginSchedulerService";

    public int getBuildNumber()
    {
        return 2;
    }

    public String getShortDescription()
    {
        return "Fixing the classname for services using JiraPluginSchedulerService class";
    }

    public Collection<Message> doUpgrade() throws Exception
    {
        final DelegatorInterface genericDelegator = ComponentLocator.getComponent(DelegatorInterface.class);
        final List<GenericValue> services = genericDelegator.findByAnd("ServiceConfig", EasyMap.build("clazz", OLD_CLASS_NAME));
        if (services.isEmpty())
        {
            log.info("No JiraPluginSchedulerService found. No classname fixing required");
        }

        for (final GenericValue service : services)
        {
            service.set("clazz", NEW_CLASS_NAME);
            log.info("Fixing classname for service " + service.getString("name"));
        }
        genericDelegator.storeAll(services);
        return null;
    }

    public String getPluginKey()
    {
        return JiraPluginUpgradeManager.SAL_PLUGIN_KEY;
    }
}
