package com.atlassian.jira.service;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.log.Log4jKit;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import org.apache.log4j.Logger;

/**
 * Runs all services due for execution at the current time.
 */
public class ServiceRunner implements JobRunner
{
    private static final Logger LOG = Logger.getLogger(ServiceRunner.class);
    private static final String MOCK_USER_NAME = "ServiceRunner";


    @Nullable
    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest jobRunnerRequest)
    {
        // Get the job class to actually run
        final Map<String,Serializable> parameters = jobRunnerRequest.getJobConfig().getParameters();
        final Long serviceId = (Long) parameters.get(ServiceManager.SERVICE_ID_KEY);
        if (serviceId == null)
        {
            return JobRunnerResponse.failed("Service entry with job ID '" + jobRunnerRequest.getJobId() + "' has no job config parameters");
        }
        return runServiceId(serviceId);
    }

    private static JobRunnerResponse runServiceId(final Long serviceId)
    {
        JobRunnerResponse response;
        final JiraServiceContainer service;
        try
        {
            final ServiceManager serviceManager = ComponentAccessor.getComponent(ServiceManager.class);
            service = serviceManager.getServiceWithId(serviceId);
            if (service == null)
            {
                response = JobRunnerResponse.aborted("Service ID '" + serviceId + "' no longer exists!");
            }
            else
            {
                response = runService(service);
            }
        }
        catch (Exception e)
        {
            LOG.error("An error occurred while trying to run service with ID '" + serviceId + "'. " + e.getMessage(), e);
            response = JobRunnerResponse.failed(e);
        }
        return response;
    }

    private static JobRunnerResponse runService(final JiraServiceContainer service)
    {
        // make the logs come out with the name of the running service
        final String serviceName = service.getName();
        setLog4JInfo(MOCK_USER_NAME, serviceName);
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Running Service [" + service + ']');
            }
            service.run();
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Finished Running Service [" + service + ']');
            }
            return JobRunnerResponse.success();
        }
        catch (final RuntimeException e)
        {
            LOG.error("An error occurred while trying to run service '" + serviceName + "'. " + e.getMessage(), e);
            return JobRunnerResponse.failed(e);
        }
        finally
        {
            service.setLastRun();
            setLog4JInfo(MOCK_USER_NAME, "");
        }

    }

    /**
     * The ServiceRunner can run inside a different thread each time.  But the Log4J MDC ins an inheritable thread local
     * and hence will inherit the details fo the web request that started the scheduler.  So we override it here
     * explicitly. Ideally we would do this in the quartz scheduler thread itself however we cant control this easily.
     *
     * @param userName    the user name to use
     * @param serviceName the running service name
     */
    private static void setLog4JInfo(final String userName, final String serviceName)
    {
        Log4jKit.clearMDC();
        Log4jKit.putToMDC(userName, "", "", serviceName, "");
    }
}
