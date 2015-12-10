package com.atlassian.jira.index.ha;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderByImpl;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * Resolves Shared Entities
 *
 * @since v6.1
 */
public class SharedEntityResolver
{
    final private SharedEntityAccessor.Factory accessorFactory;

    public SharedEntityResolver(SharedEntityAccessor.Factory accessorFactory)
    {
        this.accessorFactory = accessorFactory;
    }

    /**
     * Returns a collection of SharedEntity's given ids
     *
     * @param entityType  type of SharedEntity - either SearchRequest or PortalPage
     * @param ids   set of ids to get
     */
    public Collection<SharedEntity> getSharedEntities(ReplicatedIndexOperation.SharedEntityType entityType, Set<Long> ids)
    {
        final Set<SharedEntity> sharedEntities = Sets.newHashSet();
        SharedEntity.TypeDescriptor typeDescriptor = entityType.getTypeDescriptor();
        final SharedEntityAccessor sharedEntityAccessor = accessorFactory.getSharedEntityAccessor(typeDescriptor);
        for (Long id : ids)
        {
            final SharedEntity sharedEntity = sharedEntityAccessor.getSharedEntity(id);
            // The SharedEntity may have been deleted in the interim, so don't update if it has
            if (sharedEntity != null)
            {
                sharedEntities.add(sharedEntity);
            }
        }
        return sharedEntities;
    }


    /**
     * Returns a set of 'placeholder' SharedEntity's - they only have an id
     *
     * @param entityType type of SharedEntity - either SearchRequest or PortalPage
     * @param ids   set of ids
     */
    public Collection<SharedEntity> getDummySharedEntities(ReplicatedIndexOperation.SharedEntityType entityType, Set<Long> ids)
    {
        final Set<SharedEntity> sharedEntities = Sets.newHashSet();
        for (Long id : ids)
        {
            if (entityType.getTypeDescriptor().equals(SearchRequest.ENTITY_TYPE))
            {
                sharedEntities.add(new SearchRequest(new QueryImpl(null, new OrderByImpl(), null), "", "dummy", "dummyDescription", id, 0));
            }
            else
            {
                sharedEntities.add(PortalPage.name("dummy").id(id).build());
            }
        }
        return sharedEntities;
    }

}
