package com.atlassian.jira.issue.transitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.commons.lang.StringUtils;

import webwork.action.ActionContext;

/**
 * A Simple LinkFactory for generating Issue Transitions.
 *
 * @since v4.1
 */
public class TransitionLinkFactory implements WebItemProvider
{
    private final VelocityRequestContextFactory requestContextFactory;
    private final IssueWorkflowManager issueWorkflowManager;
    private final JiraAuthenticationContext authenticationContext;

    public TransitionLinkFactory(final VelocityRequestContextFactory requestContextFactory,
            final IssueWorkflowManager issueWorkflowManager, final JiraAuthenticationContext authenticationContext)
    {
        this.requestContextFactory = requestContextFactory;
        this.issueWorkflowManager = issueWorkflowManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        return getAvailableActions(issue);
    }

    /**
     * Get available Workflow transitions for the current issue/current user
     *
     * @return a list containing all available actions for an issue.
     */
    private List<WebItem> getAvailableActions(final Issue issue)
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final List<ActionDescriptor> actions = issueWorkflowManager.getSortedAvailableActions(issue, authenticationContext.getUser());

        final List<WebItem> returnList = new ArrayList<WebItem>(actions.size());

        int weight = 0;
        for (final ActionDescriptor action : actions)
        {
            final String url = requestContext.getBaseUrl() + "/secure/WorkflowUIDispatcher.jspa?"
                    + "id=" + issue.getId() + ""
                    + "&action=" + action.getId()
                    + "&atl_token=" + getXsrfToken();

            final String transitionDisplayName = getWorkflowTransitionDisplayName(action);
            final String description = getWorkflowTransitionDescription(action);
            returnList.add(new WebFragmentBuilder(weight += 10).
                    styleClass("issueaction-workflow-transition").
                    id("action_id_" + action.getId()).
                    label(transitionDisplayName).
                    title((StringUtils.isBlank(description) ? null : transitionDisplayName + " - " + description)).
                    webItem("transitions-all").
                    url(url).build());
        }
        return returnList;
    }


    String getWorkflowTransitionDisplayName(final ActionDescriptor descriptor)
    {
        return WorkflowUtil.getWorkflowTransitionDisplayName(descriptor);
    }

    String getWorkflowTransitionDescription(final ActionDescriptor descriptor)
    {
        return StringUtils.trimToNull(WorkflowUtil.getWorkflowTransitionDescription(descriptor));
    }

    String getXsrfToken()
    {
        final HttpServletRequest request = ActionContext.getRequest();
        if (request != null)
        {
            return getXsrfTokenGenerator().generateToken(request);
        }
        return "";
    }

    XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
    }

}
