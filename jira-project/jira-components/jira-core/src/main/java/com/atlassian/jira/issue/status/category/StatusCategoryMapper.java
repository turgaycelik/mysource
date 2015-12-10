package com.atlassian.jira.issue.status.category;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * The purpose of this helper class is to make a best guess at semantics for statuses that may not have any.
 * The data is intended for use when workflows without the full status metadata are created, such as
 * through the workflow importer, or when upgrading from older versions of JIRA.
 *
 * The class is not intended to be used for rendering statuses directly.
 *
 * @since v6.2
 */
public class StatusCategoryMapper
{
    private static final Logger log = Logger.getLogger(StatusCategoryMapper.class);
    private StatusCategoryManager statusCategoryManager;

    public StatusCategoryMapper(final StatusCategoryManager statusCategoryManager)
    {
        this.statusCategoryManager = statusCategoryManager;
    }

    /**
     * Scan a JIRA workflow, determine what status category each status should belong to.
     *
     * Will make a best effort at returning a mapping, even for misconfigured workflows.
     * In cases where workflows are misconfigured, errors will be logged, not thrown.
     *
     * @param workflow The {@link JiraWorkflow} to check the statuses for.
     * @return A map of our best guess as to the semantics of each {@link Status} as used in this workflow.
     */
    public Map<String, StatusCategory> mapCategoriesToStatuses(JiraWorkflow workflow)
    {
        Assertions.notNull("workflow", workflow);
        Map<String, StatusCategory> map = Maps.newHashMap();

        try
        {
            List<Status> statuses = workflow.getLinkedStatusObjects();
            for (Status status : statuses)
            {
                if (null != status)
                {
                    map.put(status.getId(), getDefaultCategory());
                }
            }
        }
        catch (RuntimeException e)
        {
            log.error(String.format("The '%s' workflow is misconfigured", workflow.getName()), e);
        }

        // Find and set initial state
        try
        {
            StepDescriptor step = getInitialStepDescriptor(workflow);
            if (null != step)
            {
                Status status = workflow.getLinkedStatusObject(step);
                if (null != status)
                {
                    map.put(status.getId(), getStartingCategory());
                }
            }
        }
        catch (RuntimeException e)
        {
            log.error(String.format("Failed to determine the initial step for '%s' workflow ", workflow.getName()), e);
        }

        // Find and set any completed states
        try
        {
            for (ActionDescriptor actionDescriptor : workflow.getAllActions())
            {
                @SuppressWarnings("unchecked")
                final List<FunctionDescriptor> postFunctions = actionDescriptor.getUnconditionalResult().getPostFunctions();
                for (FunctionDescriptor descriptor : postFunctions)
                {
                    final String className = (String)descriptor.getArgs().get("class.name");
                    final String fieldName = (String)descriptor.getArgs().get("field.name");
                    final String fieldValue = (String)descriptor.getArgs().get("field.value");
                    if (StringUtils.equals(className, UpdateIssueFieldFunction.class.getName()))
                    {
                        if (StringUtils.equals(fieldName, IssueFieldConstants.RESOLUTION)
                            && StringUtils.isNotBlank(fieldValue))
                        {
                            final StepDescriptor stepDescriptor = workflow.getDescriptor().getStep(actionDescriptor.getUnconditionalResult().getStep());
                            final Status destinationStatus = workflow.getLinkedStatusObject(stepDescriptor);
                            map.put(destinationStatus.getId(), getCompleteCategory());
                        }
                    }
                }
            }
        }
        catch (RuntimeException e)
        {
            log.error(String.format("Failed to find completion steps for '%s' workflow", workflow.getName()), e);
        }

        return map;
    }

    private static StepDescriptor getInitialStepDescriptor(JiraWorkflow workflow)
    {
        List initialActions = workflow.getDescriptor().getInitialActions();
        ActionDescriptor initialAction = (ActionDescriptor) initialActions.get(0);
        int initialStep = initialAction.getUnconditionalResult().getStep();
        return workflow.getDescriptor().getStep(initialStep);
    }

    private StatusCategory getDefaultCategory()
    {
        return statusCategoryManager.getStatusCategoryByKey(StatusCategory.IN_PROGRESS);
    }

    private StatusCategory getStartingCategory()
    {
        return statusCategoryManager.getStatusCategoryByKey(StatusCategory.TO_DO);
    }

    private StatusCategory getCompleteCategory()
    {
        return statusCategoryManager.getStatusCategoryByKey(StatusCategory.COMPLETE);
    }

}
