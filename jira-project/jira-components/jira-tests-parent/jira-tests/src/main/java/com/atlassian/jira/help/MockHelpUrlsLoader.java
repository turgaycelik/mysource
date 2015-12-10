package com.atlassian.jira.help;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import javax.annotation.Nonnull;

/**
 * @since v6.2.4
 */
public class MockHelpUrlsLoader implements HelpUrlsLoader
{
    private long currentId = 0;
    private Map<Long, HelpUrls> urls = Maps.newHashMap();

    @Nonnull
    @Override
    public HelpUrlsLoaderKey keyForCurrentUser()
    {
        return new MockHelpUrlsLoaderKey(currentId);
    }

    @Override
    public HelpUrls apply(final HelpUrlsLoaderKey input)
    {
        if (input instanceof MockHelpUrlsLoaderKey)
        {
            return urls.get(((MockHelpUrlsLoaderKey) input).id);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public MockHelpUrlsLoader setCurrentId(long id)
    {
        currentId = id;
        return this;
    }

    public MockHelpUrls createUrls(MockHelpUrlsLoaderKey key)
    {
        MockHelpUrls result = new MockHelpUrls();
        urls.put(key.id, result);
        return result;
    }

    public MockHelpUrlsLoaderKey createKey(long id)
    {
        return new MockHelpUrlsLoaderKey(id);
    }

    public static class MockHelpUrlsLoaderKey implements HelpUrlsLoaderKey
    {
        private final long id;

        public MockHelpUrlsLoaderKey(final long id) {this.id = id;}

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final MockHelpUrlsLoaderKey that = (MockHelpUrlsLoaderKey) o;

            if (id != that.id) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return (int) (id ^ (id >>> 32));
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                    .append("id", id)
                    .toString();
        }
    }
}
