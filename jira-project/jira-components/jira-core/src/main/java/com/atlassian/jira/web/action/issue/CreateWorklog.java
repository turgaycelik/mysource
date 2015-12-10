package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
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

import java.util.Date;

/**
 * This action creates a new worklog
 */
public class CreateWorklog extends AbstractWorklogAction
{
    private static final String SECURITY_BREACH = "securitybreach";

    private Worklog worklog;
    private WorklogResult worklogResult;

    public CreateWorklog(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager, JiraDurationUtils jiraDurationUtils, OutlookDateManager outlookDateManager,
            FieldVisibilityManager fieldVisibilityManager, final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager, UserUtil userUtil, final FeatureManager featureManager)
    {
        super(worklogService, commentService, projectRoleManager, jiraDurationUtils, outlookDateManager, fieldVisibilityManager, fieldLayoutManager, rendererManager, userUtil, featureManager);
    }


    public String doDefault() throws Exception
    {
        // Setup the startDate to right now
        setStartDate(getFormattedStartDate(new Date()));
        return super.doDefault();
    }

    protected void doValidation()
    {
        try
        {
            //errors are added within getIssue()
            getIssue();
        }
        catch (IssuePermissionException ipe)
        {
            return;
        }
        catch (IssueNotFoundException infe)
        {
            return;
        }

        final CommentVisibility commentVisibility = getCommentVisibility();
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(commentVisibility.getGroupLevel(), commentVisibility.getRoleLevel());

        // Call the correct validation on the service so that we can get the worklog to create
        final WorklogInputParametersImpl.Builder builder = WorklogInputParametersImpl
                .issue(getIssueObject())
                .timeSpent(getTimeLogged())
                .startDate(getParsedStartDate())
                .comment(getComment())
                .visibility(visibility);
        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
        {
            final WorklogNewEstimateInputParameters params = builder
                    .newEstimate(getNewEstimate())
                    .buildNewEstimate();
            worklogResult = worklogService.validateCreateWithNewEstimate(getJiraServiceContext(), params);
        }
        else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(adjustEstimate))
        {
            final WorklogAdjustmentAmountInputParameters params = builder
                    .adjustmentAmount(getAdjustmentAmount())
                    .buildAdjustmentAmount();
            worklogResult = worklogService.validateCreateWithManuallyAdjustedEstimate(getJiraServiceContext(), params);
        }
        else
        {
            final WorklogInputParameters params = builder.build();
            worklogResult = worklogService.validateCreate(getJiraServiceContext(), params);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isTimeTrackingFieldHidden(getIssueObject()))
        {
            return SECURITY_BREACH;
        }
        
        // Based on how the user wants to update the remaining estimate we will call the correct do method on the service
        if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(adjustEstimate))
        {
            worklog = worklogService.createAndAutoAdjustRemainingEstimate(getJiraServiceContext(), worklogResult, true);
        }
        else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
        {
            worklog = worklogService.createWithNewRemainingEstimate(getJiraServiceContext(), (WorklogNewEstimateResult) worklogResult, true);
        }
        else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(adjustEstimate))
        {
            worklog = worklogService.createWithManuallyAdjustedEstimate(getJiraServiceContext(), (WorklogAdjustmentAmountResult) worklogResult, true);
        }
        else
        {
            worklog = worklogService.createAndRetainRemainingEstimate(getJiraServiceContext(), worklogResult, true);
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
     * Returns the worklog created
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

    /**
     * This method is used by the worklog JSP to distinguish between Create Worklog and Update Worklog.
     * On update, the "reduce estimated" option is meaningless, and this method is used to hide that option.
     *
     * @return true always, because this Action is "Create Worklog"
     */
    public boolean isCreateWorklog()
    {
        return true;
    }
}
