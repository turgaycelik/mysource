package com.atlassian.jira.plugin;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

/**
 * Store Persistyent Plugin State in it's own table
 *
 * @since v6.1
 */
public class OfBizPluginPersistentStateStore
{
    private static final String ENTITY = "PluginState";

    private static final String KEY = "key";
    private static final String ENABLED = "enabled";

    private final OfBizDelegator ofBizDelegator;

    public OfBizPluginPersistentStateStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public void updateState(Map<String, Boolean> state)
    {
        ofBizDelegator.storeAll(fromStateMap(state));
    }

    public void deleteState(Map<String, Boolean> state)
    {
        ofBizDelegator.removeAll(fromStateMap(state));
    }

    public Map<String, Boolean> getState()
    {
        return fromGVs(ofBizDelegator.findAll(ENTITY));
    }

    public Map<String, Boolean> getStateFor(String prefix)
    {
        return(fromGVs(ofBizDelegator.findByLike(ENTITY, ImmutableMap.of(KEY, prefix+"%"))));
    }

    private List<GenericValue> fromStateMap(Map<String, Boolean> stateMap)
    {
        final List<GenericValue> gvs = Lists.newArrayList();
        for (Map.Entry<String, Boolean> entry : stateMap.entrySet())
        {
            gvs.add(fromEntry(entry));
        }
        return gvs;
    }

    private GenericValue fromEntry(Map.Entry <String, Boolean> entry)
    {
        Map<String, Object> fields = ImmutableMap. <String, Object>of(KEY, entry.getKey(), ENABLED, entry.getValue().toString());
        return ofBizDelegator.makeValue(ENTITY, fields);
    }

    private Map<String,Boolean> fromGVs(final List<GenericValue> gvs)
    {
        final Map<String, Boolean> stateMap = Maps.newHashMap();
        for (GenericValue gv : gvs)
        {
            stateMap.put(gv.getString(KEY), Boolean.valueOf(gv.getString(ENABLED)));
        }
        return stateMap;
    }

}
