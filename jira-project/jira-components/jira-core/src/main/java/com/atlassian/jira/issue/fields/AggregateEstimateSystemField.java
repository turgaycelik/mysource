package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

/**
 * System field that displays the calculated aggregate remaining estimate.  This adds all sub-tasks values to its
 * own values.
 *
 * @since v3.11
 */
public class AggregateEstimateSystemField extends AbstractAggregateDurationSystemField
{

    public AggregateEstimateSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
                                        JiraAuthenticationContext authenticationContext, AggregateTimeTrackingCalculatorFactory calculatorFactory)
    {
        super(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE, "common.concepts.aggregate.remaining.estimate", "common.concepts.aggregate.remaining.estimate", ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.AGGREGATE_TIME_SPENT), templatingEngine, applicationProperties, authenticationContext, calculatorFactory);
    }

    /**
     * Returns the remaining estimate of the passed bean
     *
     * @param bean bean to get the aggregate duration from
     * @return aggregate duration
     */
    protected Long getAggregateDuration(AggregateTimeTrackingBean bean)
    {
        return bean.getRemainingEstimate();
    }
}
