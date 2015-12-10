package com.atlassian.jira.issue.search.parameters.lucene;

import java.io.IOException;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCachedWrappedFilterCache
{
    @Test
    public void testStoreFilter() throws Exception
    {
        CachedWrappedFilterCache cache = new CachedWrappedFilterCache();
        Filter filter1 = new MockFilter();
        User fred = new MockUser("fred");
        User barney = new MockUser("barney");

        cache.storeFilter(filter1, fred);
        final Filter returnedFilter1 = cache.getFilter(fred);
        assertNotNull(returnedFilter1);
        assertNull(cache.getFilter(barney));

        cache.storeFilter(filter1, barney);
        final Filter returnedFilter2 = cache.getFilter(barney);
        assertNotNull(returnedFilter2);
        assertEquals(filter1, returnedFilter1);
        assertEquals(filter1, returnedFilter2);
    }

    private static final class MockFilter extends Filter
    {
        @Override
        public DocIdSet getDocIdSet(IndexReader reader) throws IOException
        {
            return null;
        }
    }
}
