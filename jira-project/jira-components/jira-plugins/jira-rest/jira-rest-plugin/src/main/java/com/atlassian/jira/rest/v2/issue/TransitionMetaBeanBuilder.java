package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * Builder for {@link CreateMetaBean} instances.
 *
 * @since v5.0
 */
public class TransitionMetaBeanBuilder
{
    private static final int AUTOMATIC_LOOP_BACK_STEP_ID = -1;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final JiraAuthenticationContext authContext;
    private final FieldLayoutManager fieldLayoutManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ContextUriInfo contextUriInfo;
    private final VersionBeanFactory versionBeanFactory;
    private final JiraBaseUrls baseUrls;
    private final WorkflowManager workflowManager;
    private final StatusManager statusManager;

    private ActionDescriptor action;
    private Issue issue;

    public TransitionMetaBeanBuilder(final FieldScreenRendererFactory fieldScreenRendererFactory, final JiraAuthenticationContext authContext,
            final FieldLayoutManager fieldLayoutManager, final VelocityRequestContextFactory velocityRequestContextFactory,
            final ContextUriInfo contextUriInfo, final VersionBeanFactory versionBeanFactory, JiraBaseUrls baseUrls, WorkflowManager workflowManager, StatusManager statusManager)
    {
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.authContext = authContext;
        this.fieldLayoutManager = fieldLayoutManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.contextUriInfo = contextUriInfo;
        this.versionBeanFactory = versionBeanFactory;
        this.baseUrls = baseUrls;
        this.workflowManager = workflowManager;
        this.statusManager = statusManager;
    }

    public TransitionMetaBeanBuilder issue(final Issue issue)
    {
        this.issue = issue;
        return this;
    }

    public TransitionMetaBeanBuilder action(final ActionDescriptor action)
    {
        this.action = action;
        return this;
    }

    public TransitionBean build()
    {
        final Status destinationStatus = getStatusFromStep(issue, action.getUnconditionalResult().getStep());
        TransitionMetaFieldBeanBuilder fieldBuilder = new TransitionMetaFieldBeanBuilder(fieldScreenRendererFactory, fieldLayoutManager, action, issue, authContext.getLoggedInUser(), versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls);

        return new TransitionBean(String.valueOf(action.getId()),  WorkflowUtil.getWorkflowTransitionDisplayName(action), fieldBuilder, StatusJsonBean.bean(destinationStatus, baseUrls));
    }

     /**
     * @param issue issue object to derive the workflow from
     * @param stepId the step id to get the status id for
     * @return the the status which stepId maps to in the associated workflow
     */
    private Status getStatusFromStep(Issue issue, int stepId)
    {
        final WorkflowDescriptor workflowDescriptor = workflowManager.getWorkflow(issue).getDescriptor();

        String statusId;

        if (AUTOMATIC_LOOP_BACK_STEP_ID == stepId) // Then we just need to return the current status, see JRA-32132 for details.
        {
            statusId = issue.getStatusObject().getId();
        }
        else
        {
            statusId = (String) workflowDescriptor.getStep(stepId).getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY);
        }

        return statusManager.getStatus(statusId);
    }

}
