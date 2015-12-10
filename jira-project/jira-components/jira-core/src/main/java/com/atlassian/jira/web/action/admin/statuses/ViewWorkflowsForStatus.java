package com.atlassian.jira.web.action.admin.statuses;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.config.StatusService;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * ViewWorkflowsForStatus views a list of workflows for a given status.
 *
 * Used in a dialog off ViewStatuses
 */
@WebSudoRequired
public class ViewWorkflowsForStatus extends JiraWebActionSupport
{
    private String id;

    private final StatusService statusService;
    private final WorkflowManager workflowManager;

    public ViewWorkflowsForStatus(final StatusService statusService, final WorkflowManager workflowManager)
    {
        this.statusService = statusService;
        this.workflowManager = workflowManager;
    }

    @ActionViewData
    public Status getStatus()
    {
        return statusService.getStatusById(getLoggedInApplicationUser(), id);
    }

    @ActionViewData
    public List<String> getWorkflowsForStatus()
    {
        List<JiraWorkflow> allWorkflows = workflowManager.getWorkflowsIncludingDrafts();
        final Status status = statusService.getStatusById(getLoggedInApplicationUser(), id);

        Iterable<JiraWorkflow> workflowsForStatus = Iterables.filter(allWorkflows, new Predicate<JiraWorkflow>()
        {
            @Override
            public boolean apply(@Nullable final JiraWorkflow input)
            {
                return input.getLinkedStatusIds().contains(status.getId());
            }
        });

        List<String> workflowNamesForStatus = Lists.newArrayList(Iterables.transform(workflowsForStatus, GET_WORKFLOW_NAME));

        Collections.sort(workflowNamesForStatus);

        return workflowNamesForStatus;
    }

    private static final Function<JiraWorkflow, String> GET_WORKFLOW_NAME = new Function<JiraWorkflow, String>()
    {
        @Override
        public String apply(@Nullable final JiraWorkflow input)
        {
            return input.getName();
        }
    };

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }
}
