package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.plugin.PluginParseException;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Context Provider for the Time Tracking Web Panel.
 *
 * @since v4.4
 */
public class TimeTrackingViewIssueContextProvider implements CacheableContextProvider
{
    private static final String ISSUE = "issue";
    private static final String AGGREGATE_TIME_TRACKING_GRAPH_BEAN = "aggregateTimeTrackingGraphBean";
    private static final String HAS_DATA = "hasData";
    private static final String TIME_TRACKING_GRAPH_BEAN = "timeTrackingGraphBean";
    private static final String I18N = "i18n";


    private final JiraAuthenticationContext authenticationContext;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    private final TimeTrackingGraphBeanFactory timeTrackingGraphBeanFactory;

    public TimeTrackingViewIssueContextProvider(JiraAuthenticationContext authenticationContext, AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory,
            TimeTrackingGraphBeanFactory timeTrackingGraphBeanFactory)
    {
        this.authenticationContext = authenticationContext;
        this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
        this.timeTrackingGraphBeanFactory = timeTrackingGraphBeanFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get(ISSUE);

        final TimeTrackingGraphBean timeTrackingGraphBean = timeTrackingGraphBeanFactory.createBean(issue, TimeTrackingGraphBeanFactory.Style.SHORT, authenticationContext.getI18nHelper());

        AggregateTimeTrackingBean aggregates = getAggregates(issue, context);

        if (aggregates.getSubTaskCount() > 0)
        {
            final TimeTrackingGraphBean aggregateTimeTrackingGraphBean = timeTrackingGraphBeanFactory.createBean(aggregates, TimeTrackingGraphBeanFactory.Style.SHORT, authenticationContext.getI18nHelper());
            paramsBuilder.add(AGGREGATE_TIME_TRACKING_GRAPH_BEAN, aggregateTimeTrackingGraphBean);
            paramsBuilder.add(HAS_DATA, timeTrackingGraphBean.hasData() || aggregateTimeTrackingGraphBean.hasData());
        }
        else
        {
            paramsBuilder.add(HAS_DATA, timeTrackingGraphBean.hasData());
        }

        paramsBuilder.add(TIME_TRACKING_GRAPH_BEAN, timeTrackingGraphBean);
        paramsBuilder.add(I18N, authenticationContext.getI18nHelper());
        final Map<String, Object> startingParams = paramsBuilder.toMap();
        final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);

        return params;
    }

    /*
     * This is cached because this is very expensive to calculate and is calculated in few areas per request. E.g. View Issue (subtask block)
     */
    private AggregateTimeTrackingBean getAggregates(Issue issue, Map<String, Object> context)
    {
        final HttpServletRequest request = getRequest(context);
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

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get(ISSUE);
        final User user = (User) context.get("user");

        return issue.getId() + "/" + (user == null ? "" : user.getName());
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
