package com.atlassian.jira.scheduler;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.extension.Startable;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.core.spi.RunDetailsDao;
import com.atlassian.scheduler.status.RunDetails;
import com.atlassian.scheduler.status.RunOutcome;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.Entity.RUN_DETAILS;
import static com.atlassian.jira.scheduler.RunDetailsFactory.ID;
import static com.atlassian.jira.scheduler.RunDetailsFactory.JOB_ID;
import static com.atlassian.jira.scheduler.RunDetailsFactory.OUTCOME;
import static com.atlassian.jira.scheduler.RunDetailsFactory.OUTCOME_SUCCESS;
import static com.atlassian.jira.scheduler.RunDetailsFactory.START_TIME;
import static org.ofbiz.core.entity.EntityOperator.IN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;
import static org.ofbiz.core.entity.EntityOperator.NOT_EQUAL;

/**
 * Provides persistence of job status information for {@code atlassian-scheduler}.
 * <p>
 * For now, we will try to keep at most two records per job ID: the most recent run, and
 * if that was not successful then also the most recent run that was successful.  We will
 * tolerate duplicates by taking only the most recent run time when duplicates exist.
 * </p><p>
 * At startup, any history older than 90 days is automatically purged.  At this time,
 * there is no other mechanism in place for automatically trimming the history.
 * </p>
 *
 * @since v6.2
 */
public class OfBizRunDetailsDao implements RunDetailsDao
{
    private static final Logger LOG = Logger.getLogger(OfBizRunDetailsDao.class);
    private static final String START_TIME_DESC = START_TIME + " DESC";

    /**
     * On startup, this will automatically purge run details that are older than 90 days.
     * This prevents it from growing without bounds, and it is unlikely that you really
     * care about the results of a scheduled job that hasn't run in that long, anyway.
     */
    private static final long AUTOMATIC_PURGE_OFFSET_MILLIS = TimeUnit.DAYS.toMillis(90L);

    private final AtomicBoolean started = new AtomicBoolean();
    private final EntityEngine entityEngine;

    public OfBizRunDetailsDao(final EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }



    public void start()
    {
        if (started.compareAndSet(false, true))
        {
            purgeHistory(new Date(System.currentTimeMillis() - AUTOMATIC_PURGE_OFFSET_MILLIS));
        }
        else
        {
            LOG.warn("start() called while already started?!", new IllegalStateException());
        }
    }

    public void stop()
    {
        started.set(false);
    }



    @Nullable
    @Override
    public RunDetails getLastRunForJob(@Nonnull final JobId jobId)
    {
        if (!started.get())
        {
            warnNotStarted("Should not call getLastRunForJob() when the scheduler is in standby; returning null for jobId=" + jobId);
            return null;
        }
        return Select.from(RUN_DETAILS)
                .whereEqual(JOB_ID, jobId.toString())
                .orderBy(START_TIME_DESC)
                .limit(1)
                .runWith(entityEngine)
                .singleValue();
    }

    @Nullable
    @Override
    public RunDetails getLastSuccessfulRunForJob(@Nonnull final JobId jobId)
    {
        if (!started.get())
        {
            warnNotStarted("Should not call getLastSuccessfulRunForJob() when the scheduler is in standby; returning null for jobId=" + jobId);
            return null;
        }
        return Select.from(RUN_DETAILS)
                .whereEqual(JOB_ID, jobId.toString())
                .andEqual(OUTCOME, OUTCOME_SUCCESS)
                .orderBy(START_TIME_DESC)
                .limit(1)
                .runWith(entityEngine)
                .singleValue();
    }

    @Override
    public void addRunDetails(@Nonnull final JobId jobId, @Nonnull final RunDetails runDetails)
    {
        if (!started.get())
        {
            warnNotStarted("Cannot record run details when the scheduling system is not started: jobId=" + jobId +
                    "; runDetails=" + runDetails);
            return;
        }
        final List<Long> idsToRemove = isSuccess(runDetails) ? getAllIdsForJob(jobId) : getUnsuccessfulIdsForJob(jobId);
        entityEngine.createValue(RUN_DETAILS, new OfBizRunDetails(null, jobId.toString(), runDetails));
        if (!idsToRemove.isEmpty())
        {
            Delete.from(RUN_DETAILS)
                    .whereCondition(new EntityExpr(ID, IN, idsToRemove))
                    .execute(entityEngine);
        }
    }

    public List<OfBizRunDetails> getAll()
    {
        if (!started.get())
        {
            warnNotStarted("Should not call getAll() when the scheduler is in standby; returning an empty list");
            return Collections.emptyList();
        }
        return Select.from(Entity.Name.RUN_DETAILS)
                .orderBy(JOB_ID, START_TIME_DESC)
                .runWith(entityEngine)
                .consumeWith(new MostRecentForEachJobId());
    }

    public void purgeHistory(final Date before)
    {
        Delete.from(Entity.RUN_DETAILS)
                .whereCondition(new EntityExpr(START_TIME, LESS_THAN, new Timestamp(before.getTime())))
                .execute(entityEngine);
    }



    private List<Long> getAllIdsForJob(final JobId jobId)
    {
        return Select.id()
                .from(RUN_DETAILS)
                .whereEqual(JOB_ID, jobId.toString())
                .runWith(entityEngine)
                .asList();
    }

    private List<Long> getUnsuccessfulIdsForJob(final JobId jobId)
    {
        return Select.id()
                .from(RUN_DETAILS)
                .whereEqual(JOB_ID, jobId.toString())
                .whereCondition(new EntityExpr(OUTCOME, NOT_EQUAL, OUTCOME_SUCCESS))
                .runWith(entityEngine)
                .asList();
    }

    private static boolean isSuccess(final RunDetails runDetails)
    {
        return runDetails.getRunOutcome() == RunOutcome.SUCCESS;
    }

    private static void warnNotStarted(final String message)
    {
        // Includes a stack trace iff DEBUG is enabled (but logs at WARN either way)
        if (LOG.isDebugEnabled())
        {
            LOG.warn(message, new IllegalStateException());
        }
        else
        {
            LOG.warn(message);
        }
    }

    static class MostRecentForEachJobId implements EntityListConsumer<GenericValue, List<OfBizRunDetails>>
    {
        private final List<OfBizRunDetails> list = new LinkedList<OfBizRunDetails>();
        private String previousJobId = null;

        @Override
        public void consume(final GenericValue entity)
        {
            final String jobId = entity.getString(JOB_ID);
            if (!jobId.equals(previousJobId))
            {
                list.add(RUN_DETAILS.build(entity));
                previousJobId = jobId;
            }
        }

        @Override
        public List<OfBizRunDetails> result()
        {
            return list;
        }
    }
}
