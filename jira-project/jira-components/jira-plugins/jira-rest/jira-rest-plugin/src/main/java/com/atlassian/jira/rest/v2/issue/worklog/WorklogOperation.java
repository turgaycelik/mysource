package com.atlassian.jira.rest.v2.issue.worklog;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.WorklogSystemField;
import com.atlassian.jira.issue.fields.rest.WorklogRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Performs/Provides operation specific logic for worklog rest operations.
 */
public enum WorklogOperation
{
    ADD
    {
        @Override
        public Worklog validateAndPerformAndLeaveEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogResult worklogResult = getWorklogService().validateCreate(serviceContext, worklogInputParameters);
            return getWorklogService().createAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public Worklog validateAndPerformAndAutoAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogResult worklogResult = getWorklogService().validateCreate(serviceContext, worklogInputParameters);
            return getWorklogService().createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public Worklog validateAndPerformAndManualAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogAdjustmentAmountResult worklogResult = getWorklogService().validateCreateWithManuallyAdjustedEstimate(serviceContext, (WorklogAdjustmentAmountInputParameters) worklogInputParameters);
            return getWorklogService().createWithManuallyAdjustedEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public Worklog validateAndPerformAndSetNewEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogNewEstimateResult worklogResult = getWorklogService().validateCreateWithNewEstimate(serviceContext, (WorklogNewEstimateInputParameters) worklogInputParameters);
            return getWorklogService().createWithNewRemainingEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public WorklogInputParameters prepareData(JiraServiceContextImpl serviceContext, Issue issue, WorklogJsonBean request, WorklogResource.WorklogAdjustmentRequest adjustment)
        {
            ErrorCollection errors = serviceContext.getErrorCollection();
            WorklogInputParametersImpl.Builder builder = WorklogInputParametersImpl
                    .issue(issue);

            addAdjustmentParams(adjustment, builder, errors, ADD, serviceContext);
            if (!errors.hasAnyErrors())
            {
                String timeSpent = request.getTimeSpent();
                Long timeSpentSeconds = request.getTimeSpentSeconds();
                if (timeSpent != null && timeSpentSeconds != null)
                {
                    errors.addError("timeSpent", serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.manual.increaseBy.missing"));
                }
                if (timeSpent != null)
                {
                    builder.timeSpent(timeSpent);
                }
                else if (timeSpentSeconds != null)
                {
                    timeSpent = getTimeLoggedString(timeSpentSeconds);
                    builder.timeSpent(timeSpent);
                }

                builder.startDate(request.getStarted() != null ? request.getStarted() : new Date());
                builder.comment(request.getComment());
                builder.visibility(Visibilities.fromVisibilityBean(request.getVisibility(), getProjectRoleManager()));

                return builder.buildAll();
            }

            return null;
        }

        @Override
        public Response prepareSuccessfulResponse(WorklogJsonBean result)
        {
            return Response.created(result.getSelf()).entity(result).cacheControl(never()).build();
        }
    },
    DELETE
    {
        @Override
        public Worklog validateAndPerformAndLeaveEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogResult worklogResult = getWorklogService().validateDelete(serviceContext, worklogInputParameters.getWorklogId());
            boolean success = getWorklogService().deleteAndRetainRemainingEstimate(serviceContext, worklogResult, true);
            return success ? worklogResult.getWorklog() : null;
        }

        @Override
        public Worklog validateAndPerformAndAutoAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogResult worklogResult = getWorklogService().validateDelete(serviceContext, worklogInputParameters.getWorklogId());
            boolean success = getWorklogService().deleteAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
            return success ? worklogResult.getWorklog() : null;
        }

        @Override
        public Worklog validateAndPerformAndManualAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogAdjustmentAmountResult worklogResult = getWorklogService().validateDeleteWithManuallyAdjustedEstimate(serviceContext, worklogInputParameters.getWorklogId(), ((WorklogAdjustmentAmountInputParameters) worklogInputParameters).getAdjustmentAmount());
            boolean success = getWorklogService().deleteWithManuallyAdjustedEstimate(serviceContext, worklogResult, true);
            return success ? worklogResult.getWorklog() : null;
        }

        @Override
        public Worklog validateAndPerformAndSetNewEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogNewEstimateResult worklogResult = getWorklogService().validateDeleteWithNewEstimate(serviceContext, worklogInputParameters.getWorklogId(), ((WorklogNewEstimateInputParameters) worklogInputParameters).getNewEstimate());
            boolean success = getWorklogService().deleteWithNewRemainingEstimate(serviceContext, worklogResult, true);
            return success ? worklogResult.getWorklog() : null;
        }

        @Override
        public WorklogInputParameters prepareData(JiraServiceContextImpl serviceContext, Issue issue, WorklogJsonBean request, WorklogResource.WorklogAdjustmentRequest adjustment)
        {
            ErrorCollection errors = serviceContext.getErrorCollection();
            Worklog existingWorklog = getAndValidateExistingWorklog(request, errors, serviceContext);
            if (!errors.hasAnyErrors())
            {
                WorklogInputParametersImpl.Builder builder = WorklogInputParametersImpl
                        .issue(issue);

                addAdjustmentParams(adjustment, builder, errors, DELETE, serviceContext);
                if (!errors.hasAnyErrors())
                {
                    builder.worklogId(existingWorklog.getId());
                    return builder.buildAll();
                }
            }
            return null;
        }

        @Override
        public Response prepareSuccessfulResponse(WorklogJsonBean result)
        {
            return Response.noContent().cacheControl(never()).build();
        }
    },
    EDIT
    {
        @Override
        public Worklog validateAndPerformAndLeaveEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogResult worklogResult = getWorklogService().validateUpdate(serviceContext, worklogInputParameters);
            return getWorklogService().updateAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public Worklog validateAndPerformAndAutoAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogResult worklogResult = getWorklogService().validateUpdate(serviceContext, worklogInputParameters);
            return getWorklogService().updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public Worklog validateAndPerformAndManualAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            // should have been handled when preping input data.
            throw new UnsupportedOperationException(serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.manual.notapplicableforedit"));
        }

        @Override
        public Worklog validateAndPerformAndSetNewEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters)
        {
            WorklogNewEstimateResult worklogResult = getWorklogService().validateUpdateWithNewEstimate(serviceContext, (WorklogNewEstimateInputParameters) worklogInputParameters);
            return getWorklogService().updateWithNewRemainingEstimate(serviceContext, worklogResult, true);
        }

        @Override
        public WorklogInputParameters prepareData(JiraServiceContextImpl serviceContext, Issue issue, WorklogJsonBean request, WorklogResource.WorklogAdjustmentRequest adjustment)
        {
            ErrorCollection errors = serviceContext.getErrorCollection();
            Worklog existingWorklog = getAndValidateExistingWorklog(request, errors, serviceContext);
            if (!errors.hasAnyErrors())
            {
                WorklogInputParametersImpl.Builder builder = WorklogInputParametersImpl
                        .issue(issue);

                addAdjustmentParams(adjustment, builder, errors, EDIT, serviceContext);
                if (!errors.hasAnyErrors())
                {
                    String timeSpent = request.getTimeSpent();
                    Long timeSpentSeconds = request.getTimeSpentSeconds();
                    if (timeSpent != null && timeSpentSeconds != null)
                    {
                        errors.addError("timeSpent", serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.manual.increaseBy.missing"));
                    }
                    if (timeSpent != null)
                    {
                        builder.timeSpent(timeSpent);
                    }
                    else if (timeSpentSeconds != null)
                    {
                        timeSpent = getTimeLoggedString(timeSpentSeconds);
                        builder.timeSpent(timeSpent);
                    }
                    Date start = request.getStarted();

                    builder.worklogId(existingWorklog.getId());
                    builder.timeSpent(timeSpent != null ? timeSpent : getTimeLoggedString(existingWorklog.getTimeSpent()));
                    builder.startDate(start != null ? start : existingWorklog.getStartDate());
                    builder.comment(request.getComment() != null ? request.getComment() : existingWorklog.getComment());
                    if (request.getVisibility() != null)
                    {
                        builder.visibility(Visibilities.fromVisibilityBean(request.getVisibility(), getProjectRoleManager()));
                    }
                    else if (request.isVisibilitySet())
                    {
                        builder.visibility(Visibilities.publicVisibility());
                    }
                    else
                    {
                        builder.visibility(Visibilities.fromGroupAndRoleId(existingWorklog.getGroupLevel(), existingWorklog.getRoleLevelId()));
                    }
                    return builder.buildAll();
                }
            }
            return null;
        }

        @Override
        public Response prepareSuccessfulResponse(WorklogJsonBean result)
        {
            return Response.ok(result).location(result.getSelf()).cacheControl(never()).build();
        }
    };


    public abstract WorklogInputParameters prepareData(JiraServiceContextImpl serviceContext, Issue issue, WorklogJsonBean request, WorklogResource.WorklogAdjustmentRequest adjustment);
    public abstract Worklog validateAndPerformAndSetNewEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters);
    public abstract Worklog validateAndPerformAndLeaveEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters);
    public abstract Worklog validateAndPerformAndAutoAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters);
    public abstract Worklog validateAndPerformAndManualAdjustEstimate(JiraServiceContext serviceContext, Issue issue, WorklogInputParameters worklogInputParameters);
    public abstract Response prepareSuccessfulResponse(WorklogJsonBean result);

    private static void addAdjustmentParams(WorklogResource.WorklogAdjustmentRequest adjustment, WorklogInputParametersImpl.Builder builder, ErrorCollection errors, WorklogOperation operation, JiraServiceContext serviceContext)
    {
        if (adjustment.getModeString() != null && adjustment.getMode() == null)
        {
            errors.addError(WorklogRestFieldOperationsHandler.ADJUST_REMAINING_ESTIMATE_INPUT, serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.invalid", Arrays.toString(WorklogSystemField.WorklogValue.AdjustEstimate.values())));
            return;
        }

        WorklogSystemField.WorklogValue.AdjustEstimate mode = adjustment.getMode() != null ? adjustment.getMode() : WorklogSystemField.WorklogValue.AdjustEstimate.AUTO;
        switch (mode)
        {
            case NEW:
                String newEstimate = adjustment.getNewEstimate();
                if (newEstimate == null)
                {
                    errors.addError("newEstimate", serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.new.newEstimate.missing"));
                }
                else
                {
                    builder.newEstimate(newEstimate);
                }
                break;
            case MANUAL:
                if (operation == EDIT)
                {
                    errors.addError(WorklogRestFieldOperationsHandler.ADJUST_REMAINING_ESTIMATE_INPUT, serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.manual.notapplicableforedit"));
                    return;
                }
                else if (operation == DELETE)
                {
                    String increaseBy = adjustment.getIncreaseBy();
                    if (increaseBy == null)
                    {
                        errors.addError("increaseBy", serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.manual.increaseBy.missing"));
                    }
                    else
                    {
                        builder.adjustmentAmount(increaseBy);
                    }
                }
                else
                {
                    String reduceBy = adjustment.getReduceBy();
                    if (reduceBy == null)
                    {
                        errors.addError("reduceBy", serviceContext.getI18nBean().getText("rest.worklog.error.adjustEstimate.manual.reduceBy.missing"));
                    }
                    else
                    {
                        builder.adjustmentAmount(reduceBy);
                    }

                }
                break;
        }
    }

    private static Worklog getAndValidateExistingWorklog(WorklogJsonBean request, ErrorCollection errors, JiraServiceContext serviceContext)
    {
        String id = request.getId();
        if (!errors.hasAnyErrors())
        {
            if (id == null)
            {
                errors.addError(IssueFieldConstants.WORKLOG, serviceContext.getI18nBean().getText("rest.worklog.error.id.missing"));
                return null;
            }

            Worklog existingWorklog = getWorklogService().getById(serviceContext, Long.parseLong(id));
            if (existingWorklog == null)
            {
                throw new NotFoundWebException(com.atlassian.jira.rest.api.util.ErrorCollection.of(serviceContext.getI18nBean().getText("worklog.service.error.no.worklog.for.id", id)));
            }

            return existingWorklog;
        }
        else
        {
            return null;
        }
    }

    private static String getTimeLoggedString(long timeSpent)
    {
        final BigDecimal hoursPerDay = getTimeTrackingConfiguration().getHoursPerDay();
        final BigDecimal daysPerWeek = getTimeTrackingConfiguration().getDaysPerWeek();
        final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
        final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
        final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
        return DateUtils.getDurationStringSeconds(timeSpent, secondsPerDay, secondsPerWeek);
    }

    private static TimeTrackingConfiguration getTimeTrackingConfiguration()
    {
        return ComponentAccessor.getComponent(TimeTrackingConfiguration.class);
    }

    private static WorklogService getWorklogService()
    {
        return ComponentAccessor.getComponent(WorklogService.class);
    }

    private static ProjectRoleManager getProjectRoleManager()
    {
        return ComponentAccessor.getComponent(ProjectRoleManager.class);
    }
}
