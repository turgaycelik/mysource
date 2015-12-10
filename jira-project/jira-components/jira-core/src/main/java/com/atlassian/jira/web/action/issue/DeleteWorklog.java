package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.util.OutlookDateManager;

/**
 * This action deletes an existing worklog
 */
public class DeleteWorklog extends AbstractWorklogAction
{
    private Worklog worklog;
    private WorklogResult worklogResult;
    private WorklogManager worklogManager;

    public DeleteWorklog(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager, JiraDurationUtils jiraDurationUtils, OutlookDateManager outlookDateManager,
            FieldVisibilityManager fieldVisibilityManager, final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager, final WorklogManager worklogManager, UserUtil userUtil, final FeatureManager featureManager)
    {
        super(worklogService, commentService, projectRoleManager, jiraDurationUtils, outlookDateManager, fieldVisibilityManager, fieldLayoutManager, rendererManager, userUtil, featureManager);
        this.worklogManager = worklogManager;
    }

    public String doDefault() throws Exception
    {
        // Retrieve the worklog. Use the manager to bypass permission checks because we are going to look at the delete
        // permission on our own.
        worklog = worklogManager.getById(getWorklogId());
        if (worklog == null)
        {
            addErrorMessage(getText("logwork.error.update.invalid.id", (getWorklogId() == null) ? null : getWorklogId().toString()));
            return ERROR;
        }
        if (!worklogService.hasPermissionToDelete(getJiraServiceContext(), worklog))
        {
            //user doesn't have permission to run this action
            return "securitybreach";
        }
        return super.doDefault();
    }

    public void doValidation()
    {
        // Call the correct validation on the service so that we can get the worklog to update
        if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
        {
            worklogResult = worklogService.validateDeleteWithNewEstimate(getJiraServiceContext(), getWorklogId(), getNewEstimate());
        }
        else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(adjustEstimate))
        {
            worklogResult = worklogService.validateDeleteWithManuallyAdjustedEstimate(getJiraServiceContext(), getWorklogId(), getAdjustmentAmount());
        }
        else
        {
            worklogResult = worklogService.validateDelete(getJiraServiceContext(), getWorklogId());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Based on how the user wants to update the remaining estimate we will call the correct do method on the service
        if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(adjustEstimate))
        {
            worklogService.deleteAndAutoAdjustRemainingEstimate(getJiraServiceContext(), worklogResult, true);
        }
        else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(adjustEstimate))
        {
            worklogService.deleteWithNewRemainingEstimate(getJiraServiceContext(), (WorklogNewEstimateResult) worklogResult, true);
        }
        else if (ADJUST_ESTIMATE_MANUAL.equalsIgnoreCase(adjustEstimate))
        {
            worklogService.deleteWithManuallyAdjustedEstimate(getJiraServiceContext(), (WorklogAdjustmentAmountResult) worklogResult, true);
        }
        else
        {
            worklogService.deleteAndRetainRemainingEstimate(getJiraServiceContext(), worklogResult, true);
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
     * Returns the worklog being deleted
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

}
