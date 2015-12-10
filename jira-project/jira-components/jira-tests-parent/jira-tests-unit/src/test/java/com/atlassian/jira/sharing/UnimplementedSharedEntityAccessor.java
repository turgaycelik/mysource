package com.atlassian.jira.sharing;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.util.collect.EnclosedIterable;

public class UnimplementedSharedEntityAccessor implements SharedEntityAccessor
{
    @Override
    public TypeDescriptor getType()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void adjustFavouriteCount(final SharedEntity entity, final int adjustmentValue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnclosedIterable get(final User user, final RetrievalDescriptor descriptor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnclosedIterable getAllIndexableSharedEntities()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnclosedIterable get(final RetrievalDescriptor descriptor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedEntity getSharedEntity(final Long entityId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedEntity getSharedEntity(final User user, final Long entityId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermissionToUse(final User user, final SharedEntity entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnclosedIterable getAll()
    {
        throw new UnsupportedOperationException();
    }
}
