package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.util.OutlookDateManager;

import java.math.BigDecimal;

/**
 * This action updates an existing worklog
 */
public class UpdateWorklog extends AbstractWorklogAction
{
    private Worklog worklog;
    private WorklogResult worklogResult;

    public UpdateWorklog(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager, JiraDurationUtils jiraDurationUtils, OutlookDateManager outlookDateManager,
            FieldVisibilityManager fieldVisibilityManager, final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager, UserUtil userUtil, final FeatureManager featureManager)
    {
        super(worklogService, commentService, projectRoleManager, jiraDurationUtils, outlookDateManager, fieldVisibilityManager, fieldLayoutManager, rendererManager, userUtil, featureManager);
    }

    public String doDefault() throws Exception
    {
        // Retrieve the worklog
        worklog = worklogService.getById(getJiraServiceContext(), getWorklogId());
        if (worklog == null)
        {
            addErrorMessage(getText("logwork.error.update.invalid.id", (getWorklogId() == null) ? null : getWorklogId().toString()));
            return ERROR;
        }
        if (!worklogService.hasPermissionToUpdate(getJiraServiceContext(), worklog))
        {
            //user doesn't have permission to run this action
            return "securitybreach";
        }

        //pre-populate the fields
        final BigDecimal hoursPerDay = getHoursPerDay();
        final BigDecimal daysPerWeek = getDaysPerWeek();
        final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
        final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
        final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
        setTimeLogged(DateUtils.getDurationStringSeconds(worklog.getTimeSpent(), secondsPerDay, secondsPerWeek));
        setStartDate(getFormattedStartDate(worklog.getStartDate()));
        setComment(worklog.getComment());
        setCommentLevel(CommentVisibility.getCommentLevelFromLevels(worklog.getGroupLevel(), worklog.getRoleLevelId()));

        return super.doDefault();
    }

    public void doValidation()
    {
        final CommentVisibility commentVisibility = getCommentVisibility();
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(commentVisibility.getGroupLevel(), commentVisibility.getRoleLevel());

        // Call the correct validation on the service so that we can get the worklog to update
        final WorklogInputParametersImpl.Builder builder = WorklogInputParametersImpl
                .timeSpent(getTimeLogged())
                .worklogId(getWorklogId())
                .startDate(getParsedStartDate())
                .comment(getComment())
                .visibility(visibility);
        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
        {
            final WorklogNewEstimateInputParameters params = builder
                    .newEstimate(getNewEstimate())
                    .buildNewEstimate();
            worklogResult = worklogService.validateUpdateWithNewEstimate(getJiraServiceContext(), params);
        }
        else
        {
            final WorklogInputParameters params = builder.build();
            worklogResult = worklogService.validateUpdate(getJiraServiceContext(), params);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Based on how the user wants to update the remaining estimate we will call the correct do method on the service
        if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(adjustEstimate))
        {
            worklogService.updateAndAutoAdjustRemainingEstimate(getJiraServiceContext(), worklogResult, true);
        }
        else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
        {
            worklogService.updateWithNewRemainingEstimate(getJiraServiceContext(), (WorklogNewEstimateResult) worklogResult, true);
        }
        else
        {
            worklogService.updateAndRetainRemainingEstimate(getJiraServiceContext(), worklogResult, true);
        }

        if (getHasErrorMessages())
        {
            return ERROR;
        }

        if (isInlineDialogMode())
        {
            return returnComplete();
        }
        return getRedirect("/browse/" + getIssue().getString("key"));
    }

    /**
     * Returns the worklog updated
     *
     * @return worklog
     * @since v3.10.1
     */
    public Worklog getWorklog()
    {
        if (worklog == null && worklogResult != null)
        {
            worklog = worklogResult.getWorklog();
        }
        return worklog;
    }

    public boolean isEditMode()
    {
        return true;
    }
}
