package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.jcip.annotations.NotThreadSafe;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.atlassian.jira.util.collect.CollectionUtil.transformSet;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides a union view of two maps.
 * <p>
 * Restrictions:
 * <ul>
 * <li><strong>The supplied maps become the property of the new map.</strong>
 *          They should not be accessed directly in any way after the
 *          composite map is created.  The result of such access is
 *          left <strong>undefined</strong>.</li>
 * <li><strong>The mutability of this map depends on that of the first map.</strong>
 *          For efficiency reasons, this class uses the first map to hold
 *          updated state.  If the first map is <strong>immutable</strong>,
 *          then this map will be, too.  The second map in never modified
 *          by this class.</li>
 * <li><strong>CompositeMap is not synchronized and is not thread-safe.</strong>
 *          If you wish to use this map from multiple threads concurrently, you
 *          must use appropriate synchronization.  The simplest approach is to
 *          wrap this map using {@link java.util.Collections#synchronizedMap(Map)}.
 *          This class will behave in an <strong>undefined</strong> manner if
 *          accessed by concurrent threads without synchronization.</li>
 * </ul>
 */
@NotThreadSafe
public class CompositeMap<K, V> extends AbstractMap<K, V>
{
    // Note: package level so the KeySet and KeySetIterator will not need
    // synthetic accessors.
    final Map<K, V> one;
    final Map<K, V> two;
    final Set<Object> removed = Sets.newHashSet();

    private KeySet keySet = null;
    private EntrySet entrySet = null;

    /**
     * Create a new CompositeMap with two composited Map instances.
     *
     * @param one  the first Map to be composited, values here override values in the second.
     * @param two  the second Map to be composited
     * @return the CompositeMap, or <tt>one</tt> itself if <tt>two</tt> is empty
     * @throws IllegalArgumentException if either map is <tt>null</tt>
     */
    public static <K, V> Map<K, V> of(final Map<K, V> one, final Map<K, V> two)
    {
        notNull("one", one);
        return notNull("two", two).isEmpty() ? one : new CompositeMap<K, V>(one, two);
    }

    /**
     * Create a new CompositeMap which composites both Map instances in the
     * argument.
     *
     * @param one the first Map
     * @param two the second Map
     * @throws IllegalArgumentException if either map is <tt>null</tt>
     */
    private CompositeMap(final Map<K, V> one, final Map<K, V> two)
    {
        this.one = one;
        this.two = two;
    }

    //-----------------------------------------------------------------------

    @Override
    public Set<K> keySet()
    {
        final KeySet keys = keySet;
        return (keys != null) ? keys : (keySet = new KeySet());
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        final EntrySet entries = entrySet;
        return (entries != null) ? entries : (entrySet = new EntrySet());
    }

    @Override
    public boolean isEmpty()
    {
        // This is accessed a trillion times in JIRA so we optimise it as best we can.
        if (one.isEmpty())
        {
            // two is immutable and the remove method for entries in two works by adding the keys to removed
            // which means for two to be nominally empty all its keys must be in removed.
            return removed.size() == two.size();
        }
        return false;
    }

    @Override
    public int size()
    {
        int count = one.size();
        for (K key : two.keySet())
        {
            if (!one.containsKey(key) && !removed.contains(key))
            {
                ++count;
            }
        }
        return count;
    }

    @Override
    public boolean containsKey(final Object key)
    {
        return one.containsKey(key) || (two.containsKey(key) && !removed.contains(key));
    }

    @Override
    public V get(final Object key)
    {
        if (one.containsKey(key))
        {
            return one.get(key);
        }
        return removed.contains(key) ? null : two.get(key);
    }

    //
    // mutators
    //

    @Override
    public V put(final K key, final V value)
    {
        if (one.containsKey(key))
        {
            return one.put(key, value);
        }
        one.put(key, value);
        return (two.containsKey(key) && removed.add(key)) ? two.get(key) : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        one.putAll(m);
        removed.removeAll(m.keySet());
    }

    @Override
    public V remove(final Object key)
    {
        final boolean inMapTwoAndNotYetRemoved = two.containsKey(key) && removed.add(key);
        return one.containsKey(key)
                ? one.remove(key)
                : (inMapTwoAndNotYetRemoved ? two.get(key) : null);
    }

    private boolean eq(Object a, Object b)
    {
        return (a != null) ? a.equals(b) : (b == null);
    }

    @Override
    public void clear()
    {
        one.clear();
        removed.addAll(two.keySet());
    }

    //
    // inner classes
    //

    /**
     * KeySet is a view constructed from both underlying maps.  It delegates to
     * the CompositeMap itself for everything except add and the bulk
     * operations (addAll, containsAll, removeAll, retainAll), all of
     * which either have no equivalent at the Map level or require different
     * behaviour.  It has no state of its own.
     */
    class KeySet extends AbstractSet<K>
    {
        private ImmutableSet<K> snapshot()
        {
            return ImmutableSet.<K>builder().addAll(this).build();
        }

        @Override
        public int size()
        {
            return CompositeMap.this.size();
        }

        @Override
        public boolean isEmpty()
        {
            return CompositeMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o)
        {
            return CompositeMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        @Override
        public Object[] toArray()
        {
            return snapshot().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a)
        {
            return snapshot().toArray(a);
        }

        @Override
        public boolean add(K k)
        {
            throw new UnsupportedOperationException("CompositeMap.KeySet.add");
        }

        @Override
        public boolean remove(Object o)
        {
            final boolean foundInTwo = two.containsKey(o) && removed.add(o);
            if (one.containsKey(o))
            {
                one.remove(o);
                return true;
            }
            return foundInTwo;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            for (Object key : c)
            {
                if (!containsKey(key))
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends K> c)
        {
            throw new UnsupportedOperationException("CompositeMap.KeySet.addAll");
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            boolean changed = one.keySet().retainAll(c);
            for (K key : two.keySet())
            {
                if (!c.contains(key) && removed.add(key))
                {
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            boolean changed = one.keySet().removeAll(c);
            for (Object key : c)
            {
                if (two.containsKey(key) && removed.add(key))
                {
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public void clear()
        {
            CompositeMap.this.clear();
        }
    }

    /**
     * The iterator for the KeySet.
     */
    class KeySetIterator implements Iterator<K>
    {
        private Iterator<K> iter = one.keySet().iterator();
        private K nextKey = null;
        private K thisKey = null;
        private boolean inMapOne = true;
        private boolean hasNext = false;
        private boolean hasThis = false;

        public boolean hasNext()
        {
            if (hasNext)
            {
                return true;
            }

            // We use all keys from map one
            if (inMapOne)
            {
                if (iter.hasNext())
                {
                    nextKey = iter.next();
                    return hasNext = true;
                }
                inMapOne = false;
                iter = two.keySet().iterator();
            }

            // From map two, we ignore keys that have been overridden
            // either by an updated value in map one or by removal
            while (iter.hasNext())
            {
                final K key = iter.next();
                if (!one.containsKey(key) && !removed.contains(key))
                {
                    nextKey = key;
                    return hasNext = true;
                }
            }

            // We've exhausted both maps
            return false;
        }

        @Override
        public K next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException("No more keys in the set");
            }
            hasNext = false;
            hasThis = true;
            return thisKey = nextKey;
        }

        @Override
        public void remove()
        {
            if (!hasThis)
            {
                throw new IllegalStateException("No current key");
            }
            hasThis = false;

            final K key = thisKey;
            if (inMapOne)
            {
                // While in the first map, use the iterator to avoid ConcModEx, but
                // we also need to make sure the removed map is updated if needed
                iter.remove();
                if (two.containsKey(key))
                {
                    removed.add(key);
                }
            }
            else
            {
                // The second map is easier...
                one.remove(key);  // harmless way to check mutability
                removed.add(key);
            }
        }
    }

    class EntryTransformer implements Function<K, Entry<K, V>>
    {
        public Entry<K, V> get(final K key)
        {
            return new LazyMapEntry<K, V>(CompositeMap.this, key);
        }
    }

    /**
     * EntrySet is a view constructed from both underlying maps.  It delegates to
     * the CompositeMap itself for everything except add, contains, and the bulk
     * operations (addAll, containsAll, removeAll, retainAll), all of
     * which either have no equivalent at the Map level or require different
     * behaviour.  It has no state of its own.
     */
    class EntrySet extends AbstractSet<Entry<K,V>>
    {
        @Override
        public int size()
        {
            return CompositeMap.this.size();
        }

        @Override
        public boolean isEmpty()
        {
            return CompositeMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o)
        {
            if (o instanceof Entry)
            {
                final Entry<?,?> other = (Entry<?,?>)o;
                final Object key = other.getKey();
                return CompositeMap.this.containsKey(key) && eq(CompositeMap.this.get(key), other.getValue());
            }
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return new EntrySetIterator();
        }

        private Set<Entry<K,V>> snapshot()
        {
            return transformSet(keySet(), new EntryTransformer());
        }

        @Override
        public Object[] toArray()
        {
            return snapshot().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a)
        {
            return snapshot().toArray(a);
        }

        @Override
        public boolean add(Entry<K,V> kvEntry)
        {
            throw new UnsupportedOperationException("CompositeMap.EntrySet.add");
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K,V>> c)
        {
            throw new UnsupportedOperationException("CompositeMap.EntrySet.addAll");
        }

        @Override
        public boolean remove(Object o)
        {
            if (contains(o))
            {
                CompositeMap.this.remove(((Entry<?,?>)o).getKey());
                return true;
            }
            return false;
        }


        @Override
        public void clear()
        {
            CompositeMap.this.clear();
        }
    }

    /**
     * The iterator for the EntrySet.  This delegates to the KeySetIterator
     * to do the hard work and generates LazyMapEntry objects, which in turn
     * delegate to CompositeMap.get(K) to implement getValue().
     */
    class EntrySetIterator implements Iterator<Entry<K,V>>
    {
        private final KeySetIterator keys = new KeySetIterator();

        @Override
        public boolean hasNext()
        {
            return keys.hasNext();
        }

        @Override
        public Entry<K,V> next()
        {
            return new LazyMapEntry<K, V>(CompositeMap.this, keys.next());
        }

        @Override
        public void remove()
        {
            keys.remove();
        }
    }
}

