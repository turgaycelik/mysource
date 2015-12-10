package com.atlassian.jira.index;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

/**
 * Simple context for searcher extractors
 */
@ThreadSafe
public class GenericSearchExtractorContext<T> implements EntitySearchExtractor.Context<T>
{
    private final T entity;
    private final String indexName;

    public GenericSearchExtractorContext(@Nonnull final T entity, @Nonnull final String indexName)
    {
        this.entity = Preconditions.checkNotNull(entity, "entity cannot be null");
        this.indexName = Preconditions.checkNotNull(indexName, "index name cannot be null");
    }

    @Override
    public T getEntity()
    {
        return entity;
    }

    @Override
    public String getIndexName()
    {
        return indexName;
    }
}
