/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 4, 2004
 * Time: 12:56:04 PM
 */
package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.search.Query;

import java.util.Map;

public class PermissionsFilterCache
{
    private final Map cache = new LRUMap(50);

    public boolean hasQueryFor(User user)
    {
        return cache.containsKey(new CacheKey(user));
    }

    public Query getQuery(User user)
    {
        return (Query) cache.get(new CacheKey(user));
    }

    public void storeQuery(Query query, User user)
    {
        cache.put(new CacheKey(user), query);
    }

    public void flush()
    {
        cache.clear();
    }

    private static class CacheKey
    {
        String username;

        public CacheKey(User searcher)
        {
            if (searcher != null)
            {
                username = searcher.getName();
            }
        }

        @Override
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

            final CacheKey that = (CacheKey) o;

            if (username != null ? !username.equals(that.username) : that.username != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return username != null ? username.hashCode() : 0;
        }
    }
}
