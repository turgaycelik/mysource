package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;

import java.util.Map;

/**
 * Progress Bar System Field that uses the aggregate values for percentages.
 *
 * @since v3.11
 */
public class AggregateProgressBarSystemField extends AbstractProgressBarSystemField
{
    private final AggregateTimeTrackingCalculatorFactory calculatorFactory;
    private final TimeTrackingGraphBeanFactory factory;
    private static final String PROGRESS_BAR_NAME = "common.concepts.aggregate.progress.bar";

    public AggregateProgressBarSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, AggregateTimeTrackingCalculatorFactory calculatorFactory,
            TimeTrackingGraphBeanFactory factory)
    {
        super(IssueFieldConstants.AGGREGATE_PROGRESS, PROGRESS_BAR_NAME, PROGRESS_BAR_NAME, templatingEngine, applicationProperties, authenticationContext);
        this.calculatorFactory = calculatorFactory;
        this.factory = factory;
    }

    @Override
    protected TimeTrackingParameters getTimeTrackingGraphBeanParameters(final Issue issue, final I18nHelper helper)
    {
        final AggregateTimeTrackingBean aggregateBean = getAggregateBean(issue);
        final TimeTrackingGraphBean trackingGraphBean = factory.createBean(aggregateBean, TimeTrackingGraphBeanFactory.Style.NORMAL, helper);
        return new TimeTrackingParameters(aggregateBean.getTimeSpent(), aggregateBean.getOriginalEstimate(), aggregateBean.getRemainingEstimate(), trackingGraphBean);
    }

    /**
     * Always returns 'apb'.
     *
     * @return always returns 'apb'
     */
    protected String getDisplayId()
    {
        return "apb";
    }

    /**
     * This field is not able to be sorted.
     *
     * @return always returns null
     */
    public LuceneFieldSorter getSorter()
    {
        return null;
    }
    /**
     * Method retrieves the Aggregate Bean from the {@link RequestCacheKeys} if available,
     * else creates one and places it in, using the issue key for uniqueness.
     *
     * @param issue the issue to calculate aggregate values.
     * @return Aggregate duration value
     */
    private AggregateTimeTrackingBean getAggregateBean(Issue issue)
    {
        final Map requestCache = JiraAuthenticationContextImpl.getRequestCache();
        final String cacheKey = RequestCacheKeys.AGGREGATE_TIMETRACKING_BEAN + issue.getKey();

        AggregateTimeTrackingBean bean = (AggregateTimeTrackingBean) requestCache.get(cacheKey);
        if (bean == null)
        {
            bean = calculatorFactory.getCalculator(issue).getAggregates(issue);
            requestCache.put(cacheKey, bean);
        }
        return bean;
    }
}
