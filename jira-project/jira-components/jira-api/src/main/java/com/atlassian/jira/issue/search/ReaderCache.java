package com.atlassian.jira.issue.search;

import com.atlassian.jira.util.Supplier;
import org.apache.lucene.index.IndexReader;

import java.util.Collection;

/**
 * Cache values by reader.
 */
public interface ReaderCache
{
    Collection<String>[] get(IndexReader reader, String key, Supplier<Collection<String>[]> supplier);
}
