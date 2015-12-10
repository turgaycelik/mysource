package com.atlassian.jira.issue.search;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.cache.CompositeKeyCache;
import org.apache.lucene.index.IndexReader;

import java.util.Collection;

@ClusterSafe
public class DefaultReaderCache implements ReaderCache
{
    private final CompositeKeyCache<IndexReader, String, Collection<String>[]> cache = CompositeKeyCache.createWeakFirstKeySoftValueCache(DefaultReaderCache.class.getSimpleName());

    public Collection<String>[] get(final IndexReader reader, final String key, final Supplier<Collection<String>[]> supplier)
    {
        return cache.get(reader, key, supplier);
    }
}
