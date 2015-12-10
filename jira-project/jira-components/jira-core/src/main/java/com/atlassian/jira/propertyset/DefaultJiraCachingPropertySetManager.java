package com.atlassian.jira.propertyset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.event.ClearCacheEvent;

import com.google.common.annotations.VisibleForTesting;

import static java.util.Collections.newSetFromMap;

/**
 * Default implementation of {@link JiraCachingPropertySetManager}.
 */
@EventComponent
public class DefaultJiraCachingPropertySetManager implements JiraCachingPropertySetManager
{
    // Fields
    // -- Using a set is OK because JiraCachingPropertySet uses object identity for its equals() method
    @ClusterSafe("This class is basically not cluster safe anyway and is not / should not be used directly ")
    private final Collection<JiraCachingPropertySet> propertySets =
            newSetFromMap(new WeakHashMap<JiraCachingPropertySet, Boolean>());

    @Override
    @ClusterSafe("This class is basically not cluster safe anyway and is not / should not be used directly ")
    synchronized public void register(final JiraCachingPropertySet propertySet)
    {
        if (propertySet != null)
        {
            propertySets.add(propertySet);
        }
    }

    @EventListener
    @ClusterSafe("This class is basically not cluster safe anyway and is not / should not be used directly ")
    synchronized public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent clearCacheEvent)
    {
        for (final JiraCachingPropertySet propertySet : propertySets)
        {
            propertySet.clearCache();
        }
    }

    @VisibleForTesting
    @ClusterSafe("This class is basically not cluster safe anyway and is not / should not be used directly ")
    synchronized Collection<JiraCachingPropertySet> getManagedPropertySets()
    {
        return new ArrayList<JiraCachingPropertySet>(propertySets);
    }
}
