package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTask;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Context Provider for the subtask section on view issue.  Is Cacheable.
 *
 * @since v4.4
 */
public class SubTasksContxtProvider implements CacheableContextProvider
{
    private final SubTaskManager subTaskManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    private final TableLayoutFactory tableLayoutFactory;

    public SubTasksContxtProvider(final SubTaskManager subTaskManager, final JiraAuthenticationContext authenticationContext,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory)
    {
        this.subTaskManager = subTaskManager;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
        this.tableLayoutFactory = ComponentAccessor.getComponent(TableLayoutFactory.class);
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public String getUniqueContextKey(final Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        final SubTaskBean subTaskBean = getSubTaskBean(issue);

        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final String selectedIssueId = requestContext.getRequestParameter("selectedIssueId");

        paramsBuilder.add("hasSubTasks", !subTaskBean.getSubTasks(getSubTaskView()).isEmpty());
        paramsBuilder.add("selectedIssueId", selectedIssueId);
        paramsBuilder.add("subTaskTable", new SubTaskTableRenderer(issue, user));

        return paramsBuilder.toMap();
    }

    /*
    * This is cached because this is very expensive to calculate and is calculated in few areas per request. E.g. View Issue (subtask block)
    */
    private AggregateTimeTrackingBean getAggregates(final Issue issue)
    {
        final HttpServletRequest request = getRequest();
        if (request != null)
        {
            AggregateTimeTrackingBean aggregates = (AggregateTimeTrackingBean) request.getAttribute(AggregateTimeTrackingBean.AGG_TIMETRACKING + issue.getId());
            if (aggregates == null)
            {
                final AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
                aggregates = calculator.getAggregates(issue);
                request.setAttribute(AggregateTimeTrackingBean.AGG_TIMETRACKING + issue.getId(), aggregates);
            }
            return aggregates;
        }

        final AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
        return calculator.getAggregates(issue);

    }

    private SubTaskBean getSubTaskBean(final Issue issue)
    {
        final HttpServletRequest request = getRequest();
        if (request != null)
        {
            SubTaskBean subtaskBean = (SubTaskBean) request.getAttribute("atl.jira.subtask.bean." + issue.getKey());
            if (subtaskBean != null)
            {
                return subtaskBean;
            }
            subtaskBean = subTaskManager.getSubTaskBean(issue, authenticationContext.getLoggedInUser());
            request.setAttribute("atl.jira.subtask.bean." + issue.getKey(), subtaskBean);
            return subtaskBean;
        }

        return subTaskManager.getSubTaskBean(issue, authenticationContext.getLoggedInUser());
    }

    private String getSubTaskView()
    {
        return getSessionBackedRequestParam("subTaskView", SubTaskBean.SUB_TASK_VIEW_DEFAULT, SessionKeys.SUB_TASK_VIEW);
    }

    private String getSessionBackedRequestParam(String requestParamName, String defaultValue, String sessionKey)
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();

        final String requestParameter = requestContext.getRequestParameter(requestParamName);
        if (StringUtils.isNotBlank(requestParameter))
        {
            if (requestParameter.equals(defaultValue))
            {
                session.removeAttribute(sessionKey);
                return defaultValue;
            }
            else
            {
                session.setAttribute(sessionKey, requestParameter);
                return requestParameter;
            }
        }

        final String sortOrder = (String) session.getAttribute(sessionKey);
        return StringUtils.isNotBlank(sortOrder) ? sortOrder : defaultValue;
    }

    private String getTableHtml(final Issue issue, final User user)
    {
        final AggregateTimeTrackingBean aggregateTTBean = getAggregates(issue);

        final SubTaskBean subTaskBean = getSubTaskBean(issue);
        final String subTaskView = getSubTaskView();
        final Collection<SubTask> issues = subTaskBean.getSubTasks(subTaskView);
        final List<Issue> issueObjects = newArrayList();
        boolean atLeastOneIssueHasTimeTrackingData = false;

        // get the subtask Issue object out of each SubTask and calculate if we need to display timetracking progress
        for (final SubTask subTask : issues)
        {
            final Issue subTaskIssue = subTask.getSubTask();
            atLeastOneIssueHasTimeTrackingData = atLeastOneIssueHasTimeTrackingData || IssueUtils.hasTimeTracking(subTaskIssue);
            issueObjects.add(subTaskIssue);
        }

        final IssueTableWebComponent issueTable = new IssueTableWebComponent();
        final IssueTableLayoutBean layout;
        try
        {
            layout = tableLayoutFactory.getSubTaskIssuesLayout(user, issue, subTaskBean, subTaskView, atLeastOneIssueHasTimeTrackingData);
        }
        catch (ColumnLayoutStorageException e)
        {
            throw new RuntimeException(e);
        }
        catch (FieldException e)
        {
            throw new RuntimeException(e);
        }

        layout.addCellDisplayParam("aggTTBean", aggregateTTBean);
        layout.addCellDisplayParam("issueStatusMaxWidth", "short");
        return issueTable.getHtml(layout, issueObjects, null);
    }

    public class SubTaskTableRenderer
    {
        private final Issue issue;
        private final User user;

        public SubTaskTableRenderer(final Issue issue, final User user)
        {
            this.user = user;
            this.issue = issue;
        }

        public String getHtml()
        {
            return getTableHtml(issue, user);
        }
    }

    protected HttpServletRequest getRequest()
    {
        return ExecutingHttpRequest.get();
    }
}
