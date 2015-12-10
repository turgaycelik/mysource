package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowProgressAware;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class BulkWorkflowTransitionOperation implements ProgressAwareBulkOperation
{
    protected static final Logger log = Logger.getLogger(BulkWorkflowTransitionOperation.class);

    public static final String NAME = "BulkWorkflowTransition";
    public static final String NAME_KEY = "bulk.workflowtransition.operation.name";
    private static final String DESCRIPTION_KEY = "bulk.workflowtransition.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.workflowtransition.cannotperform";

    private final WorkflowManager workflowManager;
    private final IssueWorkflowManager issueWorkflowManager;
    private final FieldLayoutManager fieldLayoutManager;

    public BulkWorkflowTransitionOperation(WorkflowManager workflowManager, IssueWorkflowManager issueWorkflowManager,
                                           FieldLayoutManager fieldLayoutManager)
    {
        this.workflowManager = workflowManager;
        this.issueWorkflowManager = issueWorkflowManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser)
    {
        return canPerformOnAnyIssue(bulkEditBean, remoteUser);
    }

    public boolean canPerformOnAnyIssue(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser)
    {
        final Collection<Issue> selectedIssues = bulkEditBean.getSelectedIssues();

        for (final Issue issue : selectedIssues)
        {
            final Collection<ActionDescriptor> availableActions =
                    issueWorkflowManager.getAvailableActions(issue, remoteUser);
            if (!availableActions.isEmpty())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser,
                        final Context taskContext) throws BulkOperationException
    {
        final ActionDescriptor actionDescriptor = getActionDescriptor(bulkEditBean.getSelectedWFTransitionKey());

        for (final Issue issue1 : bulkEditBean.getSelectedIssues())
        {
            Context.Task task = taskContext.start(issue1);
            final MutableIssue issue = (MutableIssue) issue1;

            final Collection<ActionDescriptor> availableActions =
                    issueWorkflowManager.getAvailableActions(issue, applicationUser);
            if (!availableActions.contains(actionDescriptor))
            {
                // We cannot perform selected action on this issue. This can happen if:
                // - someone vetoed TRANSITION_PERMISSION
                // - someone revoked TRANSITION_PERMISSION (or any other required here)
                // - someone already changed state of this issue
                // (after user arrived to last screen of bulk transition - so something changed after user selected list of issues to change)
                task.complete();
                continue;
            }

            final Map<String, Object> additionalInputs = Maps.newHashMap();

            // Update the issue fields as required for each issue
            final Map selectedActions = bulkEditBean.getActions();
            if (selectedActions != null)
            {
                for (final BulkEditAction bulkEditAction : bulkEditBean.getActions().values())
                {
                    final OrderableField field = bulkEditAction.getField();
                    final FieldLayoutItem fieldLayoutItem = fieldLayoutManager
                            .getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId())
                            .getFieldLayoutItem(field);
                    if (fieldLayoutItem != null)
                    {
                        // JRA-14178: need to specifically set comment params here so that they get picked up
                        // by the CreateCommentFunction during the workflow processing.
                        // Also, don't update the issue with the comment field, as this will cause two comments
                        // to be created.
                        if (IssueFieldConstants.COMMENT.equals(field.getId()))
                        {
                            ((CommentSystemField) field)
                                    .populateAdditionalInputs(bulkEditBean.getFieldValuesHolder(), additionalInputs);
                        }
                        else
                        {
                            field.updateIssue(fieldLayoutItem, issue, bulkEditBean.getFieldValuesHolder());
                        }
                    }
                }
            }

            final BulkWorkflowProgressAware bulkWorkflowProgressAware =
                    new BulkWorkflowProgressAware(applicationUser, actionDescriptor.getId(), issue,
                            issue.getProjectObject());

            additionalInputs.put("sendBulkNotification", bulkEditBean.isSendBulkNotification());
            bulkWorkflowProgressAware.setAdditionalInputs(additionalInputs);

            // Transition the issue
            workflowManager.doWorkflowAction(bulkWorkflowProgressAware);

            if (bulkWorkflowProgressAware.hasError())
            {
                // JRA-9844
                bulkEditBean.addTransitionErrors(issue.getKey(), bulkWorkflowProgressAware.getErrors());
            }
            task.complete();
        }
    }

    @Override
    public int getNumberOfTasks(final BulkEditBean bulkEditBean)
    {
        return bulkEditBean.getSelectedIssues().size();
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }

    public ActionDescriptor getActionDescriptor(WorkflowTransitionKey workflowTransitionKey)
    {
        String workflowName = workflowTransitionKey.getWorkflowName();
        String actionDescriptorId = workflowTransitionKey.getActionDescriptorId();

        JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);

        return workflow.getDescriptor().getAction(Integer.parseInt(actionDescriptorId));
    }

    // Used to perform the workflow transition
    static class BulkWorkflowProgressAware implements WorkflowProgressAware
    {
        private ApplicationUser remoteUser;
        private int actionId;
        private MutableIssue issue;
        private Project project;
        private Map additionalInputs;
        private Collection<String> errors = new ArrayList<String>();

        public BulkWorkflowProgressAware(ApplicationUser remoteUser, int actionId, MutableIssue issue, Project project)
        {
            this.remoteUser = remoteUser;
            this.actionId = actionId;
            this.issue = issue;
            this.project = project;
        }


        public User getRemoteUser()
        {
            return ApplicationUsers.toDirectoryUser(remoteUser);
        }

        @Override
        public ApplicationUser getRemoteApplicationUser()
        {
            return remoteUser;
        }

        public int getAction()
        {
            return actionId;
        }

        public void setAction(int action)
        {
            actionId = action;
        }

        public void addErrorMessage(String error)
        {
            errors.add(error);
        }

        public void addError(String name, String error)
        {
            errors.add(error);
        }

        public boolean hasError()
        {
            return !errors.isEmpty();
        }

        public Map getAdditionalInputs()
        {
            return additionalInputs;
        }

        public void setAdditionalInputs(Map additionalInputs)
        {
            this.additionalInputs = additionalInputs;

        }

        public MutableIssue getIssue()
        {
            return issue;
        }

        @Override
        public Project getProject()
        {
            return project;
        }

        @Override
        public Project getProjectObject()
        {
            return project;
        }

        public Collection<String> getErrors()
        {
            return errors;
        }
    }
}
