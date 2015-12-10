package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.template.VelocityTemplatingEngine;

import java.util.Comparator;
import java.util.Map;

/**
 * Abstract field for aggregate Time Tracking values.  This class looks after the cacheing of the
 * {@link com.atlassian.jira.issue.util.AggregateTimeTrackingBean} and provides a simple plugin point for
 * aggregate fields.
 *
 * @since v3.11
 */
public abstract class AbstractAggregateDurationSystemField extends AbstractDurationSystemField
{
    protected final AggregateTimeTrackingCalculatorFactory calculatorFactory;

    public AbstractAggregateDurationSystemField(String id, String nameKey, String columnHeadingKey, String defaultSortOrder, Comparator comparator, VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, AggregateTimeTrackingCalculatorFactory calculatorFactory)
    {
        super(id, nameKey, columnHeadingKey, defaultSortOrder, comparator, templatingEngine, applicationProperties, authenticationContext);
        this.calculatorFactory = calculatorFactory;
    }

    /**
     * Aggregate fields can not be sorted due to performance/security concerns.
     *
     * @return Always returns null
     */
    public LuceneFieldSorter getSorter()
    {
        return null;
    }

    protected Long getDuration(Issue issue)
    {
        return getAggregateDuration(getAggregateBean(issue));
    }

    /**
     * Method retrieves the Aggregate Bean from the {@link RequestCacheKeys} if available,
     * else creates one and places it in, using the issue key for uniqueness.
     *
     * @param issue the issue to calculate aggregate values.
     * @return Aggregate Bean
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

    /**
     * Method to extract the appropriate value from the aggregate bean.
     *
     * @param bean bean to extract value from
     * @return the appropriate duration value
     */
    protected abstract Long getAggregateDuration(AggregateTimeTrackingBean bean);
}
