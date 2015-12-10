/**
 * Copyright 2008 Atlassian Pty Ltd
 */

package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.statistics.util.FieldHitCollector;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.SharedEntityColumnDefinition;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Function;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.collect.CollectionUtil.transform;
import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link SharedEntitySearcher}.
 *
 * @since v3.13
 */
public class DefaultSharedEntitySearcher<S extends SharedEntity> implements SharedEntitySearcher<S>
{
    static final class SharedEntitySortField extends SortField
    {
        static final SharedEntitySortField NAME = new SharedEntitySortField(SharedEntityColumnDefinition.NAME, false);
        static final SharedEntitySortField ID = new SharedEntitySortField(SharedEntityColumnDefinition.ID, false);

        SharedEntitySortField(final SharedEntityColumnDefinition column, final boolean reverse)
        {
            super(column.getSortColumn(), column.getSortType(), reverse);
        }

        SharedEntitySortField(final SharedEntityColumnDefinition column, final FieldComparatorSource FieldComparatorSource, final boolean reverse)
        {
            super(column.getSortColumn(), FieldComparatorSource, reverse);
        }
    }

    private static class Transformers
    {
        static final Function<String, Long> STRING_TO_LONG = new Function<String, Long>()
        {
            public Long get(final String input)
            {
                return Long.valueOf(String.valueOf(input));
            }
        };
    }

    private static final SortFieldFactory SORT_FACTORY = new SortFieldFactory();

    //
    // members
    //

    private final IndexSearcherFactory searcherFactory;
    private final SharedEntityAccessor<S> accessor;
    private final QueryFactory queryFactory;

    //
    // ctors
    //

    public DefaultSharedEntitySearcher(final IndexSearcherFactory searcherFactory, final SharedEntityAccessor<S> accessor, final QueryFactory queryFactory)
    {
        this.searcherFactory = notNull("searcherFactory", searcherFactory);
        this.accessor = notNull("accessor", accessor);
        this.queryFactory = notNull("queryFactory", queryFactory);
    }

    //
    // methods
    //

    public SharedEntitySearchResult<S> search(final SharedEntitySearchParameters searchParameters, final ApplicationUser user, final int pageOffset, final int pageWidth)
    {
        checkParametersForSanity(searchParameters, pageOffset, pageWidth);

        final Query query = queryFactory.create(searchParameters, ApplicationUsers.toDirectoryUser(user));
        final Sort sort = new Sort(new SharedEntitySortField[] { SORT_FACTORY.getSortField(searchParameters.getSortColumn(), !searchParameters.isAscendingSort()),
                SharedEntitySortField.NAME, SharedEntitySortField.ID });

        final IndexSearcher searcher = searcherFactory.get();
        try
        {
            return new Engine(true)
            {
                @Override
                TopDocs getHits() throws IOException
                {
                    return searcher.search(query, Integer.MAX_VALUE, sort);
                }

                @Override
                Page page(final TopDocs source)
                {
                    return new Page(source, pageOffset, pageWidth, searcher);
                }
            }.search(ApplicationUsers.toDirectoryUser(user));
        }
        finally
        {
            try
            {
                searcher.close();
            }
            catch (final IOException ignore)
            {}
        }
    }

    public SharedEntitySearchResult<S> search(final SharedEntitySearchParameters searchParameters)
    {
        final Query query = queryFactory.create(notNull("searchParameters", searchParameters));

        final IndexSearcher searcher = searcherFactory.get();
        try
        {
            return new Engine(false)
            {
                @Override
                TopDocs getHits() throws IOException
                {
                    return searcher.search(query, Integer.MAX_VALUE);
                }

                @Override
                Page page(final TopDocs source)
                {
                    return new All(source, searcher);
                }
            }.search();
        }
        finally
        {
            try
            {
                searcher.close();
            }
            catch (final IOException ignore)
            {}
        }
    }

    private void checkParametersForSanity(final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
    {
        notNull("searchParameters", searchParameters);
        not("pageOffset < 0", pageOffset < 0);
        not("pageWidth <= 0", pageWidth <= 0);
        not("pageWidth of MAX_VALUE used with non zero pageOffset", (pageWidth == Integer.MAX_VALUE) && (pageOffset != 0));
    }

    //
    // inner classes
    //

    /**
     * Does all the work that is shared between the different search methods. Implement the abstract methods to define the search and paging policy.
     */
    abstract class Engine
    {
        private final boolean preserveSort;

        private Engine(final boolean preserveSort)
        {
            this.preserveSort = preserveSort;
        }

        /**
         * Perform the actual search and return the Hits
         *
         * @return all hits the search returns
         * @throws IOException if things go horribly wrong
         */
        abstract TopDocs getHits() throws IOException;

        /**
         * Page the Hit list (transforms into a list of String reps of the ID).
         *
         * @param hits returned from the above {@link #getHits()} method
         * @return the search page
         */
        abstract Page page(TopDocs hits);

        /**
         * The actual engine method. Instantiate the engine and return the result of this method.
         *
         * @param user the searcher; results of this search may be affected by the particular searcher.
         * @return the results of the search, paged and converted to domain objects
         */
        SharedEntitySearchResult<S> search(User user)
        {
            TopDocs hits;
            try
            {
                hits = getHits();
            }
            catch (final IOException e)
            {
                ///CLOVER:OFF
                throw new RuntimeException(e);
                ///CLOVER:ON
            }
            final Page page = page(hits);
            final Ids ids = new Ids(transform(page.list(), Transformers.STRING_TO_LONG), preserveSort);
            return new SharedEntitySearchResult<S>(accessor.get(user, ids), page.hasNext(), hits.totalHits);
        }

        /**
         * The actual engine method. Instantiate the engine and return the result of this method.
         *
         * @return the results of the search, paged and converted to domain objects
         */
        SharedEntitySearchResult<S> search()
        {
            TopDocs hits;
            try
            {
                hits = getHits();
            }
            catch (final IOException e)
            {
                ///CLOVER:OFF
                throw new RuntimeException(e);
                ///CLOVER:ON
            }
            final Page page = page(hits);
            final Ids ids = new Ids(transform(page.list(), Transformers.STRING_TO_LONG), preserveSort);
            return new SharedEntitySearchResult<S>(accessor.get(ids), page.hasNext(), hits.totalHits);
        }
    }

    /**
     * Responsible for Paging and transforming the Hits
     */
    class Page
    {
        private final List<String> result;
        private final boolean hasNext;

        private Page(final TopDocs hits, final int pageOffset, final int pageWidth, final IndexSearcher searcher)
        {
            final int startOffset = Math.min(hits.totalHits, pageOffset * pageWidth);
            final int endOffset = Math.min(hits.totalHits, startOffset + pageWidth);

            final FieldHitCollector collector = new FieldHitCollector(searcher, "id");
            for (int i = startOffset; i < endOffset; i++)
            {
                try
                {
                    Document doc = searcher.doc(hits.scoreDocs[i].doc);
                    collector.collect(doc);
                }
                catch (final CorruptIndexException e)
                {
                    ///CLOVER:OFF
                    throw new RuntimeException(e);
                    ///CLOVER:ON
                }
                catch (final IOException e)
                {
                    ///CLOVER:OFF
                    throw new RuntimeException(e);
                    ///CLOVER:ON
                }
            }

            this.result = collector.getValues();
            this.hasNext = hits.totalHits > endOffset;
        }

        public List<String> list()
        {
            return result;
        }

        public boolean hasNext()
        {
            return hasNext;
        }
    }

    /**
     * All elements
     */
    class All extends Page
    {
        public All(final TopDocs hits, final IndexSearcher searcher)
        {
            super(hits, 0, hits.totalHits, searcher);
        }
    }

    private static final class Ids implements SharedEntityAccessor.RetrievalDescriptor
    {
        final List<Long> ids;
        final boolean preserveOrder;

        Ids(final Collection<Long> ids, final boolean preserveOrder)
        {
            this.ids = Collections.unmodifiableList(new ArrayList<Long>(ids));
            this.preserveOrder = preserveOrder;
        }

        public List<Long> getIds()
        {
            return ids;
        }

        public boolean preserveOrder()
        {
            return preserveOrder;
        }
    }

    private static class SortFieldFactory
    {
        SharedEntitySortField getSortField(final SharedEntityColumn column, final boolean reverse)
        {
            SharedEntityColumnDefinition columnDefinition = SharedEntityColumnDefinition.definitionFor(column);
            // must conditionally construct
            if (columnDefinition.isCustomSort())
            {
                return new SharedEntitySortField(columnDefinition, columnDefinition.createSortComparator(), reverse);
            }
            return new SharedEntitySortField(columnDefinition, reverse);
        }
    }
}
