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
 * Cleans up left over configuration from JIRA issues cache
 */
public class UpgradeTask_Build605 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build605.class);

    static final String JIRA_OPTION_CACHE_PROJECTS = "jira.option.cache.projects";
    static final String JIRA_OPTION_CACHE_PERMISSIONS = "jira.option.cache.permissions";
    static final String JIRA_OPTION_CACHE_ISSUES = "jira.option.cache.issues";
    public static final String ISSUE_CACHE_LISTENER_CLASS = "com.atlassian.jira.event.listeners.cache.IssueCacheListener";

    private final PropertiesManager propertiesManager;

    public UpgradeTask_Build605(PropertiesManager propertiesManager)
    {
        super(false);
        this.propertiesManager = propertiesManager;
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        final Collection<GenericValue> listenerConfigs = getOfBizDelegator().findAll("ListenerConfig");
        final List<GenericValue> toRemove = new ArrayList<GenericValue>();

        for (final GenericValue listenerConfig : listenerConfigs)
        {
            if (listenerConfig.getString("clazz").equals(ISSUE_CACHE_LISTENER_CLASS))
            {
                toRemove.add(listenerConfig);
            }
        }
        getOfBizDelegator().removeAll(toRemove);

        removeOption(JIRA_OPTION_CACHE_ISSUES);
        // These two aren't used anymore either
        removeOption(JIRA_OPTION_CACHE_PERMISSIONS);
        removeOption(JIRA_OPTION_CACHE_PROJECTS);
        
        log.debug("Removed JIRA issue cache configuration and listener");
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
        return "605";
    }

    @Override
    public String getShortDescription()
    {
        return "Cleans up left over configuration from JIRA issues cache";
    }
}
