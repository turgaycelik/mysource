package com.atlassian.jira.plugin;

import java.util.Collection;
import java.util.Map;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.InitializingComponent;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.plugin.manager.PluginPersistentState;
import com.atlassian.plugin.manager.PluginPersistentStateStore;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

@EventComponent
public class JiraPluginPersistentStateStore implements PluginPersistentStateStore, InitializingComponent
{
    public static final String UPDATE_LOCK_NAME = JiraPluginPersistentStateStore.class.getName() + ".updateLock";
    private final OfBizPluginPersistentStateStore ofBizPluginPersistentStateStore;
    private final ClusterManager clusterManager;
    private final ApplicationProperties applicationProperties;
    private final ClusterLockService lockService;

    private volatile Cache cache;

    public JiraPluginPersistentStateStore(final OfBizPluginPersistentStateStore ofBizPluginPersistentStateStore,
            final ClusterManager clusterManager, final ApplicationProperties applicationProperties, final ClusterLockService lockService)
    {
        this.ofBizPluginPersistentStateStore = ofBizPluginPersistentStateStore;
        this.clusterManager = clusterManager;
        this.applicationProperties = applicationProperties;
        this.lockService = lockService;
        reloadKeys();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        // only refresh the cache from persistent storage if an active node
        if (clusterManager.isActive())
        {
            reloadKeys();
        }
    }

    private void reloadKeys()
    {
        cache = new Cache(ofBizPluginPersistentStateStore.getState());
    }

    /**
     * This method is synchronised so that you don't have two people trying to save plugin state at the same time, and
     * over-writing each others changes.
     */
    public void save(final PluginPersistentState state)
    {
        final MapDifference<String, Boolean> differences = Maps.difference(cache.getState().getMap(), state.getMap());
        if (!differences.areEqual())
        {
            if (clusterManager.isActive())
            {
                ClusterLock lock = lockService.getLockForName(UPDATE_LOCK_NAME);
                lock.lock();
                try
                {
                    //Add new entries.
                    ofBizPluginPersistentStateStore.updateState(differences.entriesOnlyOnRight());

                    //Remove existing entries that no longer exist.
                    ofBizPluginPersistentStateStore.deleteState(differences.entriesOnlyOnLeft());

                    //Update any existing entries.
                    ofBizPluginPersistentStateStore.updateState(getUpdateMap(differences.entriesDiffering()));
                    reloadKeys();
                }
                finally
                {
                    lock.unlock();
                }
            }
            else
            {
                cache = new Cache(state.getMap());
            }
        }
    }

    private Map<String, Boolean> getUpdateMap(final Map<String, MapDifference.ValueDifference<Boolean>> diffMap)
    {
        final Map<String, Boolean> updates = Maps.newHashMap();
        for (Map.Entry<String, MapDifference.ValueDifference<Boolean>> entry : diffMap.entrySet())
        {
            updates.put(entry.getKey(), entry.getValue().rightValue());
        }
        return updates;
    }

    public PluginPersistentState load()
    {
        return cache.getState();
    }

    @Override
    public void afterInstantiation() throws Exception
    {
        //JRADEV-23334 : check for old sate and cache this - as the plugin system comes up before the upgrade task runs
        final Collection<String> stringsWithPrefix = applicationProperties.getStringsWithPrefix(APKeys.GLOBAL_PLUGIN_STATE_PREFIX);
        if (stringsWithPrefix.size() > 0)
        {
            cache = new Cache(stringsWithPrefix);
        }
        //JRADEV-23443 : The migration may have left a . in the front of the key - fix this up
        stripDotsFromPluginKeys();
    }

    public void stripDotsFromPluginKeys()
    {
        final Map<String, Boolean> stateWithDots = ofBizPluginPersistentStateStore.getStateFor(".");
        if (stateWithDots.isEmpty())
        {
            return;
        }
        final Map<String, Boolean> correctedState = Maps.newHashMapWithExpectedSize(stateWithDots.size());
        for (Map.Entry<String, Boolean> entry : stateWithDots.entrySet())
        {
            correctedState.put(entry.getKey().substring(1), entry.getValue());
        }
        ofBizPluginPersistentStateStore.updateState(correctedState);
        ofBizPluginPersistentStateStore.deleteState(stateWithDots);
    }

    class Cache
    {
        private final PluginPersistentState state;

        Cache(final Map<String, Boolean> stateMap)
        {
            state = buildState(stateMap);
        }

        private PluginPersistentState buildState(final Map<String, Boolean> stateMap)
        {
            return PluginPersistentState.Builder.create().addState(stateMap).toState();
        }

        Cache(final Collection<String> keys)
        {
            final int statePrefixIndex = APKeys.GLOBAL_PLUGIN_STATE_PREFIX.length() + 1;
            final Map<String, Boolean> stateMap = Maps.newHashMap();
            for (final String key : keys)
            {
                stateMap.put(key.substring(statePrefixIndex), Boolean.valueOf(applicationProperties.getString(key)));
            }
            state = buildState(stateMap);
        }

        public PluginPersistentState getState()
        {
            return state;
        }
    }
}
