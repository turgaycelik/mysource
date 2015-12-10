package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

/**
 * System field that displays the calculated aggregate original estimate.  This adds all sub-tasks values to its
 * own values.
 *
 * @since v3.11
 */
public class AggregateOriginalEstimateSystemField extends AbstractAggregateDurationSystemField
{

    public AggregateOriginalEstimateSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
                                                JiraAuthenticationContext authenticationContext, AggregateTimeTrackingCalculatorFactory calculatorFactory)
    {
        super(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE, "common.concepts.aggregate.original.estimate", "common.concepts.aggregate.original.estimate", ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.AGGREGATE_TIME_SPENT), templatingEngine, applicationProperties, authenticationContext, calculatorFactory);
    }

    /**
     * Returns the original estimate of the passed bean
     *
     * @param bean bean to get the aggregate duration from
     * @return aggregate duration
     */
    protected Long getAggregateDuration(AggregateTimeTrackingBean bean)
    {
        return bean.getOriginalEstimate();
    }
}
