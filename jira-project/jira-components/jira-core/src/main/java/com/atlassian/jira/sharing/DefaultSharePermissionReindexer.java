package com.atlassian.jira.sharing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.jira.index.AccumulatingResultBuilder;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.dbc.Assertions;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.Assertions.stateNotNull;

public class DefaultSharePermissionReindexer implements SharePermissionReindexer
{
    private final SharedEntityIndexer indexer;
    private final Set<Reindexer<? extends SharedEntity>> retrievers;

    public DefaultSharePermissionReindexer(final SharedEntityIndexer indexer)
    {
        Assertions.notNull("indexer", indexer);
        this.indexer = indexer;
        final Set<Reindexer<? extends SharedEntity>> retrievers = new HashSet<Reindexer<? extends SharedEntity>>(2);
        retrievers.add(new Reindexer<SearchRequest>(SearchRequest.ENTITY_TYPE));
        retrievers.add(new Reindexer<PortalPage>(PortalPage.ENTITY_TYPE));
        this.retrievers = Collections.unmodifiableSet(retrievers);
    }

    public void reindex(final SharePermission permission)
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        for (final Reindexer<? extends SharedEntity> element : retrievers)
        {
            builder.add(permission.getType().get(), permission.getId(), element.reindex(permission));
        }
        builder.toResult().await();
    }

    /**
     * Responsible for finding and then re-indexing {@link SharedEntity shared entities} that have a particular permission.
     */
    class Reindexer<S extends SharedEntity>
    {
        private final SharedEntity.TypeDescriptor<S> type;

        private Reindexer(final TypeDescriptor<S> type)
        {
            this.type = type;
        }

        Index.Result reindex(final SharePermission permission)
        {
            final SharedEntitySearchParameters params = new SharedEntitySearchParametersBuilder().setSharePermission(
                notNull("permission", permission)).toSearchParameters();
            final SharedEntitySearcher<S> searcher = indexer.getSearcher(type);
            stateNotNull("searcher", searcher);
            final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
            searcher.search(params).foreach(new Consumer<S>()
            {
                public void consume(final S element)
                {
                    builder.add(permission.getType().get(), permission.getId(), indexer.index(element));
                }
            });
            return builder.toResult();
        }
    }
}
