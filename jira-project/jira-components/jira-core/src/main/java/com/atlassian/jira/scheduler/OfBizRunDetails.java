package com.atlassian.jira.scheduler;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.core.status.RunDetailsImpl;
import com.atlassian.scheduler.status.RunDetails;
import com.atlassian.scheduler.status.RunOutcome;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.2
 */
public class OfBizRunDetails implements RunDetails
{
    private final Long id;
    private final String jobId;
    private final RunDetails delegate;

    public OfBizRunDetails(final Long id, final String jobId, final RunDetails runDetails)
    {
        this.id = id;
        this.jobId = notNull("jobId", jobId);
        this.delegate = notNull("runDetails", runDetails);
    }

    public OfBizRunDetails(final Long id, final String jobId, final Date startTime, final RunOutcome runOutcome, final long durationInMillis, @Nullable final String message)
    {
        this(id, jobId, new RunDetailsImpl(startTime, runOutcome, durationInMillis, message));
    }



    public Long getId()
    {
        return id;
    }

    public String getJobId()
    {
        return jobId;
    }

    @Override
    @Nonnull
    public Date getStartTime()
    {
        return delegate.getStartTime();
    }

    @Override
    public long getDurationInMillis()
    {
        return delegate.getDurationInMillis();
    }

    @Override
    @Nonnull
    public RunOutcome getRunOutcome()
    {
        return delegate.getRunOutcome();
    }

    @Override
    @Nonnull
    public String getMessage()
    {
        return delegate.getMessage();
    }



    @Override
    public String toString()
    {
        return "OfBizRunDetails[id=" + id + ",jobId=" + jobId + ",delegate=" + delegate + ']';
    }
}
