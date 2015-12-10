package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Context Provider for the Sub Task Progress bar.  Is cacheable.
 *
 * @since v4.4
 */
public class SubTasksProgressContextProvider implements CacheableContextProvider
{
    private final SubTaskManager subTaskManager;
    private final JiraAuthenticationContext authenticationContext;

    public SubTasksProgressContextProvider(SubTaskManager subTaskManager, JiraAuthenticationContext authenticationContext)
    {
        this.subTaskManager = subTaskManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);
        final Issue issue = (Issue) context.get("issue");

        final SubTaskBean subTaskBean = getSubTaskBean(issue, context);
        paramsBuilder.add("subTaskProgress", subTaskBean.getSubTaskProgress());
        return paramsBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = (User) context.get("user");

        return issue.getId() + "/" + (user == null ? "" : user.getName());
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

    protected HttpServletRequest getRequest(Map<String, Object> context)
    {
        return ExecutingHttpRequest.get();

    }

}
