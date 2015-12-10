package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

/**
 * System field that displays the calculated aggregate time spent.  This adds all sub-tasks values to its
 * own values.
 *
 * @since v3.11
 */
public class AggregateTimeSpentSystemField extends AbstractAggregateDurationSystemField
{

    public AggregateTimeSpentSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
                                         JiraAuthenticationContext authenticationContext, AggregateTimeTrackingCalculatorFactory calculatorFactory)
    {
        super(IssueFieldConstants.AGGREGATE_TIME_SPENT, "common.concepts.aggregate.time.spent", "common.concepts.aggregate.time.spent", ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.AGGREGATE_TIME_SPENT), templatingEngine, applicationProperties, authenticationContext, calculatorFactory);
    }

    /**
     * Returns the time spent of the passed bean
     *
     * @param bean bean to get the aggregate duration from
     * @return aggregate duration
     */
    protected Long getAggregateDuration(AggregateTimeTrackingBean bean)
    {
        return bean.getTimeSpent();
    }
}
