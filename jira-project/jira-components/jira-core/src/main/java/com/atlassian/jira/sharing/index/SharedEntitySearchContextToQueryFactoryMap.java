package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Determines which {@link QueryFactory} to be used for a given {@link SharedEntitySearchContext}
 *
 * @since v4.4.1
 */
public class SharedEntitySearchContextToQueryFactoryMap extends ForwardingMap<SharedEntitySearchContext, QueryFactory>
{
    private final Map<SharedEntitySearchContext, QueryFactory> searchContextQueryFactoryMap;

    public SharedEntitySearchContextToQueryFactoryMap(final PermissionQueryFactory permissionQueryFactory,
            final IsSharedQueryFactory isSharedQueryFactory)
    {
        this.searchContextQueryFactoryMap =
                ImmutableMap.of
                        (
                                SharedEntitySearchContext.USE, permissionQueryFactory,
                                SharedEntitySearchContext.ADMINISTER, isSharedQueryFactory
                        );
    }

    @Override
    protected Map<SharedEntitySearchContext, QueryFactory> delegate()
    {
        return searchContextQueryFactoryMap;
    }
}
