package com.atlassian.jira.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.MockComponentClassManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.service.services.DebugService;
import com.atlassian.scheduler.SchedulerService;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test for JRA-15879
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultServiceManagerConcurrentModEx
{
    @Mock
    private SchedulerService mockSchedulerService;

    @Mock
    private ClusterMessagingService messagingService;

    @Test
    public void testGetServicesDoesntThrowConcurrentMod() throws Exception
    {
        final List<JiraServiceContainer> services = Lists.<JiraServiceContainer>newArrayList(new MockJiraServiceContainer.Builder().id(1).build(), new MockJiraServiceContainer.Builder().id(2).build());
        final DefaultServiceManager manager = new DefaultServiceManagerForTest(new MockServiceConfigStore()
        {
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return services;
            }

            @Override
            public JiraServiceContainer addServiceConfig(final String name, final Class<? extends JiraService> clazz, final long delay)
            {
                return new MockJiraServiceContainer.Builder().id(3).serviceClass(DebugService.class.getName()).build();
            }
        }, new MockComponentClassManager(), null, null, mockSchedulerService, messagingService);
        manager.start();

        assertListsEquals(services, manager.getServices());

        final Iterator<JiraServiceContainer> it = manager.getServices().iterator();
        it.next();
        manager.addService("fred", MockJiraServiceContainer.class.getName(), 1000000);
        //shouldn't throw ConcurrentModificationException
        it.next();
    }

    void assertListsEquals(final List<JiraServiceContainer> expected, final Collection<JiraServiceContainer> got)
    {
        final Comparator<JiraServiceContainer> dumbIdComparator = new Comparator<JiraServiceContainer>()
        {
            public int compare(final JiraServiceContainer o1, final JiraServiceContainer o2)
            {
                return (int) (o1.getId() - o2.getId());
            }
        };

        final List<JiraServiceContainer> sortedExpectedServices = new ArrayList<JiraServiceContainer>(expected);
        Collections.sort(sortedExpectedServices, dumbIdComparator);

        final List<JiraServiceContainer> sortedGotServices = new ArrayList<JiraServiceContainer>(got);
        Collections.sort(sortedGotServices, dumbIdComparator);

        assertEquals(expected, sortedGotServices);
    }

    private Collection<JiraServiceContainer> makeList(final Iterable<JiraServiceContainer> servicesForExecution)
    {
        List<JiraServiceContainer> list = new ArrayList<JiraServiceContainer>();
        for (JiraServiceContainer jiraServiceContainer : servicesForExecution)
        {
            list.add(jiraServiceContainer);
        }
        return list;
    }

    private static class DefaultServiceManagerForTest extends DefaultServiceManager
    {
        public DefaultServiceManagerForTest(final ServiceConfigStore serviceConfigStore, final ComponentClassManager componentClassManager, final PermissionManager permissionManager, final InBuiltServiceTypes inBuiltServiceTypes,
                SchedulerService schedulerService, ClusterMessagingService messagingService)
        {
            super(serviceConfigStore, componentClassManager, permissionManager, inBuiltServiceTypes,
                    schedulerService, new MemoryCacheManager(), new SimpleClusterLockService(), messagingService);
        }

        @Override
        boolean picoContainerComponentsRegistered()
        {
            return true;
        }
    }
}
