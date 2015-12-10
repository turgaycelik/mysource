package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Remove the VCS Update Service
 *
 * @since v6.1
 */
public class UpgradeTask_Build6122 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6122.class);

    public UpgradeTask_Build6122()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6122";
    }

    @Override
    public String getShortDescription()
    {
        return "Remove the VCS Update Service, now managed inside the jira-cvs-plugin.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        OfBizDelegator delegator = getOfBizDelegator();

        List<GenericValue> serviceConfigs = delegator.findByAnd("ServiceConfig", EasyMap.build("name", "VCS Update Service"));

        if (!serviceConfigs.isEmpty())
        {
            if (serviceConfigs.size() > 1)
            {
                // More than one should not be present
                log.error("Should not be more than one VCS Update Service present.");
                return;
            }
            GenericValue serviceConfigGV = serviceConfigs.get(0);

            log.info("Removing VCS Update Service with id: " + serviceConfigGV.getLong("id"));

            OFBizPropertyUtils.removePropertySet(serviceConfigGV);
            serviceConfigGV.remove();

            // Feels dirty to be clearing the cache this way, following pattern set by UpgradeTask_Build6084 / UpgradeTask_Build6040.
            ServiceManager component = ComponentAccessor.getComponent(ServiceManager.class);
            log.info("Cleaning ServiceManager cache");
            component.refreshAll();
        }
    }
}
