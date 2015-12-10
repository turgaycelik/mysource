package com.atlassian.jira.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.service.services.LocalService;
import com.atlassian.jira.util.thread.JiraThreadLocalUtils;
import com.atlassian.scheduler.SchedulerHistoryService;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.status.RunDetails;

import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Proxies calls to JiraService and manages delay between calls.
 */
public class JiraServiceContainerImpl implements JiraServiceContainer
{
    private static final Logger log = Logger.getLogger(JiraServiceContainerImpl.class);

    private final Long id;
    private final JiraService service;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile long delay;

    public JiraServiceContainerImpl(final JiraService service, final Long id)
    {
        this.service = service;
        this.id = id;
    }

    public void init(final PropertySet props) throws ObjectConfigurationException
    {
        service.init(props);
    }

    /**
     * Perform the action of this service.  The caller of this method assumes that no housekeeping has been done, and
     * will call setLastRun() after the run() method.
     * <p/>
     * init() is guaranteed to be called before run(), but the parameters passed to init() are not guaranteed to be
     * correct.  Any parameters expected to be set by init() should be checked in this method upon each invocation.
     *
     * @see com.atlassian.jira.service.JiraService#init
     */
    public void run()
    {
        if (!running.compareAndSet(false, true))
        {
            //
            // its possible (BUT very unlikely) that a Thread race condition allows this to be the list
            // possible services but it's already running. So we don't really care because we simply return
            // and do nothing.  See JRA-19542
            //
            log.debug("Service was inexplicably in the possible list twice. No harm done.  Just returning without doing anything.");
            return;
        }
        JiraThreadLocalUtils.preCall();
        try
        {
            service.run();
        }
        finally
        {
            running.set(false);
            JiraThreadLocalUtils.postCall(log, new JiraThreadLocalUtils.ProblemDeterminationCallback()
            {
                public void onOpenTransaction()
                {
                    log.error("Connection not cleared from thread local while running service.");
                    if (service != null)
                    {
                        log.error("Service name: " + service.getName());
                    }
                    else
                    {
                        log.error("The service is null");
                    }
                }
            });
        }
    }

    /**
     * This method is called when the service is unloaded (usually when the web application or server
     * is being shut down).
     * <p/>
     * You may wish to remove any connections that you have established, eg. database connections.
     */
    public void destroy()
    {
        service.destroy();
    }

    /**
     * Indicates whether administrators can delete this service from within the web interface.
     * <p/>
     * Generally only Atlassian services should return true from this.
     */
    public boolean isInternal()
    {
        return service.isInternal();
    }

    /**
     * The time that this service was last run.  Generally this is called
     * after the service has executed, to ensure that there is X delay between
     * finish of previous run & start of the next one.
     *
     * @return The time in milliseconds that this service was last run.
     */
    public final long getLastRun()
    {
        SchedulerHistoryService schedulerHistoryService = ComponentAccessor.getComponent(SchedulerHistoryService.class);
        RunDetails lastRunDetails = schedulerHistoryService.getLastRunForJob(toJobId(id));
        return (lastRunDetails != null) ? lastRunDetails.getStartTime().getTime() : 0L;
    }

    private static JobId toJobId(final long serviceId)
    {
        return JobId.of(JiraService.class.getName() + ':' + serviceId);
    }

    /**
     * Set the last run time to be the current time.  As we do not assume that users will
     * call this in their run method, all code that calls run() should also call this afterwards.
     *
     * @see #run
     */
    public final void setLastRun()
    {
        // We silently Ignore this.
    }

    /**
     * Sets the delay between invocation of each service.
     *
     * @param delay The time in milliseconds between runs of the service.
     */
    public void setDelay(final long delay)
    {
        this.delay = delay;
    }

    /** The length of delay in milliseconds between runs of this service. */
    public long getDelay()
    {
        return delay;
    }

    public boolean isDueAt(final long time)
    {
        return (getLastRun() + getDelay()) <= time;
    }

    public boolean isUnique()
    {
        return service.isUnique();
    }

    public String getDescription()
    {
        return service.getDescription();
    }

    public String getName()
    {
        return service.getName();
    }

    public void setName(final String name)
    {
        service.setName(name);
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return service.getObjectConfiguration();
    }

    public boolean hasProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return service.hasProperty(propertyKey);
    }

    public String getProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return service.getProperty(propertyKey);
    }

    public String getTextProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return service.getTextProperty(propertyKey);
    }

    public Long getLongProperty(final String propertyKey) throws ObjectConfigurationException
    {
        final String property = getProperty(propertyKey);
        try
        {
            return new Long(property);
        }
        catch (final NumberFormatException e)
        {
            throw new ObjectConfigurationException("Could not get Long from " + property);
        }
    }

    public String getDefaultProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return getObjectConfiguration().getFieldDefault(propertyKey);
    }

    public PropertySet getProperties() throws ObjectConfigurationException
    {
        return service.getProperties();
    }

    /**
     * Whether a service is currently running.  This is needed for services that may potentially run
     * for longer than their delay period.  Eg a service that is run every minute, but takes 2 mins to run.
     */
    public boolean isRunning()
    {
        return running.get();
    }

    public String getKey()
    {
        throw new UnsupportedOperationException();
    }

    public Long getId()
    {
        return id;
    }

    public String getServiceClass()
    {
        return getServiceClassObject().getName();
    }

    @Nonnull
    @Override
    public Class<? extends JiraService> getServiceClassObject()
    {
        return service.getClass();
    }

    public boolean isUsable()
    {
        if (PluginBackedService.class.isAssignableFrom(getServiceClassObject()))
        {
            return ((PluginBackedService) service).isAvailable();
        }

        // JIRA-backed services are always usable
        return true;
    }

    @Override
    public boolean isLocalService()
    {
        return LocalService.class.isAssignableFrom(service.getClass());
    }

    @Override
    public String toString()
    {
        return "Container: " + service.getClass().getName() + ' ' + (getDescription() != null ? getDescription() : "") + " delay [" + getDelay() + "ms]";
    }
}
