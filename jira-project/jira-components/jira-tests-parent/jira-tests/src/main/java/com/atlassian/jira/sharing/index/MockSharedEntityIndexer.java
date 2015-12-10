/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.MockResult;

import java.util.Collection;
import java.util.Set;

/**
 * @since v3.13
 */
public class MockSharedEntityIndexer implements SharedEntityIndexer
{
    @Override
    public Index.Result index(final SharedEntity entity)
    {
        return new MockResult();
    }

    @Override
    public Index.Result deIndex(final SharedEntity entity)
    {
        return new MockResult();
    }

    @Override
    public Index.Result index(final SharedEntity entity, final boolean updateReplicatedIndex)
    {
        return new MockResult();
    }

    @Override
    public Index.Result index(Set<SharedEntity> sharedEntities, boolean updateReplicatedIndex)
    {
        return new MockResult();
    }

    @Override
    public Index.Result deIndex(Set<SharedEntity> sharedEntities, boolean updateReplicatedIndex)
    {
        return new MockResult();
    }

    @Override
    public <S extends SharedEntity> SharedEntitySearcher<S> getSearcher(final SharedEntity.TypeDescriptor<S> type)
    {
        return null;
    }

    @Override
    public long optimize(final TypeDescriptor<?> type)
    {
        return 0;
    }

    @Override
    public String clear(final TypeDescriptor<?> type)
    {
        return null;
    }

    @Override
    public void shutdown(final TypeDescriptor<?> type)
    {}

    @Override
    public Collection<String> getAllIndexPaths()
    {
        return null;
    }

    @Override
    public void recreate(final TypeDescriptor<?> type)
    {}
}
