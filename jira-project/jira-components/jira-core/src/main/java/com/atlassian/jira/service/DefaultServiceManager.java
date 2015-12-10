/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.util.concurrent.LazyReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang.StringUtils.isNumeric;

@EventComponent
public class DefaultServiceManager implements ServiceManager, Startable
{
    final static String RESCHEDULE_SERVICE = "Reschedule Service";

    final static String UNSCHEDULE_SERVICE = "Unschedule Service";

    private static final Logger LOG = Logger.getLogger(DefaultServiceManager.class);

    private static final JobRunnerKey SERVICE_JOB_KEY = JobRunnerKey.of(DefaultServiceManager.class.getName());

    static final String UPDATE_LOCK = DefaultServiceManager.class.getName() + ".updateLock";

    private final ServiceConfigStore serviceConfigStore;
    private final CachedReference<Map<Long, JiraServiceContainer>> servicesReference;
    private final ComponentClassManager componentClassManager;
    private final PermissionManager permissionManager;
    private final InBuiltServiceTypes inBuiltServiceTypes;
    private final SchedulerService schedulerService;
    private final ClusterMessagingService messagingService;
    private final LazyReference<ClusterLock> updateLockRef;
    private final MessageConsumer messageConsumer;

    public DefaultServiceManager(final ServiceConfigStore serviceConfigStore, final ComponentClassManager componentClassManager,
            final PermissionManager permissionManager, final InBuiltServiceTypes inBuiltServiceTypes, final SchedulerService schedulerService,
            final CacheManager cacheManager, final ClusterLockService clusterLockService, final ClusterMessagingService messagingService)
    {
        this.permissionManager = permissionManager;
        this.inBuiltServiceTypes = inBuiltServiceTypes;
        this.schedulerService = schedulerService;
        this.messagingService = messagingService;
        this.serviceConfigStore = notNull("serviceConfigStore", serviceConfigStore);
        this.componentClassManager = componentClassManager;
        this.servicesReference = cacheManager.getCachedReference(
                DefaultServiceManager.class.getName() + ".servicesReference",
                new ServicesCacheSupplier() );
        this.updateLockRef = new LazyReference<ClusterLock>()
        {
            @Override
            protected ClusterLock create() throws Exception
            {
                return clusterLockService.getLockForName(UPDATE_LOCK);
            }
        };
        this.messageConsumer = new MessageConsumer();
    }

    @Override
    public void start()
    {
        messagingService.registerListener(RESCHEDULE_SERVICE, messageConsumer);
        messagingService.registerListener(UNSCHEDULE_SERVICE, messageConsumer);

        schedulerService.registerJobRunner(SERVICE_JOB_KEY, new ServiceRunner());
        ensureServicesScheduled();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        if (!TRUE.equals(event.getProperty(AbstractService.SERVICE_EVENT)))
        {
            refreshAll();
        }
    }

    public Collection<JiraServiceContainer> getServices()
    {
        return Collections.unmodifiableCollection(getServiceCache().values());
    }

    @Override
    public Iterable<JiraServiceContainer> getServicesManageableBy(final User user)
    {
        if (isAnonymous(user))
        {
            return ImmutableSet.of();
        }
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
        {
            return getServices();
        }
        if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            final Set<String> names = getServiceClassNames(inBuiltServiceTypes.manageableBy(user));
            return Iterables.filter(getServices(), new Predicate<JiraServiceContainer>()
            {
                @Override
                public boolean apply(final JiraServiceContainer service)
                {
                    return names.contains(service.getServiceClass());
                }
            });
        }
        return ImmutableSet.of();
    }

    private static Set<String> getServiceClassNames(final Set<InBuiltServiceTypes.InBuiltServiceType> builtInTypes)
    {
        final Set<String> names = Sets.newHashSetWithExpectedSize(builtInTypes.size());
        for (InBuiltServiceTypes.InBuiltServiceType serviceType : builtInTypes)
        {
            names.add(serviceType.getType().getName());
        }
        return names;
    }

    @Override
    public void runNow(final long serviceId) throws Exception
    {
        JiraServiceContainer jiraServiceContainer = getServiceWithId(serviceId);
        if (jiraServiceContainer == null)
        {
            throw new ServiceException("Service with id '"  + serviceId + "' was not found");
        }

        JobConfig config = JobConfig.forJobRunnerKey(SERVICE_JOB_KEY)
                .withSchedule(Schedule.runOnce(null))
                .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
                .withParameters(ImmutableMap.<String, Serializable>of(SERVICE_ID_KEY, serviceId));

        JobId jobId = schedulerService.scheduleJobWithGeneratedId(config);
        LOG.debug("JIRA Service '" + jiraServiceContainer.getName() + "' scheduled for immediate execution with job id '" + jobId + '\'');
    }

    @Override
    public Iterable<JiraServiceContainer> getServicesForExecution(final long time)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean containsServiceWithId(final Long id)
    {
        return getServiceCache().containsKey(id);
    }


    @Override
    public void refreshAll()
    {
        servicesReference.reset();
        ensureServicesScheduled();
    }

    @Override
    public JiraServiceContainer getServiceWithId(final Long id) throws Exception
    {
        return getServiceCache().get(id);
    }

    @Nullable
    @Override
    public JiraServiceContainer getServiceWithName(final String name) throws Exception
    {
        final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForName(name);
        if ((jiraServiceContainer != null) && getServiceCache().containsKey(jiraServiceContainer.getId()))
        {
            return jiraServiceContainer;
        }
        else
        {
            return null;
        }
    }

    @Override
    public JiraServiceContainer addService(final String name, final String serviceClassName, final long delay)
            throws ServiceException, ClassNotFoundException
    {
        return addService(name, serviceClassName, delay, null);
    }

    @Override
    public JiraServiceContainer addService(final String name, final String serviceClassName, final long delay, final Map<String, String[]> params)
            throws ServiceException, ClassNotFoundException
    {
        if (StringUtils.isBlank(serviceClassName))
        {
            throw new ServiceException("The service class name must not be blank");
        }
        // Load the service class using the ComponentClassManager so we include plugins2 classes including plugins that are not enabled yet.
        final Class<JiraService> serviceClass = componentClassManager.loadClass(serviceClassName);

        return addService(name, serviceClass, delay, params);
    }

    public JiraServiceContainer addService(String name, Class<? extends JiraService> serviceClass, long delay)
            throws ServiceException
    {
        return addService(name, serviceClass, delay, null);
    }

    public JiraServiceContainer addService(String name, Class<? extends JiraService> serviceClass, long delay, Map<String, String[]> params)
            throws ServiceException
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            // Add the service to the map of services
            final JiraServiceContainer serviceContainer = serviceConfigStore.addServiceConfig(name, serviceClass, delay);

            if (params != null)
            {
                //update the service with the correct params
                serviceConfigStore.editServiceConfig(serviceContainer, delay, params);
            }
            try
            {
                scheduleJob(serviceContainer);
            }
            catch (SchedulerServiceException e)
            {
                throw new RuntimeException(e);
            }
            servicesReference.reset();
            return serviceContainer;
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public void editServiceByName(final String name, final long delay, final Map<String, String[]> params)
            throws Exception
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            final JiraServiceContainer serviceContainer = serviceConfigStore.getServiceConfigForName(name);
            if (serviceContainer == null)
            {
                throw new IllegalArgumentException("There is no ServiceConfig with name: " + name);
            }
            else if (!serviceContainer.isUsable())
            {
                throw new IllegalStateException("You can not edit an unloadable service");
            }
            serviceConfigStore.editServiceConfig(serviceContainer, delay, params);
            // Reschedule the job.
            scheduleJob(serviceContainer);
            servicesReference.reset();
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public void editService(final Long id, final long delay, final Map<String, String[]> params)
            throws Exception
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            final JiraServiceContainer serviceContainer = serviceConfigStore.getServiceConfigForId(id);
            serviceConfigStore.editServiceConfig(serviceContainer, delay, params);
            // Reschedule the job.
            scheduleJob(serviceContainer);
            servicesReference.reset();
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public void removeServiceByName(final String name) throws Exception
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForName(name);
            if (jiraServiceContainer == null)
            {
                throw new IllegalArgumentException("No services with name '" + name + "' exist.");
            }

            removeService(jiraServiceContainer);
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public void removeService(final Long id) throws Exception
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForId(id);
            removeService(jiraServiceContainer);
        }
        finally
        {
            updateLock.unlock();
        }
    }

    private void removeService(final JiraServiceContainer jiraServiceContainer)
    {
        serviceConfigStore.removeServiceConfig(jiraServiceContainer);
        unscheduleJob(jiraServiceContainer);
        servicesReference.reset();
    }

    public void refreshService(final Long id) throws Exception
    {
        refreshService(id, true);
    }

    private void refreshService(final Long id, boolean notify) throws Exception
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            final JiraServiceContainer serviceContainer = serviceConfigStore.getServiceConfigForId(id);
            // Reschedule the job.
            if (serviceContainer != null)
            {
                scheduleJob(serviceContainer, notify);
            }
            servicesReference.reset();
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public void refreshServiceByName(final String name) throws Exception
    {
        final Lock updateLock = getUpdateLock();
        updateLock.lock();
        try
        {
            final JiraServiceContainer serviceContainer = serviceConfigStore.getServiceConfigForName(name);
            // Reschedule the job.
            if (serviceContainer != null)
            {
                scheduleJob(serviceContainer);
            }
            servicesReference.reset();
        }
        finally
        {
            updateLock.unlock();
        }
    }

    /**
     * Returns service schedule skipper
     */
    public ServiceScheduleSkipper getScheduleSkipper()
    {
        throw new UnsupportedOperationException("The service skipper is no longer the way to run one service. Call 'runNow(long serviceId)' ");
    }

    @VisibleForTesting
    boolean picoContainerComponentsRegistered()
    {
        return ComponentManager.getInstance().getState().isComponentsRegistered();
    }

    private void unscheduleJob(final Long serviceId)
    {
        schedulerService.unscheduleJob(toJobId(serviceId));
    }

    private void unscheduleJob(final JiraServiceContainer jiraServiceContainer)
    {
        unscheduleJob(jiraServiceContainer.getId());

        if (jiraServiceContainer.isUsable() && jiraServiceContainer.isLocalService())
        {
            messagingService.sendRemote(UNSCHEDULE_SERVICE, jiraServiceContainer.getId().toString());
        }
    }

    private void scheduleJob(final JiraServiceContainer jiraServiceContainer) throws SchedulerServiceException
    {
        scheduleJob(jiraServiceContainer, true);
    }

    private void scheduleJob(final JiraServiceContainer jiraServiceContainer, final boolean notify) throws SchedulerServiceException
    {
        if (jiraServiceContainer.getDelay() <= 0)
        {
            return;
        }
        // Work out the fire time delay.
        Date nextFireTime = null;
        long lastRunMillis = jiraServiceContainer.getLastRun();
        final long nextRunMillis =
                lastRunMillis > 0 ? lastRunMillis + jiraServiceContainer.getDelay() : System.currentTimeMillis();
        final boolean localService = jiraServiceContainer.isLocalService();

        if (nextRunMillis > System.currentTimeMillis())
        {
            nextFireTime = new Date(nextRunMillis);
        }
        JobConfig config = JobConfig.forJobRunnerKey(SERVICE_JOB_KEY)
                .withSchedule(Schedule.forInterval(jiraServiceContainer.getDelay(), nextFireTime))
                .withRunMode(localService ? RunMode.RUN_LOCALLY : RunMode.RUN_ONCE_PER_CLUSTER)
                .withParameters(ImmutableMap.<String, Serializable>of(SERVICE_ID_KEY, jiraServiceContainer.getId()));

        schedulerService.scheduleJob(toJobId(jiraServiceContainer.getId()), config);

        if (localService && notify)
        {
            messagingService.sendRemote(RESCHEDULE_SERVICE, jiraServiceContainer.getId().toString());
        }
    }

    private static JobId toJobId(final long serviceId)
    {
        return JobId.of(JiraService.class.getName() + ':' + serviceId);
    }

    private Map<Long, JiraServiceContainer> getServiceCache()
    {
        if (!picoContainerComponentsRegistered())
        {
            throw new IllegalStateException("It is illegal to call the ServiceManager before all components are loaded. Please use " + Startable.class + " to get notified when JIRA has started.");
        }
        return servicesReference.get();
    }

    private void ensureServicesScheduled()
    {
        for (JiraServiceContainer jiraServiceContainer : getServices())
        {
            if (jiraServiceContainer.isUsable())
            {
                ensureServiceScheduled(jiraServiceContainer);
            }
        }
    }

    private void ensureServiceScheduled(JiraServiceContainer jiraServiceContainer)
    {
        if (schedulerService.getJobDetails(toJobId(jiraServiceContainer.getId())) == null)
        {
            try
            {
                scheduleJob(jiraServiceContainer);
            }
            catch (Exception e)
            {
                LOG.warn("Unable to schedule service '" + jiraServiceContainer.getName() + "', " + e.getMessage());
            }
        }
    }

    // To hide the misleading null safety warning triggered by LazyReference extending WeakReference
    private Lock getUpdateLock()
    {
        return updateLockRef.get();
    }


    private class ServicesCacheSupplier implements Supplier<Map<Long, JiraServiceContainer>>
    {
        @Override
        public Map<Long, JiraServiceContainer> get()
        {
            try
            {
                return loadServiceConfigs();
            }
            catch (final Exception t)
            {
                LOG.error("Could not configure services: ", t);
                return ImmutableMap.of();
            }
        }

        private Map<Long,JiraServiceContainer> loadServiceConfigs()
        {
            final Collection<JiraServiceContainer> serviceConfigs = serviceConfigStore.getAllServiceConfigs();
            if (serviceConfigs == null || serviceConfigs.isEmpty())
            {
                LOG.debug("No Services to Load");
                return ImmutableMap.of();
            }

            final Map<Long, JiraServiceContainer> services = Maps.newHashMapWithExpectedSize(serviceConfigs.size());
            for (final JiraServiceContainer jiraServiceContainer : serviceConfigs)
            {
                services.put(jiraServiceContainer.getId(), jiraServiceContainer);
            }
            return ImmutableMap.copyOf(services);
        }
    }

    private class MessageConsumer implements ClusterMessageConsumer
    {
        @Override
        public void receive(final String channel, final String message, final String senderId)
        {
            if (isNumeric(message))
            {
                try
                {
                    if (RESCHEDULE_SERVICE.equals(channel))
                    {
                        refreshService(Long.valueOf(message), false);
                    }
                    else if (UNSCHEDULE_SERVICE.equals(channel))
                    {
                        unscheduleJob(Long.valueOf(message));
                    }
                }
                catch(Exception e)
                {
                    LOG.error("Error refreshing services", e);
                }
            }
        }
    }
}