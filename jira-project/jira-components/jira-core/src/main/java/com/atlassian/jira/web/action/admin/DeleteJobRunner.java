package com.atlassian.jira.web.action.admin;

import java.util.Collection;

import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.scheduler.SchedulerRuntimeException;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.status.JobDetails;

import org.apache.commons.lang3.StringUtils;

/**
 * @since v6.3
 */
@WebSudoRequired
public class DeleteJobRunner extends JiraWebActionSupport
{
    private final SchedulerService schedulerService;
    private final String SCHEDULER_ADMIN_URL = "SchedulerAdmin.jspa";

    private boolean confirm;
    private String runnerKey;

    public DeleteJobRunner(final SchedulerService schedulerService)
    {
        this.schedulerService = schedulerService;
    }

    @Override
    public String execute() throws Exception
    {
        if (StringUtils.isBlank(runnerKey))
        {
            return getRedirect(SCHEDULER_ADMIN_URL);
        }

        final JobRunnerKey jobRunnerKey = JobRunnerKey.of(runnerKey);

        if (schedulerService.getRegisteredJobRunnerKeys().contains(jobRunnerKey))
        {
            addErrorMessage(getText("admin.schedulerdetails.only.abandoned", runnerKey));
            return ERROR;
        }

        if (StringUtils.isNotEmpty(runnerKey) && confirm)
        {
            final Collection<JobDetails> jobs = schedulerService.getJobsByJobRunnerKey(jobRunnerKey);
            for (JobDetails job : jobs)
            {
                try
                {
                    schedulerService.unscheduleJob(job.getJobId());
                }
                catch (SchedulerRuntimeException sre)
                {
                    addErrorMessage(getText("admin.schedulerdetails.failed.to.remove", job.getJobId().toString(), sre.getMessage()));
                }
            }
            if (hasAnyErrors())
            {
                return ERROR;
            }
            else
            {
                return returnCompleteWithInlineRedirect(SCHEDULER_ADMIN_URL);
            }
        }

        return INPUT;
    }

    @ActionViewData("error")
    @Override
    public Collection<String> getErrorMessages()
    {
        return super.getErrorMessages();
    }

    @ActionViewData
    @SuppressWarnings("unused")
    public String getRunnerKey()
    {
        return runnerKey;
    }

    @SuppressWarnings("unused")
    public void setRunnerKey(final String runnerKey)
    {
        this.runnerKey = runnerKey;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }
}
