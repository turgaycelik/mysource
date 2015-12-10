/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesUtil;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.index.AccumulatingResultBuilder;
import com.atlassian.jira.index.DefaultConfiguration;
import com.atlassian.jira.index.DefaultIndex;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.index.Indexes;
import com.atlassian.jira.index.ha.ReplicatedIndexManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Function;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import static com.atlassian.jira.index.Operations.newDelete;
import static com.atlassian.jira.index.Operations.newOptimize;
import static com.atlassian.jira.index.Operations.newUpdate;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * default implementation
 *
 * @since v3.13
 */
public class DefaultSharedEntityIndexer implements SharedEntityIndexer
{
    private static final Logger log = Logger.getLogger(DefaultSharedEntityIndexer.class);
    private static final int DEFAULT_WAIT_TIME_SECONDS = 30;

    private final QueryFactory queryFactory;
    private final SharedEntityAccessor.Factory accessorFactory;
    private final IndexFactory indexFactory;
    private final ReplicatedIndexManager replicatedIndexManager;
    private final ReentrantReadWriteLock lock;
    private final RuntimeException failToAcquireLock = new RuntimeException()
    {
        @Override
        public Throwable fillInStackTrace()
        {
            return this;
        }
    };

    // typed EntityDocumentFactory, needs to be extended to handle different entity types if required
    private final EntityDocumentFactory documentFactory;

    /*
     * Production dependencies constructor.
     */
    public DefaultSharedEntityIndexer(final IndexPathManager indexPathManager, final ShareTypeFactory shareTypeFactory,
            final SharedEntitySearchContextToQueryFactoryMap searchContextToQueryFactoryMap,
            final SharedEntityAccessor.Factory accessorFactory, final DirectoryFactory directoryFactory,
            final UserManager userManager, final ReplicatedIndexManager replicatedIndexManager,
            final ApplicationProperties applicationProperties)
    {
        this(new DefaultQueryFactory(shareTypeFactory, searchContextToQueryFactoryMap, userManager), accessorFactory,
                new DefaultEntityDocumentFactory(shareTypeFactory), directoryFactory, replicatedIndexManager, applicationProperties);
    }

    /*
     * Main constructor.
     */
    DefaultSharedEntityIndexer(final QueryFactory queryFactory, final SharedEntityAccessor.Factory accessorFactory,
            final EntityDocumentFactory documentFactory, final DirectoryFactory directoryFactory, final ReplicatedIndexManager replicatedIndexManager, final ApplicationProperties applicationProperties)
    {
        this.queryFactory = notNull("queryFactory", queryFactory);
        this.accessorFactory = notNull("accessorFactory", accessorFactory);
        this.documentFactory = notNull("documentFactory", documentFactory);
        indexFactory = new IndexFactory(directoryFactory, applicationProperties);
        this.replicatedIndexManager = replicatedIndexManager;
        lock = new ReentrantReadWriteLock();
    }

    public Index.Result index(final SharedEntity entity)
    {
        return index(entity, true);
    }

    public Index.Result deIndex(final SharedEntity entity)
    {
        if (!getReadLock())
        {
            return new DefaultIndex.Failure(failToAcquireLock);
        }
        try
        {
            replicatedIndexManager.deIndexSharedEntity(entity);
            return indexFactory.delete(documentFactory.get(entity));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Index.Result index(final SharedEntity entity, final boolean updateReplicatedIndex)
    {
        if (!getReadLock())
        {
            return new DefaultIndex.Failure(failToAcquireLock);
        }
        try
        {
            if (updateReplicatedIndex)
            {
                replicatedIndexManager.indexSharedEntity(entity);
            }
            return indexFactory.update(documentFactory.get(entity));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Index.Result index(final Set<SharedEntity> sharedEntities, boolean updateReplicatedIndex)
    {
        if (!getReadLock())
        {
            return new DefaultIndex.Failure(failToAcquireLock);
        }
        try
        {
            final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
            for (SharedEntity entity: sharedEntities)
            {
                if (updateReplicatedIndex)
                {
                    replicatedIndexManager.indexSharedEntity(entity);
                }
                builder.add(entity.getEntityType().getName(), entity.getId(), indexFactory.update(documentFactory.get(entity)));
            }
            return builder.toResult();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Index.Result deIndex(final Set<SharedEntity> sharedEntities, boolean updateReplicatedIndex)
    {
        if (!getReadLock())
        {
            return new DefaultIndex.Failure(failToAcquireLock);
        }
        try
        {
            final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
            for (SharedEntity entity: sharedEntities)
            {
                replicatedIndexManager.deIndexSharedEntity(entity);
                builder.add(entity.getEntityType().getName(), entity.getId(), indexFactory.delete(documentFactory.get(entity)));
            }
            return builder.toResult();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public <S extends SharedEntity> SharedEntitySearcher<S> getSearcher(final TypeDescriptor<S> type)
    {
        if (!getReadLock())
        {
            return null;
        }
        try
        {
            final SharedEntityAccessor<S> sharedEntityAccessor = accessorFactory.getSharedEntityAccessor(type);
            return new DefaultSharedEntitySearcher<S>(new IndexSearcherFactory()
            {
                public IndexSearcher get()
                {
                    return indexFactory.get(type, IndexFactory.Create.YES).openSearcher();
                }
            }, sharedEntityAccessor, queryFactory);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    private boolean getReadLock()
    {
        return getLock(lock.readLock(), "Failed to acquire SharedEntity index read lock");
    }

    private boolean getWriteLock()
    {
        return getLock(lock.writeLock(), "Failed to acquire SharedEntity index write lock");
    }

    private boolean getLock(Lock lock, String message)
    {
        try
        {
            if(lock.tryLock(DEFAULT_WAIT_TIME_SECONDS, TimeUnit.SECONDS))
            {
                return true;
            }
            log.warn(message);
        }
        catch (InterruptedException e)
        {
            log.warn(message, e);
        }
        return false;
    }

    public long optimize(final TypeDescriptor<?> type)
    {
        final long start = System.nanoTime();
        if (getWriteLock())
        {
            try
            {
                indexFactory.get(type, IndexFactory.Create.YES).getIndex().perform(newOptimize()).await();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    public String clear(final TypeDescriptor<?> type)
    {
        if (getWriteLock())
        {
            try
            {
                indexFactory.get(type, IndexFactory.Create.NO).deleteIndexDirectory();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return "todo-something here?";
    }

    public void recreate(final TypeDescriptor<?> type)
    {
        if (getWriteLock())
        {
            try
            {
                indexFactory.get(type, IndexFactory.Create.YES).deleteIndexDirectory();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
    }

    public void shutdown(final TypeDescriptor<?> type)
    {
        try
        {
            indexFactory.get(type, IndexFactory.Create.NO).close();
        }
        catch (final Exception ignore)
        {}
    }

    public Collection<String> getAllIndexPaths()
    {
        return Collections.emptyList();
    }

    public interface EntityDocumentFactory
    {
        /**
         * Get the {@link EntityDocument} for the entity.
         *
         * @param entity the {@link SharedEntity} to index
         * @return the {@link EntityDocument} - must not be null.
         */
        EntityDocument get(SharedEntity entity);
    }

    /**
     * Holds a {@link Document} and an identifying {@link Term} so we can delete any pre-existing document.
     */
    public interface EntityDocument
    {
        TypeDescriptor<?> getType();

        Term getIdentifyingTerm();

        Document getDocument();
    }

    static class DefaultEntityDocumentFactory implements EntityDocumentFactory
    {
        private final SharedEntityDocumentFactory documentFactory;

        DefaultEntityDocumentFactory(final ShareTypeFactory shareTypeFactory)
        {
            documentFactory = createDocumentFactory(shareTypeFactory);
        }

        /**
         * Package level protected for tests.
         * @param shareTypeFactory for getting a DocumentFactory
         * @return the document factory
         */
        ///CLOVER:OFF
        SharedEntityDocumentFactory createDocumentFactory(final ShareTypeFactory shareTypeFactory)
        {
            return DefaultDocumentFactory.create(shareTypeFactory);
        }

        ///CLOVER:ON

        public EntityDocument get(final SharedEntity entity)
        {
            return new EntityDocument()
            {
                private final Term identifyingTerm = new Term(SharedEntityFieldFactory.Default.ID.getFieldName(), entity.getId().toString());

                public Term getIdentifyingTerm()
                {
                    return identifyingTerm;
                }

                public Document getDocument()
                {
                    return documentFactory.create(entity);
                }

                public TypeDescriptor<?> getType()
                {
                    return entity.getEntityType();
                }
            };
        }
    }

    private final static class IndexFactory
    {
        private final Function<TypeDescriptor<?>, Directory> directoryFactory;
        @ClusterSafe
        private final ConcurrentMap<TypeDescriptor<?>, Index.Manager> managers = new ConcurrentHashMap<TypeDescriptor<?>, Index.Manager>();
        private final ApplicationProperties applicationProperties;

        IndexFactory(final Function<TypeDescriptor<?>, Directory> directoryFactory, final ApplicationProperties applicationProperties)
        {
            this.directoryFactory = notNull("directoryFactory", directoryFactory);
            this.applicationProperties = applicationProperties;
        }

        Index.Manager get(final TypeDescriptor<?> type, final Create option)
        {
            Index.Manager result = managers.get(type);
            while ((result == null) && option.create())
            {
                int maxQueueSize = PropertiesUtil.getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.SharedEntity.MAX_QUEUE_SIZE, 1000);
                managers.put(type, Indexes.createQueuedIndexManager(type.getName(), new DefaultConfiguration(directoryFactory.get(type),
                    QueryBuilder.Analyzers.LOWERCASE), maxQueueSize));
                result = managers.get(type);
            }
            return (result != null) ? result : NULL_MANAGER;
        }

        Index.Result delete(final EntityDocument document)
        {
            final Index index = get(document.getType(), Create.YES).getIndex();
            return index.perform(newDelete(document.getIdentifyingTerm(), UpdateMode.INTERACTIVE));
        }

        Index.Result update(final EntityDocument document)
        {
            final Index index = get(document.getType(), Create.YES).getIndex();
            return index.perform(newUpdate(document.getIdentifyingTerm(), document.getDocument(), UpdateMode.INTERACTIVE));
        }

        enum Create
        {
            YES(true),
            NO(false);

            private Create(final boolean create)
            {
                this.create = create;
            }

            boolean create()
            {
                return create;
            }

            private final boolean create;
        }
    }

    /**
     * used only when closing and shutting down so we don't create stray index directories
     */
    private static final Index.Manager NULL_MANAGER = new Index.Manager()
    {
        public void close()
        {}

        public void deleteIndexDirectory()
        {}

        public Index getIndex()
        {
            throw new UnsupportedOperationException();
        }

        public IndexSearcher openSearcher()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isIndexCreated()
        {
            throw new UnsupportedOperationException();
        }
    };
}
