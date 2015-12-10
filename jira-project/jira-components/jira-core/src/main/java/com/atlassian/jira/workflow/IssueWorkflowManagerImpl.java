package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.DocumentIssueImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static org.apache.commons.lang.ArrayUtils.EMPTY_INT_ARRAY;

public class IssueWorkflowManagerImpl implements IssueWorkflowManager
{
    private static final Logger log = Logger.getLogger(IssueWorkflowManagerImpl.class);

    private final IssueManager issueManager;
    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    public IssueWorkflowManagerImpl(final IssueManager issueManager, final WorkflowManager workflowManager,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager)
    {
        this.issueManager = issueManager;
        this.workflowManager = workflowManager;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
    }

    @Deprecated
    @Override
    public Collection<ActionDescriptor> getAvailableActions(final Issue issue)
    {
        return getAvailableActions(issue, TransitionOptions.defaults(), authenticationContext.getUser());
    }

    @Override
    public Collection<ActionDescriptor> getAvailableActions(final Issue issue, final ApplicationUser user)
    {
        return getAvailableActions(issue, TransitionOptions.defaults(), user);
    }

    @Deprecated
    @Override
    public Collection<ActionDescriptor> getAvailableActions(final Issue issue, final TransitionOptions transitionOptions)
    {
        return getAvailableActions(issue, transitionOptions, authenticationContext.getUser());
    }

    @Override
    public Collection<ActionDescriptor> getAvailableActions(final Issue issue, final TransitionOptions transitionOptions, final ApplicationUser user)
    {
        final int[] actionIds = getAvailableActionIds(issue, transitionOptions, user);
        final Collection<ActionDescriptor> availableActions = new ArrayList<ActionDescriptor>(actionIds.length);
        final WorkflowDescriptor workflowDescriptor = workflowManager.getWorkflow(issue).getDescriptor();

        for (final int actionId : actionIds)
        {
            final ActionDescriptor action = workflowDescriptor.getAction(actionId);
            if (action == null)
            {
                log.error("State of issue [" + issue + "] has an action [id=" + actionId
                        + "] which cannot be found in the workflow descriptor");
            }
            else
            {
                availableActions.add(action);
            }
        }

        return availableActions;
    }

    @Deprecated
    @Override
    public List<ActionDescriptor> getSortedAvailableActions(final Issue issue)
    {
        return getSortedAvailableActions(issue, TransitionOptions.defaults(), authenticationContext.getUser());
    }

    @Deprecated
    @Override
    public List<ActionDescriptor> getSortedAvailableActions(final Issue issue, final TransitionOptions transitionOptions)
    {
        return getSortedAvailableActions(issue, transitionOptions, authenticationContext.getUser());
    }

    @Override
    public List<ActionDescriptor> getSortedAvailableActions(final Issue issue, final ApplicationUser user)
    {
        return getSortedAvailableActions(issue, TransitionOptions.defaults(), user);
    }

    @Override
    public List<ActionDescriptor> getSortedAvailableActions(final Issue issue, final TransitionOptions transitionOptions, final ApplicationUser user)
    {
        final List<ActionDescriptor> availableActions = Lists.newArrayList(getAvailableActions(issue, transitionOptions, user));
        Collections.sort(availableActions, new Comparator<ActionDescriptor>()
        {
            @Override
            public int compare(final ActionDescriptor actionDescriptor, final ActionDescriptor actionDescriptor1)
            {
                return getSequenceFromAction(actionDescriptor).compareTo(getSequenceFromAction(actionDescriptor1));
            }
        });
        return availableActions;
    }

    private Integer getSequenceFromAction(final ActionDescriptor action)
    {
        if (action == null)
        {
            return Integer.MAX_VALUE;
        }

        final Map metaAttributes = action.getMetaAttributes();
        if (metaAttributes == null)
        {
            return Integer.MAX_VALUE;
        }

        final String value = (String) metaAttributes.get("opsbar-sequence");

        if (value == null || StringUtils.isBlank(value) || !StringUtils.isNumeric(value))
        {
            return Integer.MAX_VALUE;
        }

        return Integer.valueOf(value);
    }

    @Deprecated
    @Override
    public boolean isValidAction(final Issue issue, final int actionid, final TransitionOptions transitionOptions)
    {
        return isValidAction(issue, actionid, transitionOptions, authenticationContext.getUser());
    }

    @Deprecated
    @Override
    public boolean isValidAction(final Issue issue, final int actionid)
    {
        return isValidAction(issue, actionid, TransitionOptions.defaults(), authenticationContext.getUser());
    }

    @Override
    public boolean isValidAction(final Issue issue, final int actionId, final ApplicationUser user)
    {
        return isValidAction(issue, actionId, TransitionOptions.defaults(), user);
    }

    @Override
    public boolean isValidAction(final Issue issue, final int actionId, final TransitionOptions transitionOptions, final ApplicationUser user)
    {
        for (final int id : getAvailableActionIds(issue, transitionOptions, user))
        {
            if (id == actionId)
            {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    int[] getAvailableActionIds(final Issue issue, final TransitionOptions transitionOptions, final ApplicationUser user)
    {
        if (!transitionOptions.skipPermissions() && !permissionManager.hasPermission(ProjectPermissions.TRANSITION_ISSUES, issue, user))
        {
            return EMPTY_INT_ARRAY;
        }

        final Issue issueObject;
        final Issue originalIssue;

        if (issue instanceof DocumentIssueImpl)
        {
            issueObject = issueManager.getIssueObject(issue.getId());
            originalIssue = issueObject;
        }
        else
        {
            if (issue.getWorkflowId() == null)
            {
                log.error("!!! Issue " + issue.getKey() + " has no workflow ID !!! ");
                return EMPTY_INT_ARRAY;
            }
            issueObject = issue;
            originalIssue = issueManager.getIssueObject(issue.getId());
        }

        final Workflow workflow = workflowManager.makeWorkflow(user);
        final HashMap<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("pkey", issueObject.getProjectObject().getKey()); // Allows ${project.key} in condition args
        inputs.put("issue", issueObject);
        // The condition should examine the original issue object - put this in the transientvars
        // This is done here as AbstractWorkflow later changes this collection to be an unmodifiable map
        inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
        inputs.putAll(transitionOptions.getWorkflowParams());
        return workflow.getAvailableActions(issueObject.getWorkflowId(), inputs);
    }
}
