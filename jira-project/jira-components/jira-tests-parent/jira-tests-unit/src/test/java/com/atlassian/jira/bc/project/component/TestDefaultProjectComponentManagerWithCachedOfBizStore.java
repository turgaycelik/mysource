package com.atlassian.jira.bc.project.component;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

public class TestDefaultProjectComponentManagerWithCachedOfBizStore
        extends TestDefaultProjectComponentManagerWithOfBizStore
{

    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator, final CacheManager cacheManager, final ClusterLockService clusterLockService)
    {
        return new CachingProjectComponentStore(new OfBizProjectComponentStore(ofBizDelegator), cacheManager, clusterLockService);
    }
}
