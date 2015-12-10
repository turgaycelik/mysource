/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.index.AccumulatingResultBuilder;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.IndexingStrategy;
import com.atlassian.jira.index.MultiThreadedIndexingConfiguration;
import com.atlassian.jira.index.MultiThreadedIndexingStrategy;
import com.atlassian.jira.index.SimpleIndexingStrategy;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.EnclosedIterable;

import static com.atlassian.jira.config.properties.PropertiesUtil.getIntProperty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default IndexManager for {@link SharedEntity shared entities}
 *
 * @since v3.13
 */
public class DefaultSharedEntityIndexManager implements SharedEntityIndexManager
{
    private final SharedEntityIndexer indexer;
    private final FileFactory fileFactory;
    private final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration;

    private final IndexingStrategy simpleIndexingStrategy = new SimpleIndexingStrategy();

    private final SharedEntity.TypeDescriptor<?>[] types = new SharedEntity.TypeDescriptor[] { SearchRequest.ENTITY_TYPE, PortalPage.ENTITY_TYPE };

    private final Map<SharedEntity.TypeDescriptor<?>, Retriever<? extends SharedEntity>> retrievers;

    public DefaultSharedEntityIndexManager(
            final SharedEntityIndexer indexer,
            final SearchRequestManager searchRequestManager,
            final PortalPageManager portalPageManager,
            final FileFactory fileFactory,
            final ApplicationProperties applicationProperties)
    {
        this.indexer = indexer;
        this.fileFactory = fileFactory;

        final Map<SharedEntity.TypeDescriptor<?>, Retriever<? extends SharedEntity>> retrievers = new LinkedHashMap<TypeDescriptor<?>, Retriever<? extends SharedEntity>>(
            types.length);
        retrievers.put(SearchRequest.ENTITY_TYPE, new Retriever<SharedEntity>()
        {
            public EnclosedIterable<SharedEntity> getAll()
            {
                return searchRequestManager.getAllIndexableSharedEntities();
            }
        });
        retrievers.put(PortalPage.ENTITY_TYPE, new Retriever<SharedEntity>()
        {
            public EnclosedIterable<SharedEntity> getAll()
            {
                return portalPageManager.getAllIndexableSharedEntities();
            }
        });
        this.retrievers = Collections.unmodifiableMap(retrievers);

        this.multiThreadedIndexingConfiguration = new PropertiesAdapter(applicationProperties);
    }

    public long reIndexAll(final Context context)
    {
        notNull("event", context);
        long result = 0;

        for (final Map.Entry<SharedEntity.TypeDescriptor<?>, Retriever<? extends SharedEntity>> entry : retrievers.entrySet())
        {
            indexer.recreate(entry.getKey());
            result += reIndex(context, entry.getKey(), entry.getValue());
        }
        return result;
    }

    private <S extends SharedEntity> long reIndex(final Context context, final TypeDescriptor<?> type, final Retriever<S> retriever)
    {
        context.setName(type.getName());
        final long start = System.currentTimeMillis();
        final EnclosedIterable<S> all = retriever.getAll();
        final IndexingStrategy strategy = getStrategy(all.size());
        try
        {
            final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
            all.foreach(new Consumer<S>()
            {
                public void consume(final S entity)
                {
                    final Context.Task task = context.start(entity);
                    builder.add(entity.getEntityType().getName(), entity.getId(), strategy.get(new Supplier<Index.Result>()
                    {
                        public Index.Result get()
                        {
                            try
                            {
                                return indexer.index(entity, false);
                            }
                            finally
                            {
                                task.complete();
                            }
                        }
                    }));
                }
            });
            builder.toResult().await();
            return System.currentTimeMillis() - start;
        }
        finally
        {
            strategy.close();
        }
    }

    private <S> IndexingStrategy getStrategy(final int count)
    {
        if (count < multiThreadedIndexingConfiguration.minimumBatchSize())
        {
            return simpleIndexingStrategy;
        }
        return new MultiThreadedIndexingStrategy(simpleIndexingStrategy, multiThreadedIndexingConfiguration, "SharedEntityIndexer");
    }

    public long reIndexAll() throws IndexException
    {
        return reIndexAll(Contexts.nullContext());
    }

    @Override
    public long reIndexAllIssuesInBackground(Context context)
    {
        return reIndexAll(context);
    }

    @Override
    public long reIndexAllIssuesInBackground(final Context context, final boolean reIndexComments, final boolean reIndexChangeHistory)
    {
        return reIndexAll(context);
    }

    public long optimize()
    {
        long result = 0;
        for (final TypeDescriptor<?> type : types)
        {
            result += indexer.optimize(type);
        }
        return result;
    }

    public void shutdown()
    {
        for (final TypeDescriptor<?> type : types)
        {
            indexer.shutdown(type);
        }
    }

    @Override
    public long activate(final Context context, final boolean reindex)
    {
        return activate(context);
    }

    public long activate(final Context context)
    {
        notNull("event", context);
        return reIndexAll(context);
    }

    public void deactivate()
    {
        for (final TypeDescriptor<?> type : types)
        {
            clear(type);
        }
    }

    public boolean isIndexingEnabled()
    {
        // TODO consider retiring from the Lifecycle interface, only really implemented in DefaultIndexManager
        return true;
    }

    @Override
    public boolean isIndexAvailable()
    {
        // TODO consider retiring from the Lifecycle interface, only really implemented in DefaultIndexManager
        return true;
    }

    public boolean isIndexConsistent()
    {
        // TODO actually do something useful here.
        // There isn't currently any way to check this without actually doing a search request, which
        // may be slow.  If you ask to search when the index doesn't exist yet, it simply gets created
        // anyway.
        return true;
    }

    public Collection<String> getAllIndexPaths()
    {
        return indexer.getAllIndexPaths();
    }

    public int size()
    {
        return sum(sizes());
    }

    public boolean isEmpty()
    {
        for (final Retriever<? extends SharedEntity> element : retrievers.values())
        {
            if (!element.getAll().isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    private int sum(final int[] ints)
    {
        int result = 0;
        for (final int j : ints)
        {
            result += j;
        }
        return result;
    }

    private int[] sizes()
    {
        final int[] sizes = new int[retrievers.size()];
        int i = 0;
        for (final Retriever<? extends SharedEntity> element : retrievers.values())
        {
            sizes[i++] = element.getAll().size();
        }
        return sizes;
    }

    @Override
    public String toString()
    {
        return "SharedEntityIndexManager: paths: " + getAllIndexPaths();
    }

    /**
     * Nuke the index directory.
     *
     * @param type
     */
    private void clear(final TypeDescriptor<?> type)
    {
        fileFactory.removeDirectoryIfExists(indexer.clear(type));
    }

    interface Retriever<S extends SharedEntity>
    {
        /**
         * Get all entities so we can re-index them.
         *
         * @return a CloseableIterable over all entities for a type.
         */
        EnclosedIterable<S> getAll();
    }

    static class PropertiesAdapter implements MultiThreadedIndexingConfiguration
    {
        private final ApplicationProperties applicationProperties;

        PropertiesAdapter(ApplicationProperties applicationProperties)
        {
            this.applicationProperties = notNull("applicationProperties", applicationProperties);
        }

        public int minimumBatchSize()
        {
            return getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.SharedEntity.MIN_BATCH_SIZE, 50);
        }

        public int maximumQueueSize()
        {
            return getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.SharedEntity.MAX_QUEUE_SIZE, 1000);
        }

        public int noOfThreads()
        {
            return getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.SharedEntity.THREADS, 10);
        }
    }
}
