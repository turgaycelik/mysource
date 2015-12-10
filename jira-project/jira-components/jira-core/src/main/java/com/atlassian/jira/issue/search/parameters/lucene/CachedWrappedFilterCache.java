package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.search.Filter;

import java.util.Map;

/**
 * A cache for {@link com.atlassian.jira.issue.search.parameters.lucene.CachedWrappedFilterCache} objects
 */
public class CachedWrappedFilterCache
{
    private final Map cache = new LRUMap(8);

    public Filter getFilter(User user)
    {
        return (Filter) cache.get(new WFCacheKey(user));
    }

    public void storeFilter(Filter filter, User user)
    {
        cache.put(new WFCacheKey(user), filter);
    }

    private static class WFCacheKey
    {
        String username;

        public WFCacheKey(User searcher)
        {
            if (searcher != null)
            {
                username = searcher.getName();
            }
        }

        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final WFCacheKey that = (WFCacheKey) o;

            if (username != null ? !username.equals(that.username) : that.username != null)
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            return (username != null ? username.hashCode() : 0);
        }
    }

}