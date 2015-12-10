package com.atlassian.jira.util.index;


import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.config.properties.BackingPropertySetManager;
import com.atlassian.jira.extension.Startable;

import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.module.propertyset.PropertySet;

import static org.apache.commons.lang3.Validate.validState;

/**
 * @since v6.0
 */
public class IndexingCounterManagerImpl implements IndexingCounterManager, IndexingCounter, Startable
{
    @VisibleForTesting
    static final String COUNTER_LOCK_NAME = IndexingCounterManagerImpl.class.getName() + ".counter";
    @VisibleForTesting
    static final String PROPERTY_KEY = IndexingCounterManagerImpl.class.getName() + ".counterValue";

    private final AtomicBoolean startHasBeenCalled;
    private final BackingPropertySetManager backingPropertySetManager;
    private final ClusterLockService clusterLockService;

    private ClusterLock counterLock;
    private long counter;

    public IndexingCounterManagerImpl(
            final BackingPropertySetManager backingPropertySetManager, final ClusterLockService clusterLockService)
    {
        this.backingPropertySetManager = backingPropertySetManager;
        this.clusterLockService = clusterLockService;
        this.counter = 0;
        this.startHasBeenCalled = new AtomicBoolean(false);
    }

    @Override
    public void start()
    {
        this.counterLock = clusterLockService.getLockForName(COUNTER_LOCK_NAME);
        final PropertySet propertySet = getPropertySet();
        if (propertySet.exists(PROPERTY_KEY))
        {
            counter = propertySet.getLong(PROPERTY_KEY);
        }
        else
        {
            propertySet.setLong(PROPERTY_KEY, counter);
        }
        startHasBeenCalled.set(true);
    }

    @VisibleForTesting
    PropertySet getPropertySet()
    {
        return backingPropertySetManager.getPropertySetSupplier().get();
    }

    @Override
    public long getCurrentValue()
    {
        counterLock.lock();
        try
        {
            return counter;
        }
        finally
        {
            counterLock.unlock();
        }
    }

    @Override
    public long incrementValue()
    {
        validState(startHasBeenCalled.get(), "Called incrementValue() before start()");
        counterLock.lock();
        try
        {
            final long speculativeIncrement = counter + 1;
            getPropertySet().setLong(PROPERTY_KEY, speculativeIncrement);
            return ++counter;
        }
        finally
        {
            counterLock.unlock();
        }
    }
}
