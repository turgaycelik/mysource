package com.atlassian.jira.rest.v2.issue.worklog;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.WorklogSystemField;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.WorklogWithPaginationBean;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Provides logic for worklog endpoints (currently defined as a subResource of issue resource.)
 * @since v4.2
 */
public class WorklogResource
{
    private WorklogService worklogService;
    private UserManager userManager;
    private JiraAuthenticationContext authenticationContext;
    private I18nHelper i18n;
    private TimeTrackingConfiguration timeTrackingConfiguration;
    private JiraBaseUrls jiraBaseUrls;
    private EmailFormatter emailFormatter;

    public WorklogResource(final WorklogService worklogService, final JiraAuthenticationContext authenticationContext, final UserManager userManager, I18nHelper i18n, TimeTrackingConfiguration timeTrackingConfiguration, JiraBaseUrls jiraBaseUrls, EmailFormatter emailFormatter)
    {
        this.worklogService = worklogService;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.i18n = i18n;
        this.timeTrackingConfiguration = timeTrackingConfiguration;
        this.jiraBaseUrls = jiraBaseUrls;
        this.emailFormatter = emailFormatter;
    }

    public Response getIssueWorklogs(Issue issue)
    {
        return Response.ok(convertToBean(getIssueWorklogsObjects(issue))).cacheControl(never()).build();
    }

    public List<Worklog> getIssueWorklogsObjects(Issue issue)
    {
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authenticationContext.getUser());
        return worklogService.getByIssueVisibleToUser(serviceContext, issue);
    }

    public Response getWorklog(String worklogId)
    {
        try
        {
            final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authenticationContext.getUser());
            final Worklog worklog = worklogService.getById(serviceContext, Long.parseLong(worklogId));
            if (worklog != null)
            {
                return Response.ok(WorklogJsonBean.getWorklog(worklog, jiraBaseUrls, userManager, timeTrackingConfiguration, authenticationContext.getUser(), emailFormatter)).cacheControl(never()).build();
            }
            else
            {
                final ErrorCollection errors = ErrorCollection.of(serviceContext.getErrorCollection());
                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("worklog.service.error.no.worklog.for.id", worklogId)));
        }
    }

    public Response getWorklogForIssue(String worklogId, Issue issue)
    {
        try
        {
            final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authenticationContext.getUser());
            final Worklog worklog = worklogService.getById(serviceContext, Long.parseLong(worklogId));
            if (worklog != null)
            {
                if (issue.getId().equals(worklog.getIssue().getId()))
                {
                    return Response.ok(WorklogJsonBean.getWorklog(worklog, jiraBaseUrls, userManager, timeTrackingConfiguration, authenticationContext.getUser(), emailFormatter)).cacheControl(never()).build();
                }
                else
                {
                    final ErrorCollection errors = ErrorCollection.of(serviceContext.getErrorCollection());
                    return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
                }
            }
            else
            {
                final ErrorCollection errors = ErrorCollection.of(serviceContext.getErrorCollection());
                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("worklog.service.error.no.worklog.for.id", worklogId)));
        }
    }

    public Response addWorklog(Issue issue, WorklogJsonBean request, WorklogAdjustmentRequest adjustEstimateDetails, UriInfo uriInfo)
    {
        return doOperation(issue, request, adjustEstimateDetails, WorklogOperation.ADD, uriInfo);
    }

    public Response updateWorklog(Issue issue, WorklogJsonBean request, WorklogAdjustmentRequest adjustEstimateDetails, UriInfo uriInfo)
    {
        return doOperation(issue, request, adjustEstimateDetails, WorklogOperation.EDIT, uriInfo);
    }

    public Response deleteWorklog(Issue issue, WorklogJsonBean request, WorklogAdjustmentRequest adjustEstimateDetails, UriInfo uriInfo)
    {
        return doOperation(issue, request, adjustEstimateDetails, WorklogOperation.DELETE, uriInfo);
    }

    private Response doOperation(Issue issue, WorklogJsonBean request, WorklogAdjustmentRequest adjustEstimateDetails, WorklogOperation operation, UriInfo uriInfo)
    {
        final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(authenticationContext.getUser());
        if (request == null)
        {
            // return with error;
            return Response.serverError().build();
        }

        WorklogInputParameters worklogInputParameters = operation.prepareData(jiraServiceContext, issue, request, adjustEstimateDetails);
        if (!jiraServiceContext.getErrorCollection().hasAnyErrors())
        {
            WorklogSystemField.WorklogValue.AdjustEstimate mode = adjustEstimateDetails.getMode() != null ? adjustEstimateDetails.getMode() : WorklogSystemField.WorklogValue.AdjustEstimate.AUTO;
            Worklog worklog= null;
            switch (mode)
            {
                case NEW:
                    worklog = operation.validateAndPerformAndSetNewEstimate(jiraServiceContext, issue, worklogInputParameters);
                    break;
                case MANUAL:
                    worklog = operation.validateAndPerformAndManualAdjustEstimate(jiraServiceContext, issue, worklogInputParameters);
                    break;
                case LEAVE:
                    worklog = operation.validateAndPerformAndLeaveEstimate(jiraServiceContext, issue, worklogInputParameters);
                    break;
                case AUTO:
                    worklog = operation.validateAndPerformAndAutoAdjustEstimate(jiraServiceContext, issue, worklogInputParameters);
                    break;
            }

            if (!jiraServiceContext.getErrorCollection().hasAnyErrors())
            {
                if (worklog == null)
                {
                    jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("rest.worklog.error.unknown"));
                }
                else
                {
                    return operation.prepareSuccessfulResponse(convertToBean(worklog));
                }
            }
        }
        throw new RESTException(ErrorCollection.of(jiraServiceContext.getErrorCollection()));
    }

    private WorklogJsonBean convertToBean(final Worklog worklog)
    {
        return WorklogJsonBean.getWorklog(worklog, jiraBaseUrls, userManager, timeTrackingConfiguration, authenticationContext.getUser(), emailFormatter);
    }

    private WorklogWithPaginationBean convertToBean(final List<Worklog> worklogs)
    {
        WorklogWithPaginationBean worklogWithPaginationBean = new WorklogWithPaginationBean();
        worklogWithPaginationBean.setMaxResults(worklogs.size());
        worklogWithPaginationBean.setTotal(worklogs.size());
        worklogWithPaginationBean.setStartAt(0);

        final List<WorklogJsonBean> beans = new ArrayList<WorklogJsonBean>(worklogs.size());
        for (final Worklog worklog : worklogs)
        {
            beans.add(convertToBean(worklog));
        }

        worklogWithPaginationBean.setWorklogs(beans);
        return worklogWithPaginationBean;
    }

    public static class WorklogAdjustmentRequest
    {
        private final String mode;
        private final String newEstimate;
        private final String reduceBy;
        private final String increaseBy;

        public WorklogAdjustmentRequest(String mode, String newEstimate, String reduceBy, String increaseBy)
        {
            this.mode = mode;
            this.newEstimate = newEstimate;
            this.reduceBy = reduceBy;
            this.increaseBy = increaseBy;
        }

        public WorklogSystemField.WorklogValue.AdjustEstimate getMode()
        {
            if (StringUtils.isNotBlank(mode))
            {
                return WorklogSystemField.WorklogValue.AdjustEstimate.valueOf(mode.toUpperCase());
            }
            return null;
        }

        public String getModeString()
        {
            return mode;
        }

        public String getNewEstimate()
        {
            return newEstimate;
        }

        public String getReduceBy()
        {
            return reduceBy;
        }

        public String getIncreaseBy()
        {
            return increaseBy;
        }
    }
}
