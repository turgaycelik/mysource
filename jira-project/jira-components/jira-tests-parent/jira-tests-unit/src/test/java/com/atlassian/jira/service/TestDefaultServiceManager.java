package com.atlassian.jira.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.configurable.ObjectConfigurationFactory;
import com.atlassian.configurable.XMLObjectConfigurationFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.ofbiz.MockOfBizPropertyEntryStore;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.MockComponentClassManager;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.services.DebugService;
import com.atlassian.jira.service.services.mail.MailQueueService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.scheduler.SchedulerHistoryService;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.RunMode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultServiceManager
{
    @AvailableInContainer
    private ObjectConfigurationFactory objectConfigurationFactory = new XMLObjectConfigurationFactory();
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
    @Mock
    @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @Mock @AvailableInContainer
    private SchedulerService mockSchedulerService;
    @Mock @AvailableInContainer
    private SchedulerHistoryService mockSchedulerHistoryService;
    @Mock @AvailableInContainer
    private ComponentClassManager mockComponentClassManager;
    @Mock @AvailableInContainer
    private ClusterMessagingService messagingService;
    @AvailableInContainer
    private OfBizPropertyEntryStore ofBizPropertyEntryStore = new MockOfBizPropertyEntryStore();
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);
    private CacheManager cacheManager;
    private ClusterLockService clusterLockService;

    @Before
    public void setUp() throws ClassNotFoundException
    {
        cacheManager = new MemoryCacheManager();
        clusterLockService = new SimpleClusterLockService();
        when(mockComponentClassManager.newInstance(DebugService.class.getName())).thenReturn(new DebugService());
        when(mockComponentClassManager.newInstance(MailQueueService.class.getName())).thenReturn(new MailQueueService());
    }

    @Test
    public void testAddService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        // make sure this is the only service configured
        assertTrue(dsm.getServices().size() == 1);

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

   @Test
    public void testAddLocalService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        dsm.addService(name, MailQueueService.class.getName(), 500);

        ArgumentCaptor<JobId> jobIdCaptor = ArgumentCaptor.forClass(JobId.class);
        ArgumentCaptor<JobConfig> jobConfigCaptor = ArgumentCaptor.forClass(JobConfig.class);
        verify(mockSchedulerService).scheduleJob(jobIdCaptor.capture(), jobConfigCaptor.capture());

        assertThat(jobConfigCaptor.getValue().getRunMode(), is(RunMode.RUN_LOCALLY));
    }

  @Test
    public void testAddNonLocalService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        dsm.addService(name, DebugService.class.getName(), 500);

        ArgumentCaptor<JobId> jobIdCaptor = ArgumentCaptor.forClass(JobId.class);
        ArgumentCaptor<JobConfig> jobConfigCaptor = ArgumentCaptor.forClass(JobConfig.class);
        verify(mockSchedulerService).scheduleJob(jobIdCaptor.capture(), jobConfigCaptor.capture());

        assertThat(jobConfigCaptor.getValue().getRunMode(), is(RunMode.RUN_ONCE_PER_CLUSTER));
    }

    @Test
    public void testAddServiceWithParams() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(),  new MockComponentClassManager());
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        Map<String, String[]> params = MapBuilder.newBuilder("debug param one", new String[]{"/tmp/"}).toMap();

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, DebugService.class.getName(), 500, params);

        // make sure this is the only service configured
        assertTrue(dsm.getServices().size() == 1);

        assertNotNull(jiraServiceContainer.getProperties());
        assertEquals("/tmp/", jiraServiceContainer.getProperty("debug param one"));

        jiraServiceContainer = dsm.getServiceWithId(jiraServiceContainer.getId());
        assertNotNull(jiraServiceContainer);
        assertNotNull(jiraServiceContainer.getProperties());
        assertEquals("/tmp/", jiraServiceContainer.getProperty("debug param one"));

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    @Test
    public void testAddServiceFailsForBadService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        try
        {
            dsm.addService(name, "com.stuff.otherStuff.ServiceClass", 500);
            fail("ClassNotFoundException expected.");
        }
        catch (ClassNotFoundException yay)
        {
            // Expected.
        }
    }

    @Test
    public void testEditServiceByName() throws Exception
    {
        final ComponentClassManager componentClassManager = new MockComponentClassManager();
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), componentClassManager);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, componentClassManager, null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        dsm.addService(name, "com.atlassian.jira.service.services.mail.MailQueueService", 500);

        dsm.editServiceByName(name, 8, new HashMap<String, String[]>(0));

        JiraServiceContainer jiraServiceContainer = notNull(dsm.getServiceWithName(name));
        assertEquals(jiraServiceContainer.getDelay(), 8);

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());

        verify(messagingService).registerListener(eq(DefaultServiceManager.RESCHEDULE_SERVICE), any(ClusterMessageConsumer.class));
        verify(messagingService).registerListener(eq(DefaultServiceManager.UNSCHEDULE_SERVICE), any(ClusterMessageConsumer.class));
        verify(messagingService, times(3)).sendRemote(anyString(), anyString());
    }

    @Test
    public void testEditServiceById() throws Exception
    {
        final ComponentClassManager componentClassManager = new MockComponentClassManager();
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), componentClassManager);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, componentClassManager, null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        dsm.editService(jiraServiceContainer.getId(), 8, new HashMap<String, String[]>(0));

        jiraServiceContainer = notNull(dsm.getServiceWithId(jiraServiceContainer.getId()));

        assertEquals(jiraServiceContainer.getDelay(), 8);

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    @Test
    public void testContainsServiceWithId() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        assertTrue(dsm.containsServiceWithId(jiraServiceContainer.getId()));
        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    @Test
    public void testRemoveServiceByNameNoService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(new MockOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, null, null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();
        final String name = "test service";

        try
        {
            dsm.removeServiceByName(name);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("No services with name '" + name + "' exist.", e.getMessage());
        }
        catch (Exception ex)
        {
            fail();
        }
    }

    @Test
    public void testRemoveServiceByNameMultipleServices() throws Exception
    {
        final String name = "test service";
        ComponentAccessor.getOfBizDelegator().createValue("ServiceConfig", FieldMap.build("name", name));
        ComponentAccessor.getOfBizDelegator().createValue("ServiceConfig", FieldMap.build("name", name));

        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(ComponentAccessor.getOfBizDelegator(), null);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, null, null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();

        try
        {
            dsm.removeServiceByName(name);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Multiple services with name '" + name + "' exist.", e.getMessage());
        }
        catch (Exception ex)
        {
            fail();
        }
    }

    @Test
    public void testRemoveServiceByName() throws Exception
    {
        final String name = "test service";
        ComponentAccessor.getOfBizDelegator().createValue("ServiceConfig", FieldMap.build("name", name, "time", 1L, "clazz", MailQueueService.class.getName()));

        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(ComponentAccessor.getOfBizDelegator(), mockComponentClassManager);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, null, null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();

        dsm.removeServiceByName(name);

        final List<GenericValue> services = ComponentAccessor.getOfBizDelegator().findByAnd("ServiceConfig", FieldMap.build("name", name));
        assertTrue(services.isEmpty());

        verify(messagingService).registerListener(eq(DefaultServiceManager.RESCHEDULE_SERVICE), any(ClusterMessageConsumer.class));
        verify(messagingService).registerListener(eq(DefaultServiceManager.UNSCHEDULE_SERVICE), any(ClusterMessageConsumer.class));
        verify(messagingService).sendRemote(eq(DefaultServiceManager.RESCHEDULE_SERVICE), anyString());
        verify(messagingService).sendRemote(eq(DefaultServiceManager.UNSCHEDULE_SERVICE), anyString());
    }

    @Test
    public void testRemoveService() throws Exception
    {
        final GenericValue service = ComponentAccessor.getOfBizDelegator().createValue("ServiceConfig", FieldMap.build(
                "name", "test service",
                "time", 1L,
                "clazz", DebugService.class.getName()));

        ServiceConfigStore serviceConfigStore = new OfBizServiceConfigStore(ComponentAccessor.getOfBizDelegator(), mockComponentClassManager);
        DefaultServiceManager dsm = new Fixture(serviceConfigStore, null, null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        dsm.start();

        dsm.removeService(service.getLong("id"));

        final List<GenericValue> services = ComponentAccessor.getOfBizDelegator().findByAnd("ServiceConfig", FieldMap.build("name", "test service"));
        assertTrue(services.isEmpty());
    }

    @Test
    public void testAnonymousUsersShouldNotBeAbleToManageAnyService() throws Exception
    {
        final List<JiraServiceContainer> storedServiceConfigs =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).build(),
                        new MockJiraServiceContainer.Builder().id(2).build(),
                        new MockJiraServiceContainer.Builder().id(3).build(),
                        new MockJiraServiceContainer.Builder().id(4).build()
                );

        ServiceConfigStore serviceConfigStore = new MockServiceConfigStore()
        {
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return storedServiceConfigs;
            }
        };

        DefaultServiceManager serviceManager = new Fixture(serviceConfigStore, new MockComponentClassManager(), null, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        serviceManager.start();

        User anAnonymousUser = null;
        Iterable<JiraServiceContainer> actualServicesManageableByAnAnonymousUser = serviceManager.getServicesManageableBy(anAnonymousUser);

        assertTrue(Iterables.isEmpty(actualServicesManageableByAnAnonymousUser));
    }

    @Test
    public void testSystemAdminUsersShouldBeAbleToManageAllServices() throws Exception
    {
        final List<JiraServiceContainer> storedServiceConfigs =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).build(),
                        new MockJiraServiceContainer.Builder().id(2).build(),
                        new MockJiraServiceContainer.Builder().id(3).build(),
                        new MockJiraServiceContainer.Builder().id(4).build()
                );

        ServiceConfigStore serviceConfigStore = new MockServiceConfigStore(){
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return storedServiceConfigs;
            }
        };


        User aSysAdmin = new MockUser("system-admin");

        MockPermissionManager mockPermissionManager = new MyPermissionManager(false, true);

        DefaultServiceManager serviceManager = new Fixture(serviceConfigStore, new MockComponentClassManager(),
                mockPermissionManager, null, mockSchedulerService, cacheManager, clusterLockService, messagingService);
        serviceManager.start();

        Iterable<JiraServiceContainer> actualServicesManageableByASysAdmin = serviceManager.getServicesManageableBy(aSysAdmin);

        assertThat(actualServicesManageableByASysAdmin,
                containsInAnyOrder(storedServiceConfigs.toArray(new JiraServiceContainer[storedServiceConfigs.size()])));
    }

    @Test
    public void testAdminUsersShouldBeAbleToManageOnlyPopAndImapServices() throws Exception
    {
        final List<JiraServiceContainer> expectedServiceConfigsForAnAdminUser =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(2).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(5).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build()
                );

        final List<JiraServiceContainer> storedServiceConfigs =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(2).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(3).serviceClass("com.test.TestService").build(),
                        new MockJiraServiceContainer.Builder().id(4).serviceClass("com.test.TestService").build(),
                        new MockJiraServiceContainer.Builder().id(5).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build()
                );

        ServiceConfigStore serviceConfigStore = new MockServiceConfigStore(){
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return storedServiceConfigs;
            }
        };

        User anAdminUser = new MockUser("jira-admin");

        MockPermissionManager mockPermissionManager = new MyPermissionManager(true, false);

        DefaultServiceManager serviceManager = new Fixture(serviceConfigStore, new MockComponentClassManager(),
                mockPermissionManager, new DefaultInBuiltServiceTypes(mockPermissionManager), mockSchedulerService,
                cacheManager, clusterLockService, messagingService);
        serviceManager.start();

        Iterable<JiraServiceContainer> actualServicesManageableByAnAdminUser = serviceManager.getServicesManageableBy(anAdminUser);

        assertListsEquals(Lists.newArrayList(expectedServiceConfigsForAnAdminUser), Lists.newArrayList(actualServicesManageableByAnAdminUser));
    }

    static void assertListsEquals(final List<JiraServiceContainer> expected, final Collection<JiraServiceContainer> got)
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

    static class MyPermissionManager extends MockPermissionManager
    {
        private final boolean isAdmin;
        private final boolean isSysAdmin;

        MyPermissionManager(final boolean isAdmin, final boolean isSysAdmin)
        {
            this.isAdmin = isAdmin;
            this.isSysAdmin = isSysAdmin;
        }

        @Override
        public boolean hasPermission(int permissionsId, User u)
        {
            if (Permissions.SYSTEM_ADMIN == permissionsId)
            {
                return isSysAdmin;
            }
            if (Permissions.ADMINISTER == permissionsId)
            {
                return isAdmin;
            }
            throw new UnsupportedOperationException();
        }
    }

    static class Fixture extends DefaultServiceManager
    {
        Fixture(final ServiceConfigStore serviceConfigStore, final ComponentClassManager componentClassManager, final PermissionManager permissionManager, final InBuiltServiceTypes inBuiltServiceTypes, final SchedulerService schedulerService,
                final CacheManager cacheManager, final ClusterLockService clusterLockService, final ClusterMessagingService messagingService)
        {
            super(serviceConfigStore, componentClassManager, permissionManager, inBuiltServiceTypes, schedulerService, cacheManager, clusterLockService, messagingService);
        }

        @Override
        boolean picoContainerComponentsRegistered()
        {
            // Pretendsies for testing
            return true;
        }
    }
}
