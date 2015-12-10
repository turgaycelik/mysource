package com.atlassian.jira.scheduler;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.scheduler.status.RunOutcome;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v6.2
 */
public class RunDetailsFactory extends AbstractEntityFactory<OfBizRunDetails>
{
    public static final String ID = "id";
    public static final String JOB_ID = "jobId";
    public static final String START_TIME = "startTime";
    public static final String DURATION = "runDuration";
    public static final String OUTCOME = "runOutcome";
    public static final String MESSAGE = "infoMessage";

    // The indicator values used for each status
    public static final String OUTCOME_SUCCESS = "S";
    public static final String OUTCOME_UNAVAILABLE = "U";
    public static final String OUTCOME_ABORTED = "A";
    public static final String OUTCOME_FAILED = "F";



    @Override
    public String getEntityName()
    {
        return Entity.Name.RUN_DETAILS;
    }

    @Override
    public OfBizRunDetails build(GenericValue gv)
    {
        return new OfBizRunDetails(
                gv.getLong(ID),
                gv.getString(JOB_ID),
                gv.getTimestamp(START_TIME),
                toRunOutcome(gv.getString(OUTCOME)),
                gv.getLong(DURATION),
                gv.getString(MESSAGE));
    }

    @Override
    public Map<String,Object> fieldMapFrom(OfBizRunDetails jobStatus)
    {
        return new FieldMap()
                .add(ID, jobStatus.getId())
                .add(JOB_ID, jobStatus.getJobId())
                .add(START_TIME, toTimestamp(jobStatus.getStartTime()))
                .add(OUTCOME, toRunOutcomeIndicator(jobStatus.getRunOutcome()))
                .add(DURATION, jobStatus.getDurationInMillis())
                .add(MESSAGE, jobStatus.getMessage());
    }



    private static Timestamp toTimestamp(Date date)
    {
        return (date != null) ? new Timestamp(date.getTime()) : null;
    }

    private static String toRunOutcomeIndicator(RunOutcome runOutcome)
    {
        switch (runOutcome)
        {
            case SUCCESS:
                return OUTCOME_SUCCESS;
            case UNAVAILABLE:
                return OUTCOME_UNAVAILABLE;
            case ABORTED:
                return OUTCOME_ABORTED;
            case FAILED:
                return OUTCOME_FAILED;
        }
        throw new IllegalArgumentException("Unsupported run outcome: " + runOutcome);
    }

    private static RunOutcome toRunOutcome(String value)
    {
        if (value != null && value.length() == 1)
        {
            switch (value.charAt(0))
            {
                case 'S':
                    return RunOutcome.SUCCESS;
                case 'U':
                    return RunOutcome.UNAVAILABLE;
                case 'A':
                    return RunOutcome.ABORTED;
                case 'F':
                    return RunOutcome.FAILED;
            }
        }
        throw new IllegalArgumentException("Unsupported run outcome: " + value);
    }
}
