package com.atlassian.jira.scheduler;

import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.core.spi.RunDetailsDao;
import com.atlassian.scheduler.quartz1.Quartz1SchedulerService;
import com.atlassian.scheduler.quartz1.spi.Quartz1SchedulerConfiguration;

/**
 * Wraps the scheduler service so that lifecycle events are passed on to our RunDetailsDao.
 *
 * @since v6.3
 */
public class JiraQuartz1SchedulerService extends Quartz1SchedulerService
{
    private final RunDetailsDao runDetailsDao;

    public JiraQuartz1SchedulerService(final RunDetailsDao runDetailsDao, final Quartz1SchedulerConfiguration config)
            throws SchedulerServiceException
    {
        super(runDetailsDao, config);
        this.runDetailsDao = runDetailsDao;
    }

    // Starts the run details DAO first so any fast jobs that trigger during misfire handling can save status
    // successfully, but stops it again if the scheduler fails to start normally.
    @Override
    protected void startImpl() throws SchedulerServiceException
    {
        boolean ok = false;
        try
        {
            startRunDetailsDao();
            super.startImpl();
            ok = true;
        }
        finally
        {
            if (!ok)
            {
                stopRunDetailsDao();
            }
        }

    }

    @Override
    protected void standbyImpl() throws SchedulerServiceException
    {
        super.standbyImpl();
        stopRunDetailsDao();
    }

    @Override
    protected void shutdownImpl()
    {
        super.shutdownImpl();
        stopRunDetailsDao();
    }

    @SuppressWarnings("CastToConcreteClass")
    private void startRunDetailsDao()
    {
        if (runDetailsDao instanceof OfBizRunDetailsDao)
        {
            ((OfBizRunDetailsDao)runDetailsDao).start();
        }
    }

    @SuppressWarnings("CastToConcreteClass")
    private void stopRunDetailsDao()
    {
        if (runDetailsDao instanceof OfBizRunDetailsDao)
        {
            ((OfBizRunDetailsDao)runDetailsDao).stop();
        }
    }
}
