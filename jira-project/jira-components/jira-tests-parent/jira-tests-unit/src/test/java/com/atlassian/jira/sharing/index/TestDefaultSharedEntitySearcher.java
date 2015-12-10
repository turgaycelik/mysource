package com.atlassian.jira.sharing.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.sharing.MockSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MockCloseableIterable;

import com.google.common.collect.Lists;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test case for DefaultSharedEntitySearcher
 *
 * @since v3.13
 */
public class TestDefaultSharedEntitySearcher extends MockControllerTestCase
{
    private static final Long ID_123 = new Long(123);

    private ApplicationUser user;
    private final Function<Long, SharedEntity> transformer = new Function<Long, SharedEntity>()
    {
        public SharedEntity get(final Long input)
        {
            return new MockSharedEntity(input, null, SharePermissions.PRIVATE);
        }
    };

    @Before
    public void setUp()
    {
        user = new MockApplicationUser("admin");
    }

    @Test
    public void testBadConstructor() throws IOException
    {
        @SuppressWarnings("unchecked")
        final SharedEntityAccessor<SharedEntity> entityAccessor = mockController.getMock(SharedEntityAccessor.class);
        final QueryFactory queryFactory = mockController.getMock(QueryFactory.class);
        final IndexSearcherFactory searcher = getSearcherFactory(Collections.<Long, String> emptyMap());

        replay();

        assertBadConstructor(null, entityAccessor, queryFactory);
        assertBadConstructor(searcher, null, queryFactory);
        assertBadConstructor(searcher, entityAccessor, null);
    }

    private void assertBadConstructor(final IndexSearcherFactory searcherFactory, final SharedEntityAccessor<SharedEntity> accessor, final QueryFactory queryFactory)
    {
        try
        {
            new DefaultSharedEntitySearcher<SharedEntity>(searcherFactory, accessor, queryFactory);
            fail("should have barfed on construction");
        }
        catch (final IllegalArgumentException ignored)
        {}
    }

    @Test
    public void testSearchBadParams() throws IOException
    {
        mockController.getMock(SharedEntityAccessor.class);
        mockController.getMock(QueryFactory.class);
        final ClosedIndexSearcherFactory searcherFactory = getSearcherFactory(Collections.<Long, String> emptyMap());
        mockController.addObjectInstance(searcherFactory);

        @SuppressWarnings("unchecked")
        final DefaultSharedEntitySearcher<SharedEntity> entitySearcher = mockController.instantiateAndReplay(DefaultSharedEntitySearcher.class);

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();
        assertBaseParams(entitySearcher, null, user, 0, 100);
        assertBaseParams(entitySearcher, searchParameters, user, -1, 100);
        assertBaseParams(entitySearcher, searchParameters, user, 0, 0);
        assertBaseParams(entitySearcher, searchParameters, user, 0, -1);
        assertBaseParams(entitySearcher, searchParameters, user, 5, Integer.MAX_VALUE);
        assertEquals(0, searcherFactory.open);
    }

    private void assertBaseParams(final DefaultSharedEntitySearcher<SharedEntity> entitySearcher, final SharedEntitySearchParameters searchParameters, final ApplicationUser user, final int pageOffset, final int pageWidth)
    {
        try
        {
            entitySearcher.search(searchParameters, user, pageOffset, pageWidth);
            fail("Should have barfed on params");
        }
        catch (final IllegalArgumentException ignored)
        {}
    }

    @Test
    public void testBasicSearch() throws IOException
    {
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();
        final Query expectedQuery = new TermQuery(new Term("data", "rats"));

        @SuppressWarnings("unchecked")
        final Map<Long, String> dataMap = EasyMap.build(ID_123, "rats");

        final ClosedIndexSearcherFactory searcherFactory = getSearcherFactory(dataMap);
        mockController.addObjectInstance(searcherFactory);

        final QueryFactory queryFactory = mockController.getMock(QueryFactory.class);
        queryFactory.create(searchParameters, user.getDirectoryUser());
        mockController.setReturnValue(expectedQuery);

        final List<Long> expectedIds = Lists.newArrayList(ID_123);
        final EnclosedIterable<SharedEntity> closeableIterable = createCloseableIterable(expectedIds);
        @SuppressWarnings("unchecked")
        final SharedEntityAccessor<SharedEntity> sharedEntityAccessor = mockController.getMock(SharedEntityAccessor.class);
        sharedEntityAccessor.get(user.getDirectoryUser(), getIds(expectedIds, true));
        mockController.setDefaultReturnValue(closeableIterable);

        @SuppressWarnings("unchecked")
        final DefaultSharedEntitySearcher<SharedEntity> entitySearcher = mockController.instantiate(DefaultSharedEntitySearcher.class);

        final SharedEntitySearchResult<SharedEntity> searchResult = entitySearcher.search(searchParameters, user, 0, 100);
        assertNotNull(searchResult);
        assertEquals(1, searchResult.getTotalResultCount());
        assertEquals(1, searchResult.size());
        assertFalse(searchResult.hasMoreResults());
        assertIdsInOrder(searchResult, expectedIds);
        assertEquals(0, searcherFactory.open);
    }

    @Test
    public void testSearchWithUserPaging() throws IOException
    {
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();
        final Query expectedQuery = new TermQuery(new Term("data", "rats"));

        final Map<Long, String> dataMap = buildRats(100);

        final ClosedIndexSearcherFactory searcherFactory = getSearcherFactory(dataMap);
        mockController.addObjectInstance(searcherFactory);

        final QueryFactory queryFactory = mockController.getMock(QueryFactory.class);
        queryFactory.create(searchParameters, user.getDirectoryUser());
        mockController.setReturnValue(expectedQuery);

        final List<Long> expectedIds = buildIds(20);
        @SuppressWarnings("unchecked")
        final SharedEntityAccessor<SharedEntity> accessorDuck = getDuck(SharedEntityAccessor.class, new Object()
        {
            @SuppressWarnings("unused")
            public EnclosedIterable<SharedEntity> get(final SharedEntityAccessor.RetrievalDescriptor descriptor)
            {
                assertTrue(descriptor.preserveOrder());
                assertNotNull(descriptor.getIds());
                assertEquals(20, descriptor.getIds().size());
                return new MockCloseableIterable<SharedEntity>(descriptor.getIds(), transformer);
            }

            @SuppressWarnings("unused")
            public EnclosedIterable<SharedEntity> get(final User searcher, final SharedEntityAccessor.RetrievalDescriptor descriptor)
            {
                assertEquals(user.getDirectoryUser(), searcher);
                assertTrue(descriptor.preserveOrder());
                assertNotNull(descriptor.getIds());
                assertEquals(20, descriptor.getIds().size());
                return new MockCloseableIterable<SharedEntity>(descriptor.getIds(), transformer);
            }
        });
        mockController.addObjectInstance(accessorDuck);

        @SuppressWarnings("unchecked")
        final DefaultSharedEntitySearcher<SharedEntity> entitySearcher = mockController.instantiate(DefaultSharedEntitySearcher.class);

        // we have 100 hits but we want to page 20 of them
        final SharedEntitySearchResult<SharedEntity> searchResult = entitySearcher.search(searchParameters, user, 0, 20);
        assertNotNull(searchResult);
        assertEquals(20, searchResult.size());
        assertEquals(100, searchResult.getTotalResultCount());
        assertTrue(searchResult.hasMoreResults());
        assertIdsInOrder(searchResult, expectedIds);
        assertEquals(0, searcherFactory.open);
    }

    @Test
    public void testSearchWithoutUser() throws IOException
    {
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();
        final Query expectedQuery = new TermQuery(new Term("data", "rats"));

        final Map<Long, String> dataMap = buildRats(100);

        final ClosedIndexSearcherFactory searcherFactory = getSearcherFactory(dataMap);
        mockController.addObjectInstance(searcherFactory);

        final QueryFactory queryFactory = mockController.getMock(QueryFactory.class);
        queryFactory.create(searchParameters);
        mockController.setReturnValue(expectedQuery);

        final List<Long> expectedIds = buildIds(100);

        @SuppressWarnings("unchecked")
        final SharedEntityAccessor<SharedEntity> accessorDuck = getDuck(SharedEntityAccessor.class, new Object()
        {
            @SuppressWarnings("unused")
            public EnclosedIterable<SharedEntity> get(final SharedEntityAccessor.RetrievalDescriptor descriptor)
            {
                assertFalse(descriptor.preserveOrder());
                assertNotNull(descriptor.getIds());
                assertEquals(100, descriptor.getIds().size());
                return new MockCloseableIterable(descriptor.getIds(), new Function<Long, MockSharedEntity>()
                {
                    public MockSharedEntity get(final Long input)
                    {
                        return new MockSharedEntity(input, null, SharePermissions.PRIVATE);
                    }
                });
            }
        });
        mockController.addObjectInstance(accessorDuck);

        @SuppressWarnings("unchecked")
        final DefaultSharedEntitySearcher<SharedEntity> entitySearcher = mockController.instantiate(DefaultSharedEntitySearcher.class);

        // we have 100 hits but we want to page 20 of them
        final SharedEntitySearchResult<SharedEntity> searchResult = entitySearcher.search(searchParameters);
        assertNotNull(searchResult);
        assertEquals(100, searchResult.size());
        assertEquals(100, searchResult.getTotalResultCount());
        assertFalse(searchResult.hasMoreResults());
        assertIdsInOrder(searchResult, expectedIds);
        assertEquals(0, searcherFactory.open);
    }

    private void assertIdsInOrder(final EnclosedIterable<SharedEntity> closeableIterable, final List<Long> expectedIds)
    {
        assertNotNull(closeableIterable);
        assertEquals(expectedIds.size(), closeableIterable.size());
        if (expectedIds.size() == 0)
        {
            assertTrue(closeableIterable.isEmpty());
        }
        final Iterator<Long> iterator = expectedIds.iterator();
        closeableIterable.foreach(new Consumer<SharedEntity>()
        {
            public void consume(final SharedEntity actual)
            {
                assertEquals(iterator.next(), actual.getId());
            }
        });
    }

    private List<Long> buildIds(final int i)
    {
        final List<Long> list = new ArrayList<Long>(i);
        for (long j = 0; j < i; j++)
        {
            list.add(j);
        }
        return list;
    }

    private Map<Long, String> buildRats(final int i)
    {
        final Map<Long, String> map = new LinkedHashMap<Long, String>(i);
        for (long j = 0; j < i; j++)
        {
            map.put(j, "rats");
        }
        return map;
    }

    private EnclosedIterable<SharedEntity> createCloseableIterable(final List<Long> expectedIds)
    {
        return new MockCloseableIterable<SharedEntity>(expectedIds, transformer);
    }

    SharedEntityAccessor.RetrievalDescriptor getIds(final List<Long> ids, final boolean preserveOrder)
    {
        return new SharedEntityAccessor.RetrievalDescriptor()
        {
            public List<Long> getIds()
            {
                return ids;
            }

            public boolean preserveOrder()
            {
                return preserveOrder;
            }
        };
    }

    /**
     * A searcher with some standard data in it
     */
    ClosedIndexSearcherFactory getSearcherFactory(final Map<Long, String> dataMap)
    {
        return new ClosedIndexSearcherFactory(dataMap);
    }

    class ClosedIndexSearcherFactory implements IndexSearcherFactory
    {
        private final Map<Long, String> dataMap;
        int open = 0;

        public ClosedIndexSearcherFactory(final Map<Long, String> dataMap)
        {
            this.dataMap = dataMap;
        }

        public IndexSearcher get()
        {
            open++;
            return new DelegateSearcher(getSearcher(dataMap))
            {
                @Override
                public void close() throws IOException
                {
                    open--;
                    super.close();
                }
            };
        }
    }

    /**
     * A searcher with some standard data in it
     */
    IndexSearcher getSearcher(final Map<Long, String> idMap)
    {
        try
        {
            final RAMDirectory ramDirectory = new RAMDirectory();
            final IndexWriter indexWriter = new IndexWriter(ramDirectory, new WhitespaceAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
            for (final Map.Entry<Long, String> entry : idMap.entrySet())
            {
                final Document doc = new Document();
                doc.add(new Field("data", entry.getValue(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("id", String.valueOf(entry.getKey()), Field.Store.YES, Field.Index.NOT_ANALYZED));
                indexWriter.addDocument(doc);
            }

            indexWriter.close();
            return new IndexSearcher(ramDirectory, true);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    <T> T getDuck(final Class<T> clazz, final Object duck)
    {
        return clazz.cast(DuckTypeProxy.getProxy(clazz, duck));
    }

    @SuppressWarnings("deprecation")
    private class DelegateSearcher extends IndexSearcher
    {
        private final IndexSearcher delegate;

        DelegateSearcher(final IndexSearcher delegate)
        {
            super(delegate.getIndexReader());
            this.delegate = delegate;
        }

        @Override
        public void close() throws IOException
        {
            delegate.close();
        }

        public Document doc(final int arg0, final FieldSelector arg1) throws CorruptIndexException, IOException
        {
            return delegate.doc(arg0, arg1);
        }

        @Override
        public Document doc(final int arg0) throws CorruptIndexException, IOException
        {
            return delegate.doc(arg0);
        }

        @Override
        public int docFreq(final Term arg0) throws IOException
        {
            return delegate.docFreq(arg0);
        }

        @Override
        public int[] docFreqs(final Term[] arg0) throws IOException
        {
            return delegate.docFreqs(arg0);
        }

        @Override
        public boolean equals(final Object obj)
        {
            return delegate.equals(obj);
        }

        @Override
        public Explanation explain(final Query query, final int doc) throws IOException
        {
            return delegate.explain(query, doc);
        }

        @Override
        public Explanation explain(final Weight arg0, final int arg1) throws IOException
        {
            return delegate.explain(arg0, arg1);
        }

        @Override
        public Similarity getSimilarity()
        {
            return delegate.getSimilarity();
        }

        @Override
        public int hashCode()
        {
            return delegate.hashCode();
        }

        @Override
        public int maxDoc()
        {
            return delegate.maxDoc();
        }

        @Override
        public Query rewrite(final Query arg0) throws IOException
        {
            return delegate.rewrite(arg0);
        }

        @Override
        public void search(final Query query, final Filter filter, final Collector results) throws IOException
        {
            delegate.search(query, filter, results);
        }

        @Override
        public TopFieldDocs search(final Query query, final Filter filter, final int n, final Sort sort) throws IOException
        {
            return delegate.search(query, filter, n, sort);
        }

        @Override
        public TopDocs search(final Query query, final Filter filter, final int n) throws IOException
        {
            return delegate.search(query, filter, n);
        }

        @Override
        public TopFieldDocs search(final Weight arg0, final Filter arg1, final int arg2, final Sort arg3) throws IOException
        {
            return delegate.search(arg0, arg1, arg2, arg3);
        }

        @Override
        public TopDocs search(final Weight arg0, final Filter arg1, final int arg2) throws IOException
        {
            return delegate.search(arg0, arg1, arg2);
        }

        @Override
        public void setSimilarity(final Similarity similarity)
        {
            delegate.setSimilarity(similarity);
        }

        @Override
        public void search(final Weight weight, final Filter filter, final Collector results) throws IOException
        {
            delegate.search(weight, filter, results);
        }

        @Override
        public String toString()
        {
            return delegate.toString();
        }
    }
}
