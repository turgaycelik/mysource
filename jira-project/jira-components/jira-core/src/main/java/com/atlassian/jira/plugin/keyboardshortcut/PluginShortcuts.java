package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Thread-safe class for storing and retrieving shortcuts.
 *
 * @since 6.0
 */
@ThreadSafe
class PluginShortcuts
{
    /**
     * Read/write lock that guards access to this instance.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * All currently registered shortcuts, mapped by module key.
     */
    @GuardedBy("lock")
    private final Map<String, KeyboardShortcut> shortcuts = Maps.newHashMap();

    /**
     * A lazily-initialised, sorted copy of {@link #shortcuts}.
     */
    @GuardedBy("lock")
    @ClusterSafe("Driven by plugin state, which is kept in synch across the cluster")
    private final ResettableLazyReference<ImmutableList<KeyboardShortcut>> sortedCopy = new ResettableLazyReference<ImmutableList<KeyboardShortcut>>()
    {
        @Override
        protected ImmutableList<KeyboardShortcut> create() throws Exception
        {
            return Ordering.natural().immutableSortedCopy(shortcuts.values());
        }
    };

    /**
     * Re-initialises this PluginShortcuts with the given KeyboardShortcutModuleDescriptors.
     *
     * @param descriptors a List of KeyboardShortcutModuleDescriptor
     */
    public void reInit(List<KeyboardShortcutModuleDescriptor> descriptors)
    {
        lock.writeLock().lock();
        try
        {
            shortcuts.clear();
            sortedCopy.reset();
            for (KeyboardShortcutModuleDescriptor descriptor : descriptors)
            {
                shortcuts.put(descriptor.getCompleteKey(), descriptor.getModule());
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Registers a new KeyboardShortcut with the given module key.
     *
     * @param moduleKey the module key
     * @param keyboardShortcut the KeyboardShortcut
     */
    public void register(String moduleKey, KeyboardShortcut keyboardShortcut)
    {
        lock.writeLock().lock();
        try
        {
            shortcuts.put(moduleKey, keyboardShortcut);
            sortedCopy.reset();
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Unregisters the KeyboardShortcut with the given module key.
     *
     * @param moduleKey the module key
     */
    public void unregister(String moduleKey)
    {
        lock.writeLock().lock();
        try
        {
            shortcuts.remove(moduleKey);
            sortedCopy.reset();
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns a <b>sorted</b> list of all registered keyboard shortcuts.
     *
     * @return an immutable list of KeyboardShortcut (sorted by order)
     */
    public ImmutableList<KeyboardShortcut> getAll()
    {
        lock.readLock().lock();
        try
        {
            return sortedCopy.get();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
}
