package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Migrate PluginPersistentState to the PluginState table
 *
 * @since v6.1
 */
public class UpgradeTask_Build6133 extends AbstractUpgradeTask
{


    private static final String PLUGIN_STATE_ENTITY="PluginState";


    public UpgradeTask_Build6133()
    {
        super(false);
    }

    @Override
    public String getShortDescription()
    {
        return "Migrate PluginPersistentState to the PluginState table";
    }

    @Override
    public String getBuildNumber()
    {
        return "6133";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        Collection<String> pluginStateKeys = getApplicationProperties().getStringsWithPrefix(APKeys.GLOBAL_PLUGIN_STATE_PREFIX);
        doMigrate(pluginStateKeys);
        removeKeysFromPropertiesTable(pluginStateKeys);
    }

    private void doMigrate(final Collection<String> pluginStateKeys)
    {
        try
        {
            storeGenericValue(pluginStateKeys);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void storeGenericValue(final Collection<String> pluginStateKeys) throws GenericEntityException
    {
        List<GenericValue> gvs = Lists.newArrayList();
        final String target = APKeys.GLOBAL_PLUGIN_STATE_PREFIX+".";
        for (String pluginStateKey : pluginStateKeys) {
            final GenericValue gv =  getOfBizDelegator().makeValue(PLUGIN_STATE_ENTITY);
            gv.set("key", pluginStateKey.replace(target, ""));
            gv.set("enabled", getApplicationProperties().getString(pluginStateKey));
            gvs.add(gv);
        }
        getOfBizDelegator().storeAll(gvs);
    }

    private void removeKeysFromPropertiesTable(final Collection<String> pluginStateKeys)
    {
        for (final String key : pluginStateKeys)
        {
            getApplicationProperties().setString(key, null);
        }
    }


}
