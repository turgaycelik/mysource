package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Condition to test whether an issue has subtasks
 *
 * @since v4.4
 */
public class HasSubTaskCondition implements Condition
{
    private final SubTaskManager subTaskManager;
    private final JiraAuthenticationContext authenticationContext;

    public HasSubTaskCondition(SubTaskManager subTaskManager, JiraAuthenticationContext authenticationContext)
    {
        this.subTaskManager = subTaskManager;
        this.authenticationContext = authenticationContext;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");

        final SubTaskBean subTaskBean = getSubTaskBean(issue, context);
        final boolean hasSubTasks = !subTaskBean.getSubTasks(SubTaskBean.SUB_TASK_VIEW_ALL).isEmpty();

        return hasSubTasks;
    }

    private SubTaskBean getSubTaskBean(Issue issue, Map<String, Object> context)
    {
        final HttpServletRequest request = getRequest(context);
        if (request != null)
        {
            SubTaskBean subtaskBean = (SubTaskBean) request.getAttribute("atl.jira.subtask.bean." + issue.getKey());
            if (subtaskBean != null)
            {
                return subtaskBean;
            }
            subtaskBean = subTaskManager.getSubTaskBean(issue.getGenericValue(), authenticationContext.getLoggedInUser());
            request.setAttribute("atl.jira.subtask.bean." + issue.getKey(), subtaskBean);
            return subtaskBean;
        }

        return subTaskManager.getSubTaskBean(issue.getGenericValue(), authenticationContext.getLoggedInUser());
    }

    /**
     * Method that retrieves the HttpServletRequest by the numerous methods that JIRA uses. It tries to get it from: the
     * context passed in (jirahelper or request), then from ExecutingHttpRequest, and then from the ActionContext
     *
     * @param context the context passed into the getContextMap method.
     * @return the current request.
     */
    protected HttpServletRequest getRequest(Map<String, Object> context)
    {
        HttpServletRequest request = null;
        JiraHelper jiraHelper = (JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER);
        if (jiraHelper != null)
        {
            request = jiraHelper.getRequest();
            if (request != null)
            {
                return request;
            }
        }
        final Object o = context.get("request");
        if (o != null && o instanceof HttpServletRequest)
        {
            request = (HttpServletRequest) o;
            return request;
        }

        request = ExecutingHttpRequest.get();
        if (request != null)
        {
            return request;
        }

        return ActionContext.getRequest();

    }

}
